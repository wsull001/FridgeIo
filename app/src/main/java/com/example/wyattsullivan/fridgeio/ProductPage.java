package com.example.wyattsullivan.fridgeio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class ProductPage extends AppCompatActivity {
    private DbHelper dbHelper;
    private Product theProduct;
    private SeekBar seekBar;
    private TextView descriptionBox;
    private TextView name;
    private TextView expDate;
    private TextView addDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);
        Intent intent = getIntent();
        if (!intent.hasExtra("prodID")) {
            //TODO: Error it up!!!
        }

        String prodID = intent.getStringExtra("prodID");

        //get the product to display
        dbHelper = new DbHelper(this);
        theProduct = dbHelper.getProductById(prodID);

        //get the important elements of the view
        seekBar = (SeekBar)findViewById(R.id.capacitySeekBar);
        descriptionBox = (TextView)findViewById(R.id.productPageDescText);
        name = (TextView) findViewById(R.id.productPageName);
        expDate = (TextView) findViewById(R.id.productPageExpDate);
        addDate = (TextView) findViewById(R.id.productPageAddDate);



    }
}
