package com.example.wyattsullivan.fridgeio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by samhwang on 3/8/18.
 */

public class Dialog_Bluetooth {

    public Dialog_Bluetooth() {}

    public void showDialog(final Context ctxt, FridgeList fridgeList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctxt);

        LayoutInflater inflater = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.dialog_bluetooth_listview, null);

        TextView bluetooth_title = (TextView) mView.findViewById(R.id.new_title_bluetooth);
        bluetooth_title.setText("Bluetooth Title Here");

        final ListView bluetooth_list = (ListView) mView.findViewById(R.id.listViewBluetoothDialog);

        fridgeAdapter adapter = new fridgeAdapter(ctxt, fridgeList.getNames());
        bluetooth_list.setAdapter(adapter);

        bluetooth_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Toast.makeText(ctxt, bluetooth_list.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(mView);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                dialogInterface.cancel();
            }
        });

        builder.create();
        builder.show();
    }

}
