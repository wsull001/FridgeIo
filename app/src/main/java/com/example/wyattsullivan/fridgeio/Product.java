package com.example.wyattsullivan.fridgeio;

import java.util.Date;

/**
 * Created by wyattsullivan on 1/29/18.
 */

public class Product {

    //TODO: add photo elements
    private String name;
    private Date expirationDate;
    private Date dateAdded;
    private String description;
    private int capacity;
    private String id;
    private int fridgeID;
    private String imageName;


    public Product() {
        name = null;
        expirationDate = null;
        description = null;
        capacity = -1;
        id = null;
        imageName = null;
    }

    public void setFridgeID(int fid) {
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

    public void setImageName(String iName) {
        imageName = iName;
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

    public int getFridgeID() {
        return fridgeID;
    }

    public String getImageName() {
        return imageName;
    }
}
