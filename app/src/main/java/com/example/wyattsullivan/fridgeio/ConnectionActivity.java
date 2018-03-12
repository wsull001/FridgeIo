package com.example.wyattsullivan.fridgeio;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;


class FridgeChoiceListener implements AdapterView.OnItemClickListener {

    FridgeList l;
    AlertDialog myDialog;
    int option;
    ConnectionActivity theAct;
    DbHelper db;

    FridgeChoiceListener(FridgeList ll, AlertDialog mD, int opt, ConnectionActivity tA, DbHelper data) {
        l = ll;
        myDialog = mD;
        option = opt;
        theAct = tA;
        db = data;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (option == 0) { //push this fridge
            if (db.isHosted(l.getIds()[position])) {
                Toast.makeText(theAct, "That fridge is already hosted", Toast.LENGTH_SHORT).show();
                myDialog.dismiss();
            } else {
                theAct.isRunningConnection = true;
                new Thread(new PushFridgeThread(theAct, l.getIds()[position], l.getNames()[position], theAct.device, db.getProductsByDateAdded(l.getIds()[position]))).start();
                myDialog.dismiss();
            }
        } else if (option == 1) {
            if (db.hasFridge(l.getIds()[position])) {
                Toast.makeText(theAct, "You already have this fridge", Toast.LENGTH_SHORT).show();
                myDialog.dismiss();
            } else {
                theAct.isRunningConnection = true;
                new Thread(new PullFridge(theAct,theAct.device, l.getIds()[position],l.getNames()[position])).start();
                myDialog.dismiss();
            }
        }
    }
}

public class ConnectionActivity extends AppCompatActivity {


    public boolean isRunningConnection;
    Button pushFridge;
    Button getFridge;
    Button syncFridges;
    public BluetoothDevice device;


    public void showDialog(final Context ctxt, FridgeList fridgeList, String title, int opt, DbHelper db) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctxt);

        LayoutInflater inflater = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.dialog_bluetooth_listview, null);

        TextView bluetooth_title = (TextView) mView.findViewById(R.id.new_title_bluetooth);
        bluetooth_title.setText(title);

        final ListView bluetooth_list = (ListView) mView.findViewById(R.id.listViewBluetoothDialog);

        fridgeAdapter adapter = new fridgeAdapter(ctxt, fridgeList.getNames());
        bluetooth_list.setAdapter(adapter);

        builder.setView(mView);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                dialogInterface.cancel();
            }
        });

        AlertDialog dialog = builder.create();

        bluetooth_list.setOnItemClickListener(new FridgeChoiceListener(fridgeList,dialog,opt,this, db));

        dialog.show();
    }

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
                    DbHelper dbHelp = new DbHelper(ConnectionActivity.this);
                    FridgeList fl = dbHelp.getFridges();
                    if (fl == null) {
                        Toast.makeText(ConnectionActivity.this, "No Fridges to push", Toast.LENGTH_SHORT).show();
                    } else {
                        showDialog(ConnectionActivity.this, fl, "tmp", 0, dbHelp); // 0: push fridge
                    }
                } else {
                    Toast.makeText(ConnectionActivity.this, "Wait for the current task to finish", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getFridge = (Button) findViewById(R.id.connectGetFridge);

        getFridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRunningConnection) {
                    isRunningConnection = true;
                    new Thread(new GetHostedFridges(ConnectionActivity.this, device, 1)).start();
                } else {
                    Toast.makeText(ConnectionActivity.this, "Wait for the current task to finish", Toast.LENGTH_SHORT).show();
                }
            }
        });

        syncFridges = (Button) findViewById(R.id.connectSync);

        syncFridges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRunningConnection) {
                    isRunningConnection = true;
                    new Thread(new GetHostedFridges(ConnectionActivity.this, device, 2)).start();
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

    public void setFridgeHosted(String fridgeID) {
        DbHelper db = new DbHelper(this);
        db.setIsHosted(fridgeID, true);
    }

    public void showFridgesFromRemote(FridgeList list) {
        DbHelper db = new DbHelper(this);
        showDialog(this, list, "Select a fridge to get", 1, db);
    }

    public void installFridge(String fid, String name, ArrayList<Product> prods, ArrayList<byte[]> imags) {
        DbHelper dbHelp = new DbHelper(this);
        dbHelp.createFridgeFromSync(name, fid);
        dbHelp.createNotification(fid);
        dbHelp.setIsHosted(fid, true);
        for (int i = 0; i < prods.size(); i++) {
            dbHelp.insertProductFromSync(prods.get(i), imags.get(i));
        }
    }


}

class PercentageRun implements Runnable {

    int base;
    int high;
    ConnectionActivity activity;
    String msg;

    PercentageRun(int at, int tot, ConnectionActivity act, String m) {
        base = at;
        high = tot;
        activity = act;
        msg = m;

    }


    @Override
    public void run() {
        activity.setStatusText(msg + " (" + ((base * 100) / high) + "%)");
    }
}

class DisplayFridgesRunner implements Runnable {

    ConnectionActivity parent;
    FridgeList list;
    public DisplayFridgesRunner(ConnectionActivity cAct, FridgeList ll) {
        parent = cAct;
        list = ll;
    }

    @Override
    public void run() {
        parent.notifyCompleted();
        parent.showFridgesFromRemote(list);
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
                if (products != null) {
                    os.writeInt(products.size());
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                    //to know whether to send end or not
                    boolean successful = true;


                    for (int i = 0; i < products.size(); i++) {

                        parent.runOnUiThread(new PercentageRun(i, products.size(), parent, "Sending the fridge"));

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
                } else {
                    os.writeInt(0);
                }

                os.write("end".getBytes());

                in.readFully(acks);

                if (new String(acks).equals("ack")) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parent.setStatusText("Fridge sent successfully");
                            parent.setFridgeHosted(fridgeID);
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

class GetHostedFridges implements Runnable {

    BluetoothDevice dev;
    ConnectionActivity parent;
    int option;

    public GetHostedFridges(ConnectionActivity p, BluetoothDevice d, int opt) {
        parent = p;
        dev = d;
        option = opt;
    }

    @Override
    public void run() {
        BluetoothSocket s = null;
        try {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            s = dev.createRfcommSocketToServiceRecord(UUID.fromString("6e0817a8-21dc-11e8-b467-0ed5f89f718b"));

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("...connecting...");
                }
            });

            s.connect();

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("getting fridges from server");
                }
            });

            DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
            DataInputStream dIn = new DataInputStream(s.getInputStream());
            if (option == 1) {
                dOut.write("01".getBytes());
            } else if (option == 2) {
                dOut.write("03".getBytes());
            }
            int msgLen = dIn.readInt();
            int numPacks = msgLen / 800 + 1;
            int currAck = 0;
            byte[] msg = new byte[msgLen];
            while (currAck < numPacks) {
                int off = currAck * 800;
                int tmp = dIn.read(msg, off, Math.min(800, msgLen - off));
                if (tmp == Math.min(msgLen - off, 800)) {
                    currAck++;
                }
                dOut.writeInt(currAck);
            } //msg should have been received now
            DataInputStream bIn = new DataInputStream(new ByteArrayInputStream(msg));
            int numFridges = bIn.readInt();

            Log.d("Bluetooth", "" + msgLen + " : " + numPacks);
            FridgeList fl = new FridgeList(numFridges);

            Log.d("Bluetooth", "" + msgLen + " : " + numPacks);

            for (int i = 0; i < numFridges; i++) {
                int len = bIn.readInt();
                byte[] buf = new byte[len];
                bIn.readFully(buf);
                String id = new String(buf);
                len = bIn.readInt();
                buf = new byte[len];
                bIn.readFully(buf);
                String name = new String(buf);
                fl.addFridge(name, id, i);
            }
            byte[] buf = new byte[5];
            bIn.readFully(buf);
            if (new String(buf).equals("check")) {
                dOut.write("ack".getBytes());
            } else {
                dOut.write("err".getBytes());
            }
            if (option == 1) {
                parent.runOnUiThread(new DisplayFridgesRunner(parent, fl));
                s.close();
            } else if (option == 2) {
                parent.runOnUiThread(new CheckFridgesHad(fl, s, parent));
            }

        } catch (Exception e) {
            Log.e("Bluetooth", "Failed to get fridges", e);
            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("Error: Failed to retrieve fridge list");
                }
            });

            try {
                Thread.sleep(2000);
            } catch (Exception e2) {

            }

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.notifyCompleted();
                }
            });
            try {
                s.close();
            } catch (Exception e2) {}
        }

    }
}

class PullFridge implements Runnable {

    ConnectionActivity parent;
    BluetoothDevice dev;
    String fId;
    String fName;

    public PullFridge(ConnectionActivity p, BluetoothDevice d, String id, String name) {
        parent = p;
        dev = d;
        fId = id;
        fName = name;
    }


    @Override
    public void run() {
        BluetoothSocket s = null;

        try {
            s = dev.createRfcommSocketToServiceRecord(UUID.fromString("6e0817a8-21dc-11e8-b467-0ed5f89f718b"));

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("...connecting...");
                }
            });

            s.connect();

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("Pulling fridge from server (0%)");
                }
            });

            DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
            DataInputStream dIn = new DataInputStream(s.getInputStream());

            dOut.write(("02" + fId).getBytes()); //write option and fridge Id to get to the server

            //receive all the products
            int msgLength = dIn.readInt();
            int numSegs = msgLength / 800 + 1;
            int curAck = 0; // no acks yet
            byte[] msg = new byte[msgLength];

            while (curAck < numSegs) {
                parent.runOnUiThread(new PercentageRun(curAck, numSegs, parent, "Pulling fridge from server"));
                int off = curAck * 800;
                int tmp = dIn.read(msg,off, Math.min(msgLength - off, 800));
                if (tmp == Math.min(800, msgLength - off))
                    curAck++;
                dOut.writeInt(curAck);
            }

            byte[] checker = new byte[5];
            dIn.readFully(checker);
            if (new String(checker).equals("check")) {
                dOut.write("ack".getBytes());
            } else {
                dOut.write("err".getBytes());
            }

            DataInputStream bIn = new DataInputStream(new ByteArrayInputStream(msg));

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("Got all the product data");
                }
            });

            ArrayList<Product> products = new ArrayList<Product>();
            ArrayList<byte[]> images = new ArrayList<byte[]>();

            int numProducts = bIn.readInt();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < numProducts; i++ ) {
                Product p = new Product();
                byte [] buf = new byte[30]; //id length
                bIn.readFully(buf);
                p.setId(new String(buf));
                int len = bIn.readInt();
                buf = new byte[len];
                bIn.readFully(buf);
                p.setName(new String(buf));
                buf = new byte[30]; //fridge id
                bIn.readFully(buf);
                p.setFridgeID(new String(buf));
                len = bIn.readInt();
                if (len == -1) {
                    p.setDescription("");
                } else {
                    buf = new byte[len];
                    bIn.readFully(buf);
                    p.setDescription(new String(buf));
                }
                len = bIn.readInt(); //read capacity
                p.setCapacity(len);
                len = bIn.readInt(); //read length of expDate
                buf = new byte[len];
                bIn.readFully(buf); //read expdate
                p.setExpirationDate(df.parse(new String(buf)));
                len = bIn.readInt(); //read length of date added
                buf = new byte[len];
                bIn.readFully(buf);
                p.setDateAdded(df.parse(new String(buf)));
                len = bIn.readInt(); //read length of image
                if (len == -1) {
                    buf = null;
                } else {
                    buf = new byte[len];
                    bIn.readFully(buf);
                }
                images.add(buf); //insert image into image list
                len = bIn.readInt();
                p.setIsCapacity(len == 1);
                products.add(p);
            }

            Log.d("Bluetooth", "num products: " + products.size());

            parent.runOnUiThread(new InsertFridgeRun(fName, fId, products, images, parent));


        } catch (Exception e) {
            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("Error, failed to get the fridge");
                }
            });


        } finally {

            try {
                Thread.sleep(2000);
                s.close();
            } catch (Exception e2) {

            }

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.notifyCompleted();
                }
            });

        }
    }
}

class InsertFridgeRun implements Runnable {

    String fName;
    String fID;
    ArrayList<Product> products;
    ArrayList<byte[]> images;
    ConnectionActivity act;

    public InsertFridgeRun(String name, String fid, ArrayList<Product> prods, ArrayList<byte[]> imags, ConnectionActivity a) {
        fName = name;
        fID = fid;
        products = prods;
        images = imags;
        act = a;
    }

    @Override
    public void run() {
        act.installFridge(fID, fName, products, images);
    }
}

class CheckFridgesHad implements Runnable {
    FridgeList list;
    BluetoothSocket socket;
    ConnectionActivity theActivity;
    public CheckFridgesHad(FridgeList l, BluetoothSocket s, ConnectionActivity act) {
        list = l;
        socket = s;
        theActivity = act;
    }

    @Override
    public void run() {
        DbHelper dbHelp = new DbHelper(theActivity);

        theActivity.setStatusText("Preparing updates");
        ArrayList<String> fridges = new ArrayList<String>();
        for (int i = 0; i < list.getSize(); i++) {
            if (dbHelp.hasFridge(list.getIds()[i])) {
                fridges.add(list.getIds()[i]);
            }
        }

        if (fridges.size() == 0) {
            theActivity.notifyCompleted();
        }

        //initialize different parts of the sync
        ArrayList<ArrayList<String>> toDelete = new ArrayList<ArrayList<String>>(); //keep track of all deletes per fridge
        ArrayList<ArrayList<Product>> toAdd = new ArrayList<ArrayList<Product>>(); //keep track of all new prods per fridge
        ArrayList<ArrayList<ProductCapacityPair>> toUpdate = new ArrayList<ArrayList<ProductCapacityPair>>(); //keep track of all updates
        ArrayList<ArrayList<ProductCapacityPair>> allFridgeProducts = new ArrayList<ArrayList<ProductCapacityPair>>();

        for (int i = 0; i < fridges.size(); i++) { //for each of the fridges, prepare update tables
            ArrayList<String> curDel = new ArrayList<String>();
            ArrayList<Product> curAdd = new ArrayList<Product>();
            ArrayList<ProductCapacityPair> curCapUpdate = new ArrayList<ProductCapacityPair>();
            ArrayList<ProductCapacityPair> curFridgeProducts = dbHelp.getFridgeItemCapacities(fridges.get(i));

            ArrayList<UpdateTriplet> curUpdates = dbHelp.getFridgeUpdates(fridges.get(i));
            for (int j = 0; j < curUpdates.size(); j++) {
                if (curUpdates.get(j).getType() == 2) {
                    curDel.add(curUpdates.get(j).getProductID());
                } else if (curUpdates.get(j).getType() == 1) {
                    curAdd.add(dbHelp.getProductById(curUpdates.get(j).getProductID()));
                } else if (curUpdates.get(j).getType() == 3) {
                    curCapUpdate.add(dbHelp.getProductCapacityPair(curUpdates.get(j).getProductID()));
                }
                dbHelp.resolveUpdate(curUpdates.get(j).getUpdateID());
            }
            toDelete.add(curDel);
            toAdd.add(curAdd);
            toUpdate.add(curCapUpdate);
            allFridgeProducts.add(curFridgeProducts);
        }
        for (int i = 0; i < fridges.size(); i++) {
            Log.d("Bluetooth", "Fridge: " + fridges.get(i) + "   Num Deletes: " + toDelete.get(i).size() + "   Num adds: " + toAdd.get(i).size() + "   Num updates: " + toUpdate.get(i).size());
        }

        new Thread(new SyncFridgesThread(toDelete, toAdd, toUpdate, allFridgeProducts, fridges,theActivity, socket)).start();



    }
}

class SyncFridgesThread implements Runnable {


    ArrayList<ArrayList<String>> allItemsToDelete;
    ArrayList<ArrayList<Product>> allItemsToAdd;
    ArrayList<ArrayList<ProductCapacityPair>> allCapacitiesToUpdate;
    ArrayList<ArrayList<ProductCapacityPair>> allItems;
    ArrayList<String> fridges;
    ConnectionActivity parent;
    BluetoothSocket socket;

    public SyncFridgesThread(ArrayList<ArrayList<String>> del, ArrayList<ArrayList<Product>> add,
                             ArrayList<ArrayList<ProductCapacityPair>> capUpdates,
                             ArrayList<ArrayList<ProductCapacityPair>> items,
                             ArrayList<String> fs,
                             ConnectionActivity p, BluetoothSocket s) {
        allItemsToDelete = del;
        allItemsToAdd = add;
        allCapacitiesToUpdate = capUpdates;
        fridges = fs;
        allItems = items;
        parent = p;
        socket = s;
    }



    @Override
    public void run() {
        try {

            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
            DataInputStream dIn = new DataInputStream(socket.getInputStream());

            dOut.writeInt(fridges.size()); //write number of fridges to

            for (int i = 0; i < fridges.size(); i++) {
                dOut.write(fridges.get(i).getBytes());

                ByteArrayOutputStream msgOut = new ByteArrayOutputStream();
                DataOutputStream dataMsgOut = new DataOutputStream(msgOut); //send items to delete followed by items to add
                                                                            // followed by items to update capacity
                                                                            //followed by all items
                ArrayList<String> toDelete = allItemsToDelete.get(i);
                ArrayList<Product> toAdd = allItemsToAdd.get(i);
                ArrayList<ProductCapacityPair> toUpdCapacity = allCapacitiesToUpdate.get(i);
                ArrayList<ProductCapacityPair> fridgeItems = allItems.get(i);


                //Write products to delete
                dataMsgOut.writeInt(toDelete.size());

                for (int j = 0; j < toDelete.size(); j++) {
                    dataMsgOut.write(toDelete.get(j).getBytes()); //write the product ids sequentially
                }

                dataMsgOut.write("end".getBytes());



                //Write products to add
                dataMsgOut.writeInt(toAdd.size());

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                for (int j = 0; j < toAdd.size(); j++) {
                    Product p = toAdd.get(j);
                    dataMsgOut.write(p.getId().getBytes()); //prodID
                    dataMsgOut.writeInt(p.getName().length()); //prodName length
                    dataMsgOut.write(p.getName().getBytes()); //prodName
                    dataMsgOut.write(p.getFridgeID().getBytes()); //fridgeID
                    if (p.getDesc() != null) {
                        dataMsgOut.writeInt(p.getDesc().length()); //prodDesc length
                        dataMsgOut.write(p.getDesc().getBytes()); //prodDesc
                    } else {
                        dataMsgOut.writeInt(-1);
                    }
                    dataMsgOut.writeInt(p.getCapacity()); //item capacity
                    String tmp = df.format(p.getExpDate());
                    dataMsgOut.writeInt(tmp.length());
                    dataMsgOut.write(tmp.getBytes()); //write exp date
                    tmp = df.format(p.getDateAdded());
                    dataMsgOut.writeInt(tmp.length());
                    dataMsgOut.write(tmp.getBytes());
                    if (p.getImage() == null) { //write Image
                        dataMsgOut.writeInt(-1);
                    } else {
                        byte[] buf = DbHelper.bitmapToBytes(p.getImage());
                        dataMsgOut.writeInt(buf.length);
                        dataMsgOut.write(buf);
                    }
                    dataMsgOut.writeInt((p.isCapacity() ? 1 : 0)); //write isCapacity as int

                }

                dataMsgOut.write("end".getBytes());



                //Write products to update capacity on
                dataMsgOut.writeInt(toUpdCapacity.size());

                for (int j = 0; j < toUpdCapacity.size(); j++) { //write id followed by capacity
                    dataMsgOut.write(toUpdCapacity.get(j).prodID.getBytes());
                    dataMsgOut.writeInt(toUpdCapacity.get(j).capacity);
                }

                dataMsgOut.write("end".getBytes());


                //write all products and capacities to
                dataMsgOut.writeInt(fridgeItems.size());

                for (int j = 0; j < fridgeItems.size(); j++) { //write id followed by capacity
                    dataMsgOut.write(fridgeItems.get(j).prodID.getBytes());
                    dataMsgOut.writeInt(fridgeItems.get(j).capacity);
                }

                dataMsgOut.write("end".getBytes()); //make sure end is reached successfully for ack response

                byte[] theMsg = msgOut.toByteArray();

                int msgLen = theMsg.length;

                int numPacks = theMsg.length / 800 + 1;
                int curSeq = 0;

                dOut.writeInt(msgLen);
                while (curSeq < numPacks) {
                    parent.runOnUiThread(new PercentageRun(curSeq, numPacks, parent, "Getting updates from fridge " + (i+1) + "/" + fridges.size()));
                    int off = curSeq * 800;
                    int len = Math.min(msgLen - off, 800);
                    dOut.write(theMsg, off, len);
                    int tmpAck = dIn.readInt();
                    if (tmpAck > curSeq) {
                        curSeq = tmpAck;
                    }
                }

                byte[] ack = new byte[3];

                dIn.readFully(ack);

                if (new String(ack).equals("ack")) {
                    Log.d("Bluetooth", "Sent successfully");
                }

                msgLen = dIn.readInt();
                numPacks = msgLen / 800 + 1;
                int currAck = 0;
                byte[] msg = new byte[msgLen];
                while (currAck < numPacks) {
                    int off = currAck * 800;
                    int tmp = dIn.read(msg, off, Math.min(800, msgLen - off));
                    if (tmp == Math.min(msgLen - off, 800)) {
                        currAck++;
                    }
                    dOut.writeInt(currAck);
                } //msg should have been received now
                DataInputStream bIn = new DataInputStream(new ByteArrayInputStream(msg));

                dOut.write("ack".getBytes());

                int numCapacityUpdates = bIn.readInt();

                ArrayList<ProductCapacityPair> capUpdates = new ArrayList<ProductCapacityPair>();

                for (int j = 0; j < numCapacityUpdates; j++) {
                    byte[] buf = new byte[30];
                    bIn.readFully(buf);
                    String pId = new String(buf);
                    int cap = bIn.readInt();
                    capUpdates.add(new ProductCapacityPair(pId, cap));
                }

                int numProductsAdd = bIn.readInt();

                ArrayList<Product> productsToPush = new ArrayList<Product>();
                ArrayList<byte[]> images = new ArrayList<byte[]>();

                for (int j = 0; j < numProductsAdd; j++) {
                    Product p = new Product();
                    byte [] buf = new byte[30]; //id length
                    bIn.readFully(buf);
                    p.setId(new String(buf));
                    int len = bIn.readInt();
                    buf = new byte[len];
                    bIn.readFully(buf);
                    p.setName(new String(buf));
                    buf = new byte[30]; //fridge id
                    bIn.readFully(buf);
                    p.setFridgeID(new String(buf));
                    len = bIn.readInt();
                    if (len == -1) {
                        p.setDescription("");
                    } else {
                        buf = new byte[len];
                        bIn.readFully(buf);
                        p.setDescription(new String(buf));
                    }
                    len = bIn.readInt(); //read capacity
                    p.setCapacity(len);
                    len = bIn.readInt(); //read length of expDate
                    buf = new byte[len];
                    bIn.readFully(buf); //read expdate
                    p.setExpirationDate(df.parse(new String(buf)));
                    len = bIn.readInt(); //read length of date added
                    buf = new byte[len];
                    bIn.readFully(buf);
                    p.setDateAdded(df.parse(new String(buf)));
                    len = bIn.readInt(); //read length of image
                    if (len == -1) {
                        buf = null;
                    } else {
                        buf = new byte[len];
                        bIn.readFully(buf);
                    }
                    images.add(buf); //insert image into image list
                    len = bIn.readInt();
                    p.setIsCapacity(len == 1);
                    productsToPush.add(p);
                }

                int numProductsDelete = bIn.readInt();

                ArrayList<String> productsToDelete = new ArrayList<String>();

                for (int j = 0; j < numProductsDelete; j++) {
                    byte[] buf = new byte[30];
                    bIn.readFully(buf);
                    productsToDelete.add(new String(buf));
                }

                parent.runOnUiThread(new RunFridgePullUpdate(parent, capUpdates, productsToPush, images, productsToDelete));
            }



        } catch (Exception e) {
            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.setStatusText("Error: unable to finish syncing all products");
                }
            });
        } finally {
            try {
                Thread.sleep(2000);
                socket.close();
            } catch(Exception e) {}

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.notifyCompleted();
                }
            });
        }
    }
}

class RunFridgePullUpdate implements Runnable {

    ConnectionActivity theAct;
    ArrayList<ProductCapacityPair> productsToUpdate;
    ArrayList<Product> products;
    ArrayList<byte[]> images;
    ArrayList<String> toDelete;


    public RunFridgePullUpdate(ConnectionActivity a, ArrayList<ProductCapacityPair> upd, ArrayList<Product> newStuff, ArrayList<byte[]> imgs, ArrayList<String> td) {
        theAct = a;
        productsToUpdate = upd;
        products = newStuff;
        images = imgs;
        toDelete = td;
    }

    @Override
    public void run() {

        theAct.setStatusText("Sync successful");
        DbHelper dbHelp = new DbHelper(theAct);

        for (int i = 0; i < productsToUpdate.size(); i++) {
            dbHelp.syncUpdateFullness(productsToUpdate.get(i).prodID, productsToUpdate.get(i).capacity);
        }

        for (int i = 0; i < products.size(); i++) {
            dbHelp.insertProductFromSync(products.get(i), images.get(i));
        }

        for (int i = 0; i < toDelete.size(); i++) {
            dbHelp.syncDeleteItem(toDelete.get(i));
        }


    }
}

