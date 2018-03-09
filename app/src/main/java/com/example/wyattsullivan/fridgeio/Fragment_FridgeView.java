package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 2/16/18.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Fragment_FridgeView extends Fragment {

    DbHelper dbHelp;
    private String[] keys;
    String[] fridge_names;
    fridgeAdapter adapter;
    FridgeList fridgeList;
    String mAddFridge;
    ListView list;
    TextView emptyList;

    public static Fragment_FridgeView newInstance() {
        Fragment_FridgeView fragment = new Fragment_FridgeView();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_button) {
            // builds Alert Dialog in current activity
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // inflates dialog_newfridgeitem activity
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View mView = inflater.inflate(R.layout.dialog_newitem, null);
            TextView new_fridge_title = (TextView) mView.findViewById(R.id.new_title);
            new_fridge_title.setText(R.string.new_fridge_entry_title); // Title: "New Fridge Entry"

            // gets editText entry and populates it to fridge list
            // cancels dialog if cancel button
            final EditText new_fridge = (EditText) mView.findViewById(R.id.new_entry);
            new_fridge.setHint(R.string.new_fridge_item); // Hint: "Fridge Name"
            builder.setView(mView);
            builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    mAddFridge = new_fridge.getText().toString();
                    dbHelp.createFridge(mAddFridge);
                    fridgeList = dbHelp.getFridges();
                    if (fridgeList == null) {
                        emptyList.setVisibility(View.VISIBLE);
                        list.setVisibility(View.INVISIBLE);
                    }
                    else {
                        emptyList.setVisibility(View.INVISIBLE);
                        list.setVisibility(View.VISIBLE);
                        fridge_names = fridgeList.getNames();
                        keys = fridgeList.getIds();
                        setNotification();
                    }
                    adapter.changeFridgeList(fridge_names);
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    dialogInterface.cancel();
                }
            });

            builder.create();
            builder.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fridgeview, container, false);
        list = (ListView) view.findViewById(R.id.listViewFridge);
        emptyList = (TextView) view.findViewById(R.id.emptyElementFridge);

        dbHelp = new DbHelper(getActivity());

        fridgeList = dbHelp.getFridges();
        if(fridgeList == null) {
            // must initialize empty string array
            fridge_names = new String[0];
            keys = new String[0];
            emptyList.setVisibility(View.VISIBLE);
            list.setVisibility(View.INVISIBLE);
        }
        else {
            fridge_names = fridgeList.getNames();
            keys = fridgeList.getIds();
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), FragmentManagerProduct.class);
                intent.putExtra("FridgeID", Fragment_FridgeView.this.keys[position]);
                startActivity(intent);
            }
        });

        adapter = new fridgeAdapter(getActivity(), fridge_names);
        list.setAdapter(adapter);

        ///////// TODO: temp button pls delete when done
        ImageButton button = (ImageButton) view.findViewById(R.id.fakeButtonFridgeView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog_Bluetooth b = new Dialog_Bluetooth();
                b.showDialog(getActivity(), fridgeList);
            }
        });
        ///////// TODO: temp button pls delete when done

        return view;
        // TODO: Add functionality to delete fridge (swipe right to delet)
    }

    //set default notifications for new fridge
    // assuming new fridge is created at the BACK of the fridge list
    void setNotification() {
        int fsize = fridgeList.getSize();
        dbHelp.createNotification(keys[fsize-1]);
    }
}

class fridgeAdapter extends ArrayAdapter<String>
{
    Context context;
    String[] fridgeList;
    fridgeAdapter(Context c, String[] titles)
    {
        super(c, R.layout.single_fridgeview, R.id.fridgeTitle, titles);
        this.context = c;
        this.fridgeList = titles;
    }

    // TODO: Ask Wyatt how he knew to override getCount()
    @Override public int getCount() {
        return fridgeList.length;
    }

    public void changeFridgeList(String[] newList) { fridgeList = newList; }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // layout inflater object to converts xml appearance description into java object
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // look up how inflation/inflater works (watch video)
        View row = inflater.inflate(R.layout.single_fridgeview, parent, false);

        TextView myTitle = row.findViewById(R.id.fridgeTitle);

        myTitle.setText(fridgeList[position]);

        return row;
    }

}
