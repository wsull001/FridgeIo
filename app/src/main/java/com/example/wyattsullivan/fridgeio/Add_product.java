package com.example.wyattsullivan.fridgeio;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.Date;

public class Add_product extends AppCompatActivity {
    //Widget Variables
    //TODO: Add support for image
    Button submitButton;
    DatePicker date;
    EditText name;
    EditText Description;
    //Calendar for the purpose of setting the datepicker to current date, user convenience
    Calendar cal = Calendar.getInstance();
    int currentYear = cal.get(Calendar.YEAR);
    int currentMonth = cal.get(Calendar.MONTH);
    int currentDay = cal.get(Calendar.DAY_OF_MONTH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        //Button Setter, Prepares for the submit button
        submitButton = (Button) findViewById(R.id.SubmitButton);
        submitButton.setOnClickListener(buttonClickListener);

        //Prepares DatePicker
        date = (DatePicker) findViewById(R.id.datePicker);
        date.updateDate(currentYear,currentMonth,currentDay);

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
                    tempDate.set(Calendar.YEAR, date.getYear());
                    tempDate.set(Calendar.MONTH, date.getMonth());
                    tempDate.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
                    Date exDate = tempDate.getTime();
                    //Create sample product
                    Product product = new Product();
                    product.setName(productname);
                    product.setExpirationDate(exDate);
                    product.setDateAdded(Calendar.getInstance().getTime());
                    product.setDescription(desc);

                    //Debugging Output
                    //Outputs simple information drawn from the product to ensure the product
                    //variable is properly created and that getters and setters are working
                    //TODO: Remove this when all is complete
                    String output = "Product: " + product.getName() + "\n"
                            + "Expire: " + product.getExpDate() + "\n"
                            + "Added: " + product.getDateAdded() + "\n"
                            + "Desc: " + product.getDesc();
                    Toast.makeText(getBaseContext(), output, Toast.LENGTH_LONG).show();
            }
        }
    };

}
