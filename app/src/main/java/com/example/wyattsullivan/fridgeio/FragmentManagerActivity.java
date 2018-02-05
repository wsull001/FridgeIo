package com.example.wyattsullivan.fridgeio;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentManagerActivity extends AppCompatActivity {

    private ListView productListView;
    private TextView mTextMessage;
    private DbHelper mydb;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    mTextMessage.setText(R.string.title_products);
                    return true;
                case R.id.navigation_bluetoothconnect:
                    mTextMessage.setText(R.string.title_bluetoothconnect);
                    return true;
                case R.id.navigation_grocerylist:
                    //Modification to temporarily access Add product activity
                    //Used for debugging/early emulating testing
                    //TODO: Remove this if necessary OR when AddProduct is able to be accessed normally
                    startActivity(new Intent(FragmentManagerActivity.this,Add_product.class));
                    mTextMessage.setText(R.string.title_grocerylist);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_manager);

        mydb = new DbHelper(this);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_home:
                                selectedFragment = Fragment_ProductView.newInstance();
                                break;
                            case R.id.navigation_bluetoothconnect:
                                selectedFragment = Fragment_Bluetooth.newInstance();
                                break;
                            case R.id.navigation_grocerylist:
                                selectedFragment = Fragment_GroceryList.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });
        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, Fragment_ProductView.newInstance());
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_button) {
            Intent intent = new Intent(this, Add_product.class);
            startActivity(intent);
            return true;
        }
        return false;
    }



}
