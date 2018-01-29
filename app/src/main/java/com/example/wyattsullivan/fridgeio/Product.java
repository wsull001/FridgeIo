package com.example.wyattsullivan.fridgeio;

import java.util.Date;

/**
 * Created by wyattsullivan on 1/29/18.
 */

public class Product {
    private String name;
    private Date expirationDate;
    private String description;
    private int capacity;

    public String getName() {
        return name;
    }

    public String getDesc() {
        return description;
    }

    public Date getExpDate() {
        return expirationDate;
    }

    public int getCapacity() {
        return capacity;
    }
}
