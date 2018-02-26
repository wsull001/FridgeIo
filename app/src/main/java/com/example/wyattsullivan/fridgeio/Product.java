package com.example.wyattsullivan.fridgeio;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by wyattsullivan on 1/29/18.
 */

public class Product {

    private String name;
    private Date expirationDate;
    private Date dateAdded;
    private String description;
    private int capacity;
    private String id;
    private String fridgeID;
    private Bitmap bmap;
    private boolean isCapacity;


    public Product() {
        name = null;
        expirationDate = null;
        description = null;
        capacity = -1;
        id = null;
        bmap = null;
        isCapacity = true;
    }

    public void setFridgeID(String fid) {
        fridgeID = fid;
    }

    public void setExpirationDate(Date dt) {
        expirationDate = dt;
    }

    public void setDateAdded(Date da) {
        dateAdded = da;
    }

    public void setName(String nm) {
        name = nm;
    }

    public void setDescription(String desc) {
        description = desc;
    }

    public void setCapacity(int cap) {
        capacity = cap;
    }

    public void setId(String ID) {
        this.id = ID;
    }

    public void setImage(Bitmap b) {
        bmap = b;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return description;
    }

    public Date getExpDate() {
        return expirationDate;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public int getCapacity() {
        return capacity;
    }
    public String getId() {
        return id;
    }

    public String getFridgeID() {
        return fridgeID;
    }

    public Bitmap getImage() {
        return bmap;
    }

    public void setIsCapacity(boolean b) {
        isCapacity = b;
    }

    public boolean isCapacity() {
        return isCapacity;
    }

}
