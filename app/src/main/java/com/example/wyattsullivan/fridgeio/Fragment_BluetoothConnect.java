package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 2/16/18.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Fragment_BluetoothConnect extends Fragment {
    public static Fragment_BluetoothConnect newInstance() {
        Fragment_BluetoothConnect fragment = new Fragment_BluetoothConnect();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /* Custom Menu Bar Boilerplate
     *   Create custom menu xml (or use already existing)
     *   Populate code below for each button in menu
     */

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.***menu activity***, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.***button id***) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bluetoothconnect, container, false);

        Button button = (Button) v.findViewById(R.id.bluetooth_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return v;
    }
}
