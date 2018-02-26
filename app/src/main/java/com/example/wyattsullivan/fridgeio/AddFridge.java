package com.example.wyattsullivan.fridgeio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddFridge extends AppCompatActivity {

    EditText nameField;
    Button submit;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fridge);

        nameField = (EditText) findViewById(R.id.new_fridge_name_field);
        submit = (Button) findViewById(R.id.new_fridge_submit);
        cancel = (Button) findViewById(R.id.new_fridge_cancel);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbHelper dbHelp = new DbHelper(AddFridge.this);
                dbHelp.createFridge(nameField.getText().toString());
                Intent intent = new Intent(AddFridge.this, FragmentManagerFridge.class);
                startActivity(intent);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFridge.this, FragmentManagerFridge.class);
                startActivity(intent);
            }
        });
    }
}
