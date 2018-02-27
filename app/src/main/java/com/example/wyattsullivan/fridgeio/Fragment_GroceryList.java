package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 1/30/18.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.TextView;

public class Fragment_GroceryList extends Fragment {

    DbHelper dbHelp;
    GroceryItem[] groceries;
    String[] grocery_names;
    productAdapterGrocery adapter;
    ListView list;
    String mAddGrocery;
    TextView emptyList;

    public static Fragment_GroceryList newInstance() {
        Fragment_GroceryList fragment = new Fragment_GroceryList();
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

            // inflates dialog_newgroceryitem activity
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View mView = inflater.inflate(R.layout.dialog_newgroceryitem, null);

            // gets editText entry and populates it to grocery list
            // cancels dialog if cancel button
            final EditText new_grocery = (EditText) mView.findViewById(R.id.new_grocery);
            builder.setView(mView);
            builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    mAddGrocery = new_grocery.getText().toString();
                    dbHelp.addGroceryItem(mAddGrocery);
                    groceries = dbHelp.getGroceryItems();
                    grocery_names = new String[groceries.length];
                    for(int i = 0; i < groceries.length; i++) {
                            grocery_names[i] = groceries[i].getName();
                    }
                    if (groceries.length == 0) {
                        emptyList.setVisibility(View.VISIBLE);
                        list.setVisibility(View.INVISIBLE);
                    }
                    else {
                        emptyList.setVisibility(View.INVISIBLE);
                        list.setVisibility(View.VISIBLE);
                    }
                    adapter.changeGroceryItemsList(grocery_names);
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_grocerylist, container, false);
        list = (ListView) view.findViewById(R.id.listViewGrocery);
        emptyList = (TextView) view.findViewById(R.id.emptyElement);

        dbHelp = new DbHelper(getActivity());
        groceries = dbHelp.getGroceryItems();

        // Shows empty list message if length is 0 (nothing in array)
        if(groceries.length == 0) {
            emptyList.setVisibility(View.VISIBLE);
            list.setVisibility(View.INVISIBLE);
        }
        grocery_names = new String[groceries.length];
        for(int i = 0; i < groceries.length; i++) {
                grocery_names[i] = groceries[i].getName();
        }

        adapter = new productAdapterGrocery(getActivity(), grocery_names);
        list.setAdapter(adapter);

        // TODO: MAKE ONCLICK LISTENER FOR LISTVIEW TO HAVE DIALOG BOX POPUP TO EDIT TEXT OR EDIT TEXT IN LISTVIEW

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Delete Item?");
                builder.setPositiveButton("Delete", new MyDeleteButton(groceries[position].getId(), getActivity(), Fragment_GroceryList.this));
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });


        return view;
    }
    public void updateGroceryItems() {
        DbHelper dbHelp = new DbHelper(getActivity());
        groceries = dbHelp.getGroceryItems();
        grocery_names = new String[groceries.length];
        for (int i = 0; i < groceries.length; i++) {
            grocery_names[i] = groceries[i].getName();
        }
        if (groceries.length == 0) {
            emptyList.setVisibility(View.VISIBLE);
            list.setVisibility(View.INVISIBLE);
        }
        else {
            emptyList.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);
        }
        adapter.changeGroceryItemsList(grocery_names);
        adapter.notifyDataSetChanged();
    }
}


class productAdapterGrocery extends ArrayAdapter<String>
{
    Context context;
    String[] groceryItemsList;
    productAdapterGrocery(Context c, String[] titles)
    {
        super(c, R.layout.single_grocerylistview, R.id.groceryTitle, titles);
        this.context = c;
        this.groceryItemsList = titles;
    }

    @Override public int getCount() {
        return groceryItemsList.length;
    }


    public void changeGroceryItemsList(String[] newList) {
        groceryItemsList = newList;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // layout inflater object to converts xml appearance description into java object
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // look up how inflation/inflater works (watch video)
        View row = inflater.inflate(R.layout.single_grocerylistview, parent, false);

        TextView myTitle = row.findViewById(R.id.groceryTitle);

        myTitle.setText(groceryItemsList[position]);

        return row;
    }

}


class MyDeleteButton implements DialogInterface.OnClickListener {

    private int itemId;
    private Context ctxt;
    Fragment_GroceryList parent;
    MyDeleteButton(int itId, Context context, Fragment_GroceryList p) {
        itemId = itId;
        ctxt = context;
        parent = p;
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        DbHelper mydbHelp = new DbHelper(ctxt);
        mydbHelp.deleteGroceryItem(itemId);
        parent.updateGroceryItems();
        dialog.dismiss();
    }
}
