package com.example.wyattsullivan.fridgeio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
                                                         "prodID CHAR(30))");
        db.execSQL("CREATE TABLE IF NOT EXISTS GroceryList (grocID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                            "name TEXT, " +
                                                            "quantity INTEGER, " +
                                                            "isChecked INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Fridge (fridgeID CHAR(30) PRIMARY KEY, " +
                "fname TEXT)");

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

        long result = db.insert("Fridge", null, cv);

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

        cv = new ContentValues();
        cv.put("type", 1); //mark as new entry
        cv.put("prodID", key);

        result = db.insert("ToUpdate", null, cv);

        if (result == -1)
            return false;
        return true;

    }


    public boolean deleteProduct(String id) {
        //TODO: implement delete: DELETE PHOTO
        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete("ProductList", "prodID = '" + id + "'", null);
        if (res > 0) {
            //update records in update table
            Cursor curs = db.rawQuery("SELECT * FROM ToUpdate WHERE prodID = '" + id + "'", null);
            if (curs.getCount() > 0) {
                //record already in update table, update it
                ContentValues cv = new ContentValues();
                cv.put("type", 2); //mark as delete
                res = db.update("ToUpdate",cv, "prodID = '" + id + "'", null);

            } else {
                ContentValues cv = new ContentValues();
                cv.put("type", 2); //delete
                cv.put("prodID", id);
                long res2 = db.insert("ToUpdate", null, cv);
            }
        }

        return false;
    }


    public boolean updateProductFullness(String id, int cap) {
        //TODO: implement update fullness
        return false;
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


    public boolean updateCapacity(int capacity, String prodID) {
        //TODO: Finish capacity update
        return false;
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
        Cursor curs = db.rawQuery("SELECT " + productItems + " FROM ProductList P  ORDER BY expDate",
                null);
        if (curs.getCount() == 0)
            return null;
        return turnCursIntoProducts(curs);
    }

    public ArrayList<Product> getProductsByAlphabetical(String fridgeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT " + productItems + " FROM ProductList P  ORDER BY name COLLATE NOCASE ASC",
                null);
        if (curs.getCount() == 0)
            return null;
        return turnCursIntoProducts(curs);
    }

    public ArrayList<Product> getProductsByExpiredOnly(String fridgeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT " + productItems + " FROM ProductList P WHERE expDate < date('now') ORDER BY expDate",
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
