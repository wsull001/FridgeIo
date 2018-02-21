package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 2/16/18.
 */

import android.content.Intent;
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
    }

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
