package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 2/16/18.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class Fragment_BluetoothConnect extends Fragment {


    ArrayList<BluetoothDevice> devices;

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
        BluetoothAdapter mDevAdapt = BluetoothAdapter.getDefaultAdapter();


        if (mDevAdapt != null) {

            View v = inflater.inflate(R.layout.fragment_bluetoothconnect, container, false);

            ListView lv = (ListView) v.findViewById(R.id.pairedDeviceList);

            final Set<BluetoothDevice> deviceSet = mDevAdapt.getBondedDevices();
            devices = new ArrayList<BluetoothDevice>();

            if (deviceSet.size() != 0) {
                for (BluetoothDevice i : deviceSet) {
                    if (i != null)
                        devices.add(i);
                }
            }

            Intent intnt = new Intent(getActivity(), Add_product.class);

            DeviceListAdapter listAdpt = new DeviceListAdapter(getActivity(), devices);
            lv.setAdapter(listAdpt);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), ConnectionActivity.class);
                    intent.putExtra("device", devices.get(position));

                    startActivity(intent);

                }
            });


            return v;
        }
        View v = inflater.inflate(R.layout.fragment_no_bluetooth, container, false);

        return v;
    }
}

class DeviceListAdapter extends ArrayAdapter<String> {


    Context ctxt;
    ArrayList<BluetoothDevice> devs;
    DeviceListAdapter(Context c, ArrayList<BluetoothDevice> devices) {
        super(c, R.layout.single_fridgeview);
        devs = devices;
        ctxt = c;
    }

    @Override
    public int getCount() {
        return devs.size();
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.single_fridgeview, parent, false);

        TextView myTitle = row.findViewById(R.id.fridgeTitle);

        myTitle.setText(devs.get(position).getName());



        return row;

    }


}
