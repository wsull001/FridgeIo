package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 1/30/18.
 */

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
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
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

public class Fragment_GroceryList extends Fragment {

    DbHelper dbHelp;
    GroceryItem[] groceries;
    String[] grocery_names;
    productAdapterGrocery adapter;
    SwipeMenuListView listView;
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

            // inflates dialog_newitem activity
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View mView = inflater.inflate(R.layout.dialog_newitem, null);
            TextView new_grocery_title = (TextView) mView.findViewById(R.id.new_title);
            new_grocery_title.setText(R.string.new_grocery_entry_title); // Title: "New Product Entry"

            // gets editText entry and populates it to grocery list
            // cancels dialog if cancel button
            final EditText new_grocery = (EditText) mView.findViewById(R.id.new_entry);
            new_grocery.setHint(R.string.grocery_name); // Hint: "Grocery Name"

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
                    }
                    else {
                        emptyList.setVisibility(View.INVISIBLE);
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

            builder.create();
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_grocerylist, container, false);
        listView = view.findViewById(R.id.listViewGrocery);
        emptyList = (TextView) view.findViewById(R.id.emptyElement);

        dbHelp = new DbHelper(getActivity());
        groceries = dbHelp.getGroceryItems();

        // Shows empty list message if length is 0 (nothing in array)
        if(groceries.length == 0) {
            emptyList.setVisibility(View.VISIBLE);
        }
        grocery_names = new String[groceries.length];
        for(int i = 0; i < groceries.length; i++) {
            grocery_names[i] = groceries[i].getName();
        }

        adapter = new productAdapterGrocery(getActivity(), grocery_names);
        listView.setAdapter(adapter);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(200);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
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
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View mView = inflater.inflate(R.layout.dialog_newitem, null);

                TextView new_edit_title = (TextView) mView.findViewById(R.id.new_title);
                new_edit_title.setText(R.string.edit_grocery_item_title); // Title: "Edit Grocery Item"

                final EditText new_grocery = (EditText) mView.findViewById(R.id.new_entry);
                new_grocery.setText(groceries[position].getName());
                new_grocery.setHint(R.string.grocery_name); // Hint: "Grocery Name"
                new_grocery.setSelection(new_grocery.getText().length()); // cursor at end of text

                builder.setView(mView);

                // pass EditText to getText on the onClick instead of initialization
                builder.setPositiveButton(R.string.edit, new MyEditButton(groceries[position].getId(), new_grocery, getActivity(), Fragment_GroceryList.this));
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.cancel();
                    }
                });

                builder.create();
                builder.show();
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
        }
        else {
            emptyList.setVisibility(View.INVISIBLE);
        }
        // changeGroceryItemsList not deleting on index properly (but works when adding values)
        // creating new adapter seems to fix this bug
        adapter = new productAdapterGrocery(getActivity(), grocery_names);
        listView.setAdapter(adapter);
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

class MyEditButton implements DialogInterface.OnClickListener {

    private int itemId;
    EditText updateName;
    private Context ctxt;
    Fragment_GroceryList parent;
    MyEditButton(int itId, EditText newName, Context context, Fragment_GroceryList p) {
        itemId = itId;
        updateName = newName;
        ctxt = context;
        parent = p;
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        DbHelper mydbHelp = new DbHelper(ctxt);
        mydbHelp.editGroceryItem(updateName.getText().toString(), itemId);
        parent.updateGroceryItems();
        dialog.dismiss();
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
