package com.example.wyattsullivan.fridgeio;

/**
 * Created by wyattsullivan on 2/25/18.
 */

public class GroceryItem {
    private String name;
    private boolean isChecked;
    private int Quantity;
    private int id;

    public GroceryItem(String n, boolean c) {
        name = n;
        isChecked = c;
        Quantity = -1;
    }

    public void setIsChecked(boolean b) {
        isChecked = b;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setQuantity(int a) {
        Quantity = a;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setId(int i) {
        id = i;
    }

    public int getId() {
        return id;
    }
}
