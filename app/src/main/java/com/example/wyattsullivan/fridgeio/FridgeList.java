package com.example.wyattsullivan.fridgeio;

/**
 * Created by wyattsullivan on 2/20/18.
 */

public class FridgeList {

    private String[] names;
    private String[] ids;
    private int sz;
    public FridgeList(int size) {
        sz = size;
        names = new String[size];
        ids = new String[size];
    }

    public void addFridge(String n, String i, int index) {
        names[index] = n;
        ids[index] = i;
    }

    public String[] getNames() {
        return names;
    }

    public String[] getIds() {
        return ids;
    }

    public int getSize() {
        return sz;
    }

}
