package com.example.wyattsullivan.fridgeio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

public class ConnectionActivity extends AppCompatActivity {


    boolean isRunningConnection;
    Button pushFridge;

    BluetoothDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        isRunningConnection = false;
        setStatusText("Waiting");

        pushFridge = (Button) findViewById(R.id.connectPushFridge);

        pushFridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRunningConnection) {
                    isRunningConnection = true;
                    DbHelper dbHelp = new DbHelper(ConnectionActivity.this);
                    FridgeList fl = dbHelp.getFridges();
                    ArrayList<Product> ps = dbHelp.getProductsByAlphabetical(fl.getIds()[0]);
                    new Thread(new PushFridgeThread(ConnectionActivity.this, fl.getIds()[0], fl.getNames()[0], device, ps)).start();
                } else {
                    Toast.makeText(ConnectionActivity.this, "Wait for the current task to finish", Toast.LENGTH_SHORT).show();
                }
            }
        });

        device = getIntent().getExtras().getParcelable("device");
        ((TextView)findViewById(R.id.deviceDisplay)).setText(device.getName());






    }

    //used for setting status text
    public void setStatusText(String text) {

        ((TextView)findViewById(R.id.connectStatus)).setText(text);

    }

    public void notifyCompleted() {
        setStatusText("Waiting");
        isRunningConnection = false;
    }
}

class PercentageRun implements Runnable {

    int base;
    int high;
    ConnectionActivity activity;

    PercentageRun(int at, int tot, ConnectionActivity act) {
        base = at;
        high = tot;
        activity = act;

    }


    @Override
    public void run() {
        activity.setStatusText("Sending the fridge (" + ((base * 100) / high) + "%)");
    }
}

class PushFridgeThread implements Runnable {


    String fridgeID;
    String fridgeName;
    BluetoothDevice dev;
    ConnectionActivity parent;
    ArrayList<Product> products;


    PushFridgeThread(ConnectionActivity p, String id, String name, BluetoothDevice d, ArrayList<Product> pr) {
        fridgeID = id;
        dev = d;
        parent = p;
        fridgeName = name;
        products = pr;
    }

    @Override
    public void run() {
        BluetoothSocket sock = null;
        try {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            sock = dev.createRfcommSocketToServiceRecord(UUID.fromString("6e0817a8-21dc-11e8-b467-0ed5f89f718b"));

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("...connecting...");
                }
            });


            sock.connect();
            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("Sending the fridge (0%)");
                }
            });

            DataOutputStream os = new DataOutputStream(sock.getOutputStream());
            DataInputStream in = new DataInputStream(sock.getInputStream());

            byte[] acks = new byte[3];



            os.write("00".getBytes()); // hopefully trigger the fridge
            os.write(fridgeID.getBytes());
            os.writeInt(fridgeName.length());
            os.write(fridgeName.getBytes());
            in.readFully(acks);


            if ((new String(acks)).equals("ack")) {

                //send number of products
                os.writeInt(products.size());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                //to know whether to send end or not
                boolean successful = true;


                for (int i = 0; i < products.size(); i++) {

                    parent.runOnUiThread(new PercentageRun(i, products.size(), parent));

                    Product tp = products.get(i);
                    os.write(tp.getId().getBytes());
                    os.writeInt(tp.getName().length());
                    os.write(tp.getName().getBytes());
                    os.write(tp.getFridgeID().getBytes());
                    if (tp.getDesc() != null) {
                        os.writeInt(tp.getDesc().length());
                        os.write(tp.getDesc().getBytes());
                    } else {
                        os.writeInt(-1);
                    }
                    os.writeInt(tp.getCapacity());
                    String tmpDate = df.format(tp.getExpDate());
                    os.writeInt(tmpDate.length());
                    os.write(tmpDate.getBytes());
                    tmpDate = df.format(tp.getDateAdded());
                    os.writeInt(tmpDate.length());
                    os.write(tmpDate.getBytes());
                    if (tp.getImage() != null) {
                        byte[] tmpImage = DbHelper.bitmapToBytes(tp.getImage());
                        int imgSize = tmpImage.length;
                        os.writeInt(imgSize);
                        int off = 0;
                        int numPacs = imgSize / 900 + 1;
                        int curSeq = 0;
                        while (curSeq < numPacs) {
                            int to_send = Math.min(900, imgSize - 900 * curSeq);
                            os.write(tmpImage, 900 * curSeq, to_send);
                            int resp = in.readInt();
                            if (resp > curSeq) curSeq = resp;
                        }
                    } else {
                        os.writeInt(-1);
                    }
                    os.writeInt((tp.isCapacity() ? 1 : 0));
                    os.write("check".getBytes());
                    in.readFully(acks);
                    if (!(new String(acks)).equals("ack")) {
                        parent.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parent.setStatusText("Error: failed to send all the products");
                            }
                        });
                    }
                }

                os.write("end".getBytes());

                in.readFully(acks);

                if (new String(acks).equals("ack")) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parent.setStatusText("Fridge sent successfully");
                        }
                    });
                } else {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parent.setStatusText("Error: failed to send all the products");
                        }
                    });
                }


            } else {
                parent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parent.setStatusText("Error: Fridge already uploaded");
                    }
                });
            }
            sock.close();
        } catch(Exception e) {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (Exception e2) {

            }
            Log.d("Bluetooth", "Broken", e);
            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("Error: Something broke");
                }
            });
        }


        try {
            Thread.sleep(2000);
        } catch (Exception e) {}

        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parent.notifyCompleted();
            }
        });

    }
}
