package com.example.wyattsullivan.fridgeio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

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
    private EditText quantity;

    private void setUpCapacity() {
        if (theProduct.isCapacity()) {
            quantity.setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.capacitySeekBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView6).setVisibility(View.INVISIBLE);
            ((TextView)findViewById(R.id.textView7)).setText("Quantity");
            findViewById(R.id.textView8).setVisibility(View.INVISIBLE);
            quantity.setText("" + theProduct.getCapacity());
            quantity.clearFocus();
        }
    }

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

        //set up capacity
        quantity = (EditText) findViewById(R.id.quantityValue);
        setUpCapacity();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(ProductPage.this);
                builder.setMessage("Delete Item or Move to Grocery List?");
                builder.setPositiveButton("Move to Grocery List", new deleteAndAddToGroceryList(ProductPage.this, theProduct.getId(), theProduct));
                builder.setNegativeButton("Delete", new deleteProductButton(ProductPage.this, theProduct));
                builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.setCancelable(true);
                builder.create();
                builder.show();
            }
        });
    }
}

class deleteAndAddToGroceryList implements DialogInterface.OnClickListener {

    Context context;
    String prod_id;
    String groceryItem;
    String fridgeID;
    deleteAndAddToGroceryList(Context c, String id, Product theProduct) {
        context = c;
        prod_id = id;
        groceryItem = theProduct.getName();
        fridgeID = theProduct.getFridgeID();
    }
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        DbHelper dbHelper = new DbHelper(context);
        dbHelper.deleteProduct(prod_id);
        dbHelper.addGroceryItem(groceryItem);
        Intent intent = new Intent(context, FragmentManagerProduct.class);
        intent.putExtra("FridgeID", fridgeID);
        context.startActivity(intent);
        dialogInterface.dismiss();
    }
}

class deleteProductButton implements DialogInterface.OnClickListener {

    Context context;
    String prod_id;
    String fridgeID;
    deleteProductButton(Context c, Product theProduct) {
        context = c;
        prod_id = theProduct.getId();
        fridgeID = theProduct.getFridgeID();
    }
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        DbHelper dbHelper = new DbHelper(context);
        dbHelper.deleteProduct(prod_id);
        Intent intent = new Intent(context, FragmentManagerProduct.class);
        intent.putExtra("FridgeID", fridgeID);
        context.startActivity(intent);
        dialogInterface.dismiss();
    }
}
