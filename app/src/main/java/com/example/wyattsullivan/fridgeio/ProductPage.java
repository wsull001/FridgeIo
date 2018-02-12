package com.example.wyattsullivan.fridgeio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Date;
import java.util.Calendar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ProductPage extends AppCompatActivity {
    private DbHelper dbHelper;
    private Product theProduct;
    private SeekBar seekBar;
    private TextView descriptionBox;
    private TextView name;
    private TextView expDate;
    private TextView addDate;
    private Button deleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);
        Intent intent = getIntent();
        if (!intent.hasExtra("prodID")) {
            //TODO: Error it up!!!
        }

        final String prodID = intent.getStringExtra("prodID");

        //get the product to display
        dbHelper = new DbHelper(this);
        theProduct = dbHelper.getProductById(prodID);
        deleteButton = (Button) findViewById(R.id.productPageDeleteButton);
        //get the important elements of the view
        seekBar = (SeekBar)findViewById(R.id.capacitySeekBar);
        descriptionBox = (TextView)findViewById(R.id.productPageDescText);
        name = (TextView) findViewById(R.id.productPageName);
        expDate = (TextView) findViewById(R.id.productPageExpDate);
        addDate = (TextView) findViewById(R.id.productPageAddDate);

        //Set the fields to be the product information that we need to appear
        name.setText(theProduct.getName());
        Calendar exp = Calendar.getInstance();
        exp.setTime(theProduct.getExpDate());
        String expirationDate = "Exp Date:\n" + (exp.get(Calendar.MONTH)+1) + "/"
                + (exp.get(Calendar.DAY_OF_MONTH)) + "/" + (exp.get(Calendar.YEAR));
        expDate.setText(expirationDate);
        Calendar add = Calendar.getInstance();
        add.setTime(theProduct.getDateAdded());
        String addedDate = "Add Date:\n" + (add.get(Calendar.MONTH)+1) + "/"
                + (add.get(Calendar.DAY_OF_MONTH)) + "/" + (add.get(Calendar.YEAR));
        addDate.setText(addedDate);
        descriptionBox.setText(theProduct.getDesc());

        seekBar.setProgress(theProduct.getCapacity());


        if (theProduct.getImage() != null)
            ((ImageView)findViewById(R.id.productPageImageView)).setImageBitmap(theProduct.getImage());
        else
            ((ImageView)findViewById(R.id.productPageImageView)).setImageResource(R.drawable.ic_food_default);

        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            public void onStopTrackingTouch(SeekBar seekBar){
                // TODO My code goes here

                int level = seekBar.getProgress();
                dbHelper.updateProductFullness(prodID, level);
            }

            public void onStartTrackingTouch(SeekBar seekBar){}

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){}
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteProduct(theProduct.getId());
                Intent intent = new Intent(ProductPage.this, FragmentManagerActivity.class);
                startActivity(intent);
            }
        });
    }
}
