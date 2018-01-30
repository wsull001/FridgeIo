package com.example.wyattsullivan.fridgeio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by wyattsullivan on 1/29/18.
 */

public class DbHelper extends SQLiteOpenHelper {
    public static final String dbName = "FridgeIo.db";


    public DbHelper(Context context) {
        super(context, dbName, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create the ProductList
        db.execSQL("CREATE TABLE ProductList (prodID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                            "name TEXT, " +
                                                            "FridgeID INTEGER, " +
                                                            "description TEXT," +
                                                            "fullness INTEGER," +
                                                            "expDate DATE, dateAdded DATE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS ToUpdate (updateID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                         "type INTEGER, " +
                                                         "prodID INTEGER," +
                                                         "FOREIGN KEY (prodID) REFERENCES ProductList(prodID))");
        db.execSQL("CREATE TABLE IF NOT EXISTS GroceryList (grocID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                            "name TEXT, " +
                                                            "quantity INT, " +
                                                            "notes TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS Fridge (fridgeID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fname TEXT, passwrd TEXT)");
    }

    public boolean insertProduct(Product prod) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
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

    private ArrayList<Product> turnCursIntoProducts(Cursor cursor) {
        ArrayList<Product> ret = new ArrayList<Product>();

        while (cursor.moveToNext()) {
            Product p = new Product();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            p.setId(cursor.getInt(0));
            p.setName(cursor.getString(1));
            p.setFridgeID(cursor.getInt(2));
            p.setDescription(cursor.getString(3));
            p.setCapacity(cursor.getInt(4));
            try {
                p.setExpirationDate(df.parse(cursor.getString(5)));
                p.setDateAdded(df.parse(cursor.getString(6)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            ret.add(p);

        }


        return ret;
    }


    public Product getProductById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor curs = db.rawQuery("SELECT * FROM ProductList WHERE prodID = " + Integer.toString(id), null);

        if (curs.getCount() == 0)
            return null;
        return turnCursIntoProducts(curs).get(0);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
