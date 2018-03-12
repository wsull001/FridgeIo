package com.example.wyattsullivan.fridgeio;

/**
 * Created by wyattsullivan on 3/11/18.
 */

public class UpdateTriplet {

    private final int type;
    private final int updID;
    private final String prodID;

    public UpdateTriplet(int tp, int uID, String pID) {
        this.type = tp;
        this.updID = uID;
        this.prodID = pID;
    }

    public int getType() { return type; }
    public int getUpdateID() { return updID; }
    public String getProductID() { return prodID; }

}