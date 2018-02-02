package com.example.wyattsullivan.fridgeio;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class Add_product extends AppCompatActivity {
    //Widget Variables
    //TODO: Add support for image

    DatePickerDialog.OnDateSetListener expDatePicker;
    Button submitButton;
    Button selectExpDateButton;
    Button cancelButton;
    EditText name;
    EditText Description;

    int expYear;
    int expMonth;
    int expDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        //Button Setter, Prepares for the submit button
        submitButton = (Button) findViewById(R.id.SubmitButton);
        submitButton.setOnClickListener(buttonClickListener);


        //Button Setter, Prepares for the cancel button
        cancelButton = (Button) findViewById(R.id.CancelButton);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goHome = new Intent(Add_product.this, FragmentManagerActivity.class);
                startActivity(goHome);
            }
        });


        //default exp will be today
        Calendar cal = Calendar.getInstance();
        expYear = cal.get(Calendar.YEAR);
        expMonth = cal.get(Calendar.MONTH);
        expDay = cal.get(Calendar.DAY_OF_MONTH);

        //Prepares DatePicker
        selectExpDateButton = (Button) findViewById(R.id.setExpButton);
        selectExpDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //get current date to set as default
                int year = expYear;
                int day = expDay;
                int month = expMonth;

                DatePickerDialog myDialog = new DatePickerDialog(Add_product.this, R.style.Theme_AppCompat_DayNight,
                        expDatePicker,
                        year, month, day);
                myDialog.show();
            }
        });

        expDatePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                expYear = year;
                expMonth = month;
                expDay = dayOfMonth;

                TextView text = (TextView) findViewById(R.id.ExpDateTextView);
                text.setText("" + (month+1) + "-" + dayOfMonth + "-" + year);
            }
        };


        //Edittexts
        name = (EditText) findViewById(R.id.Name);
        Description = (EditText) findViewById(R.id.Description);
    }

    //Listener for buttons,
    //For the sake of simplicity a single listener will handle the button requests
    //This will handle both the image button and the submit button
    //TODO: Note: that the image button is not yet implemented
    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.SubmitButton:
                    String productname = name.getText().toString();
                    String desc = Description.getText().toString();
                    //Note this roundabout Calendar junk is because many Date constructors are
                    //deprecated and this is one way to still get a Date variable
                    Calendar tempDate = Calendar.getInstance();
                    tempDate.set(Calendar.YEAR, expYear);
                    tempDate.set(Calendar.MONTH, expMonth);
                    tempDate.set(Calendar.DAY_OF_MONTH, expDay);
                    Date exDate = tempDate.getTime();
                    //Create sample product
                    Product product = new Product();
                    product.setName(productname);
                    product.setExpirationDate(exDate);
                    product.setDateAdded(Calendar.getInstance().getTime());
                    product.setDescription(desc);
                    product.setCapacity(100);

                    //Debugging Output
                    //Outputs simple information drawn from the product to ensure the product
                    //variable is properly created and that getters and setters are working

                    DbHelper myDB = new DbHelper(Add_product.this);
                    myDB.insertProduct(product);


                    //TODO: Remove comments when this is complete
                    /*
                    String output = "Product: " + product.getName() + "\n"
                            + "Expire: " + product.getExpDate() + "\n"
                            + "Added: " + product.getDateAdded() + "\n"
                            + "Desc: " + product.getDesc();
                    Toast.makeText(getBaseContext(), output, Toast.LENGTH_LONG).show();
                    */


                    Intent goHome = new Intent(Add_product.this, FragmentManagerActivity.class);
                    startActivity(goHome);
            }
        }
    };

}
