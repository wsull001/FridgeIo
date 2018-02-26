package com.example.wyattsullivan.fridgeio;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.util.Calendar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Date;

import static android.Manifest.permission.*;

public class Add_product extends AppCompatActivity {
    //Widget Variables
    //TODO: Add support for image

    DatePickerDialog.OnDateSetListener expDatePicker;
    Button submitButton;
    Button selectExpDateButton;
    Button imageButton;
    Button cancelButton;
    EditText name;
    EditText Description;
    ToggleButton capQuant;
    EditText itemQuant;

    private static final String IMAGE_DIRECTORY = "/productImage";
    private int GALLERY = 1, CAMERA = 2;
    private ImageView imageview;
    private String imageName;
    private Bitmap bitmap;
    int expYear;
    int expMonth;
    int expDay;
    String fridgeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        itemQuant = (EditText) findViewById(R.id.itemQuantity);

        itemQuant.setVisibility(View.INVISIBLE);


        capQuant = (ToggleButton) findViewById(R.id.toggleCapQuant);
        capQuant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { //quantity
                    Add_product.this.findViewById(R.id.itemQuantity).setVisibility(View.VISIBLE);
                } else { //capacity
                    Add_product.this.findViewById(R.id.itemQuantity).setVisibility(View.INVISIBLE);
                }
            }
        });


        fridgeID = getIntent().getStringExtra("FridgeID");


        //set the bitmap to null by default
        bitmap = null;
        imageName = "";
        imageview = (ImageView) findViewById(R.id.addProductImageView);
        //Button Setter, Prepares for the submit button
        submitButton = (Button) findViewById(R.id.SubmitButton);
        submitButton.setOnClickListener(buttonClickListener);

        imageButton = (Button) findViewById(R.id.image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

        //Button Setter, Prepares for the cancel button
        cancelButton = (Button) findViewById(R.id.CancelButton);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goHome = new Intent(Add_product.this, FragmentManagerProduct.class);
                goHome.putExtra("FridgeID", fridgeID);
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
                    product.setFridgeID(fridgeID);
                    product.setDescription(desc);
                    if (capQuant.isChecked()) {
                        product.setCapacity(Integer.parseInt(itemQuant.getText().toString()));
                    } else {
                        product.setCapacity(100);
                    }
                    product.setImage(bitmap);
                    product.setIsCapacity(!capQuant.isChecked());

                    //Debugging Output
                    //Outputs simple information drawn from the product to ensure the product
                    //variable is properly created and that getters and setters are working

                    DbHelper myDB = new DbHelper(Add_product.this);
                    Log.d("Database", "trying to insert product");
                    myDB.insertProduct(product, bitmap);



                    Intent goHome = new Intent(Add_product.this, FragmentManagerProduct.class);
                    goHome.putExtra("FridgeID", fridgeID);
                    startActivity(goHome);
            }
        }
    };

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                photoFromGallary();
                                break;
                            case 1:

                                photoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void photoFromGallary() {
        int writepermission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        int readpermission = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);

        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void photoFromCamera() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }

        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    this.bitmap = bitmap;
                    Toast.makeText(Add_product.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    imageview.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(Add_product.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imageview.setImageBitmap(thumbnail);
            bitmap = thumbnail;
        }
    }

}
