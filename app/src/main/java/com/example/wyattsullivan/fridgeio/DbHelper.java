package com.example.wyattsullivan.fridgeio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by wyattsullivan on 1/29/18.
 */

public class DbHelper extends SQLiteOpenHelper {
    public static final String dbName = "FridgeIo.db";
                                                //0       //1     //2         //3            //4         //5        //6          //7      //8
    private static final String productItems = "P.prodID, P.name, P.FridgeID, P.description, P.fullness, P.expDate, P.dateAdded, P.image, P.isCapacity";

    private Context ctxt;

    public static byte[] bitmapToBytes(Bitmap bMap) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        bMap.compress(Bitmap.CompressFormat.PNG, 0, byteOut);
        return byteOut.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public DbHelper(Context context) {
        super(context, dbName, null, 1);
        ctxt = context;
        SQLiteDatabase db = this.getWritableDatabase();
    }

    public boolean syncUpdateFullness(String pId, int cap) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("fullness", cap);

        long result = db.update("ProductList", cv, "prodID = '" + pId + "'", null);

        return (result != -1);
    }

    public boolean syncDeleteItem(String pId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete("ProductList", "prodID = '" + pId + "'", null);

        return (res != -1);
    }

    ArrayList<UpdateTriplet> getFridgeUpdates(String fid) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT U.type, U.updateID, U.prodID FROM ToUpdate U WHERE U.fridgeID = '" + fid + "'", null);
        ArrayList<UpdateTriplet> ret = new ArrayList<UpdateTriplet>();

        for (int i = 0; i < curs.getCount(); i++) {
            curs.moveToNext();

            ret.add(new UpdateTriplet(curs.getInt(0), curs.getInt(1), curs.getString(2)));
        }

        return ret;
    }




    @Override
    public void onCreate(SQLiteDatabase db) {
        //create the ProductList
        db.execSQL("CREATE TABLE IF NOT EXISTS ProductList (prodID CHAR(30) PRIMARY KEY, " +
                                                            "name TEXT, " +
                                                            "FridgeID CHAR(30), " +
                                                            "description TEXT," +
                                                            "fullness INTEGER," +
                                                            "expDate DATE, dateAdded DATE," +
                                                            "image BLOB, isCapacity INTEGER)"); //isCapacity 0-quantity 1-capacity
        //1-new, 2-delete, 3-updated
        db.execSQL("CREATE TABLE IF NOT EXISTS ToUpdate (updateID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                         "type INTEGER, " +
                                                         "prodID CHAR(30), " +
                                                         "fridgeID CHAR(30))");
        db.execSQL("CREATE TABLE IF NOT EXISTS GroceryList (grocID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                            "name TEXT, " +
                                                            "quantity INTEGER, " +
                                                             "isChecked INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Fridge (fridgeID CHAR(30) PRIMARY KEY, " +
                "fname TEXT," +
                "sort TEXT," +
                "isHosted INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Notifications (notifID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                                "FridgeID CHAR(30), " +
                                                                "enabled INTEGER, " +
                                                                "hour INTEGER, " +
                                                                "minute INTEGER, " +
                                                                "frequency INTEGER)");
    }


    private String genRand30Char() { //new primary key for fridge and productList
        StringBuffer ret = new StringBuffer(30);
        for (int i = 0; i < 30; i++) {
            int randNum = (int)(Math.random() * 52);
            char nextChar = (char)(randNum > 25 ? ('A' + randNum - 26) : ('a' + randNum));
            ret.append(nextChar);
        }
        return ret.toString();
    }




    public boolean createFridge(String name) {
        String key = genRand30Char();
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();

        cv.put("fridgeID", key);
        cv.put("fname", name);
        cv.put("sort", "DA");

        long result = db.insert("Fridge", null, cv);

        return (result != -1);
    }



    public boolean insertProductFromSync(Product prod, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String key = prod.getId();

        //if there is an image set it up
        if (image != null) {
            cv.put("image", image);
        } else {
            cv.putNull("image");
        }
        cv.put("isCapacity", (prod.isCapacity() ? 1 : 0));
        cv.put("prodID", key);
        cv.put("name", prod.getName());
        cv.put("FridgeID", prod.getFridgeID());
        cv.put("description", prod.getDesc());
        cv.put("fullness", prod.getCapacity());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        cv.put("expDate", df.format(prod.getExpDate()));
        cv.put("dateAdded", df.format(prod.getDateAdded()));
        long result = db.insert("ProductList",null, cv);
        if (result == -1) {
            return false;
        }
        return true;

    }


    public boolean createFridgeFromSync(String name, String id) {
        String key = id;
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();

        cv.put("fridgeID", key);
        cv.put("fname", name);
        cv.put("sort", "DA");

        long result = db.insert("Fridge", null, cv);

        return (result != -1);
    }

    public boolean resolveUpdate(int updID) {
        SQLiteDatabase db = getWritableDatabase();
        long result = db.delete("ToUpdate", "updateID = " + updID, null);

        return (result != -1);
    }

    public boolean createUpdate(String pID, int type, String fID) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor curs = db.rawQuery("SELECT type, updateID FROM ToUpdate WHERE prodID = '" + pID + "'", null);
        if (curs.getCount() > 0) { //already exists
            curs.moveToNext();
            int curType = curs.getInt(0);
            int curID = curs.getInt(1);
            if (curType == 1) { //currently new, don't do anything
                if (type == 2) { //remove the update from the table, it's no longer needed
                    long result = db.delete("ToUpdate", "updateID = " + curID, null);
                    return (result != -1);
                }
                //otherwise nothing is needed to be done
                return true;
            } else if (curType == 3) {
                if (type == 2) { //change from update to delete record
                    ContentValues cv = new ContentValues();
                    cv.put("type", type);

                    long result = db.update("ToUpdate", cv, "updateID = " + curID, null);
                    return (result != -1);
                }
                return true; //can't add a product that already exists, if it's updating again it's already marked as updated
            }
            return true; //it shouldn't really reach here, just in case something goes wrong
        }

        //otherwise the product isn't in the update table, put it in there
        ContentValues cv = new ContentValues();
        cv.put("type", type);
        cv.put("prodID", pID);
        cv.put("fridgeID", fID);
        long result = db.insert("ToUpdate", null, cv);

        return (result != -1);
    }


    //Insert product, picture, and add to update table
    public boolean insertProduct(Product prod, Bitmap bmap) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String key = genRand30Char();
        String imageKey = null;

        //if there is an image set it up
        if (bmap != null) {
            cv.put("image", bitmapToBytes(bmap));
        } else {
            cv.putNull("image");
        }
        cv.put("isCapacity", (prod.isCapacity() ? 1 : 0));
        cv.put("prodID", key);
        cv.put("name", prod.getName());
        cv.put("FridgeID", prod.getFridgeID());
        cv.put("description", prod.getDesc());
        cv.put("fullness", prod.getCapacity());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        cv.put("expDate", df.format(prod.getExpDate()));
        cv.put("dateAdded", df.format(prod.getDateAdded()));
        long result = db.insert("ProductList",null, cv);
        if (result == -1) {
            return false;
        }


        if (isHosted(prod.getFridgeID())) {
            createUpdate(key, 1, prod.getFridgeID()); //create a new item update
        }

        if (result == -1)
            return false;
        return true;

    }


    public boolean deleteProduct(String id, String fid) {
        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete("ProductList", "prodID = '" + id + "'", null);

        if (isHosted(fid)) {
            Log.d("Database", "Deleting product on hosted fridge");
            createUpdate(id, 2, fid); //delete
        }

        return (res > 0);
    }


    public boolean updateProductFullness(String id, int cap, String fid) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("fullness", cap);

        long result = db.update("ProductList", cv, "prodID = '" + id + "'", null);

        if (isHosted(fid)) {
            createUpdate(id, 3, fid); //capacity update
        }



        return (result != -1);
    }

    private ArrayList<Product> turnCursIntoProducts(Cursor cursor) {
        ArrayList<Product> ret = new ArrayList<Product>();

        while (cursor.moveToNext()) {
            Product p = new Product();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            p.setId(cursor.getString(0));
            p.setName(cursor.getString(1));
            p.setFridgeID(cursor.getString(2));
            p.setDescription(cursor.getString(3));
            p.setCapacity(cursor.getInt(4));
            try {
                p.setExpirationDate(df.parse(cursor.getString(5)));
                p.setDateAdded(df.parse(cursor.getString(6)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte[] img = cursor.getBlob(7);
            if (img != null) {
                p.setImage(bytesToBitmap(img));
            }

            p.setIsCapacity((cursor.getInt(8) == 1));
            ret.add(p);

        }


        return ret;
    }





    public Product getProductById(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT " + productItems + " FROM ProductList P WHERE P.prodID = '" + id + "'",
                null);

        if (curs.getCount() == 0)
            return null;
        return turnCursIntoProducts(curs).get(0);

    }

    public ArrayList<Product> getProductsByDateAdded(String fridgeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT " + productItems + " FROM ProductList P WHERE FridgeID = '" + fridgeID + "' ORDER BY dateAdded",
                null);
        if (curs.getCount() == 0)
            return null;
        return turnCursIntoProducts(curs);
    }

    public ArrayList<Product> getProductsByExpDate(String fridgeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT " + productItems + " FROM ProductList P WHERE FridgeID = '" + fridgeID + "' ORDER BY expDate",
                null);
        if (curs.getCount() == 0)
            return null;
        return turnCursIntoProducts(curs);
    }

    public ArrayList<Product> getProductsByAlphabetical(String fridgeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT " + productItems + " FROM ProductList P WHERE FridgeID = '" + fridgeID + "' ORDER BY name COLLATE NOCASE ASC",
                null);
        if (curs.getCount() == 0)
            return null;
        return turnCursIntoProducts(curs);
    }

    public ArrayList<Product> getProductsByExpiredOnly(String fridgeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT " + productItems + " FROM ProductList P WHERE FridgeID = '" + fridgeID + "' AND expDate < date('now') ORDER BY expDate",
                null);
        if (curs.getCount() == 0)
            return null;
        return turnCursIntoProducts(curs);
    }

    public FridgeList getFridges() {
        FridgeList ret;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT fridgeID, fname FROM Fridge", null);
        int size = curs.getCount();
        if (size == 0)
            return null;
        ret = new FridgeList(size);
        curs.moveToNext();
        for (int i = 0; i < curs.getCount(); i++) {
            ret.addFridge(curs.getString(1), curs.getString(0), i);
            curs.moveToNext();
        }
        return ret;
    }

    public String getSortMethod(String fridgeID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor curs = db.rawQuery("SELECT sort FROM Fridge WHERE FridgeID='" + fridgeID + "'", null);
        curs.moveToFirst();
        return curs.getString(0);
    }

    public boolean editSortMethod(String fridgeID, String sort) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newValue = new ContentValues();
        newValue.put("sort", sort);
        long result = db.update("Fridge", newValue, "fridgeID='"+fridgeID+"'", null);
        return (result != -1);
    }

    // initialize settings when creating new fridge
    public void createNotification(String fid) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();

        cv.put("FridgeID", fid);
        cv.put("enabled", 1);
        cv.put("hour", 12);
        cv.put("minute", 0);
        cv.put("frequency", 2);

        db.insert("Notifications", null, cv);
    }

    public void editNotificationEnabled(String fid, int en) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        cv.put("enabled", en);
        db.update("Notifications", cv, "FridgeID='"+fid+"'", null);
    }

    public void editNotificationHour(String fid, int h) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        cv.put("hour", h);
        db.update("Notifications", cv, "FridgeID='"+fid+"'", null);
    }

    public void editNotificationMinute(String fid, int m) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        cv.put("minute", m);
        db.update("Notifications", cv, "FridgeID='"+fid+"'", null);
    }

    public void editNotificationFrequency(String fid, int f) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        cv.put("frequency", f);
        db.update("Notifications", cv, "FridgeID='"+fid+"'", null);
    }

    // call desired notification by fridgeID
    public Notification getNotification(String fid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor curs = db.rawQuery("SELECT * FROM Notifications WHERE fridgeID='"+fid+"'", null);
        curs.moveToFirst();
        Notification n = new Notification(fid, curs.getInt(0),curs.getInt(2), curs.getInt(3), curs.getInt(4), curs.getInt(5));
        return n;
    }

    public void addGroceryItem(String name) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        SQLiteDatabase db = getWritableDatabase();
        db.insert("GroceryList", null, cv);
    }

    public boolean editGroceryItem(String newName, int id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues newValue = new ContentValues();
        newValue.put("name", newName);
        long result = db.update("GroceryList", newValue, "grocID=" + id, null);
        return (result != -1);
    }

    public GroceryItem[] getGroceryItems() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT * FROM GroceryList", null);
        int num = curs.getCount();
        GroceryItem[] ret = new GroceryItem[num];
        int index = 0;
        while (curs.moveToNext()) {
            ret[index] = new GroceryItem(curs.getString(1),(curs.getInt(3) == 1));
            ret[index].setId(curs.getInt(0));
            index++;
        }
        return ret;
    }

    public boolean deleteGroceryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();

        long result = db.delete("GroceryList", "grocID = " + id, null);
        return (result != -1);
    }

    public ProductCapacityPair getProductCapacityPair(String id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cur = db.rawQuery("SELECT P.prodID, P.fullness FROM ProductList P WHERE P.prodID = '" + id + "'", null);
        if (cur.getCount() <= 0) return null;
        cur.moveToNext();
        return new ProductCapacityPair(cur.getString(0), cur.getInt(1));
    }

    public ArrayList<ProductCapacityPair> getFridgeItemCapacities(String fid) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT P.prodID, P.fullness FROM ProductList P WHERE P.FridgeID = '" + fid + "'",null);
        ArrayList<ProductCapacityPair> ret = new ArrayList<ProductCapacityPair>();
        for (int i = 0; i < curs.getCount(); i++) {
            curs.moveToNext();
            ret.add(new ProductCapacityPair(curs.getString(0), curs.getInt(1)));
        }
        return ret;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean isHosted(String fID) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT isHosted FROM Fridge WHERE fridgeID = '" + fID + "'", null);
        if (curs.getCount() <= 0)
            return true; //shouldn't happen, but if it does do this
        curs.moveToNext();
        return (curs.getInt(0) == 1);
    }

    public void setIsHosted(String fID, boolean isHosted) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isHosted", (isHosted ? 1 : 0));
        db.update("Fridge", cv, "fridgeID = '" + fID + "'", null);
    }

    public boolean hasFridge(String fID) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor curs = db.rawQuery("SELECT * FROM Fridge WHERE fridgeID = '" + fID + "'", null);

        return (curs.getCount() > 0);

    }
}
