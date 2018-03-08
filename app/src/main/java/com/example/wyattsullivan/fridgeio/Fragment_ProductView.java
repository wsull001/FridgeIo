package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 1/30/18.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Fragment_ProductView extends Fragment {

    private static final String DATE_ADDED = "DA";
    private static final String EXPIRE_ONLY = "EO";
    private static final String EXPIRE_DATE = "ED";
    private static final String NAME_AZ = "NA";
    private String[] productNames;
    private String[] productDescriptions;
    private Bitmap[] arr;
    private String fridgeID;
    View view;
    productAdapter adapter;
    ArrayList<Product> prods;
    DbHelper dbHelp;
    ListView list;
    TextView emptyElement;


    public static Fragment_ProductView newInstance() {
        Fragment_ProductView fragment = new Fragment_ProductView();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu_product, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_button_product) {
            Intent intent = new Intent(getActivity(), Add_product.class);
            intent.putExtra("FridgeID", getActivity().getIntent().getStringExtra("FridgeID"));
            startActivity(intent);
            return true;
        }
        else if (id == R.id.home_button) {
            Intent intent = new Intent(getActivity(), FragmentManagerFridge.class);
            startActivity(intent);
            return true;
        }

        else if (id == R.id.sort_button) {
            // create new view to get the menu activity
            View menuView = getActivity().findViewById(R.id.sort_button);
            // create instance of popupmenu
            PopupMenu popup = new PopupMenu(getActivity(), menuView);
            // inflate menu view into popup menu
            popup.getMenuInflater().inflate(R.menu.popup_sort_product, popup.getMenu());
            // disable the first option in popupmenu ("Sort By:")
            popup.getMenu().getItem(0).setEnabled(false);
            // onClickItemListener for each sorting function call
            // TODO: CREATE METHOD TO KEEP SORT METHOD AFTER ADDING NEW ITEM
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.sort_by_date_added:
                            dbHelp = new DbHelper(getActivity());
                            prods = dbHelp.getProductsByDateAdded(fridgeID);
                            dbHelp.editSortMethod(fridgeID, DATE_ADDED);
                            loadAdapter();
                            adapter.changeProductItemList(productNames, arr, productDescriptions);
                            adapter.notifyDataSetChanged();
                        break;

                        case R.id.sort_by_expired_only:
                            dbHelp = new DbHelper(getActivity());
                            prods = dbHelp.getProductsByExpiredOnly(fridgeID);
                            dbHelp.editSortMethod(fridgeID, EXPIRE_ONLY);
                            loadAdapter();
                            adapter.changeProductItemList(productNames, arr, productDescriptions);
                            adapter.notifyDataSetChanged();
                        break;

                        case R.id.sort_by_expiration_date:
                            dbHelp = new DbHelper(getActivity());
                            prods = dbHelp.getProductsByExpDate(fridgeID);
                            dbHelp.editSortMethod(fridgeID, EXPIRE_DATE);
                            loadAdapter();
                            adapter.changeProductItemList(productNames, arr, productDescriptions);
                            adapter.notifyDataSetChanged();
                        break;

                        case R.id.sort_by_name:
                            dbHelp = new DbHelper(getActivity());
                            prods = dbHelp.getProductsByAlphabetical(fridgeID);
                            dbHelp.editSortMethod(fridgeID, NAME_AZ);
                            loadAdapter();
                            adapter.changeProductItemList(productNames, arr, productDescriptions);
                            adapter.notifyDataSetChanged();
                        break;

                        default:
                            return true;
                    }
                    return true;
                }
            });
            popup.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_productview, container, false);
        list = (ListView) view.findViewById(R.id.listView);
        emptyElement = view.findViewById(R.id.emptyElementProduct);
        fridgeID = getActivity().getIntent().getStringExtra("FridgeID");

        dbHelp = new DbHelper(getActivity());

        switch(dbHelp.getSortMethod(fridgeID)) {
            case DATE_ADDED:
                prods = dbHelp.getProductsByDateAdded(fridgeID);
            break;

            case EXPIRE_ONLY:
                prods = dbHelp.getProductsByExpiredOnly(fridgeID);
            break;

            case EXPIRE_DATE:
                prods = dbHelp.getProductsByExpDate(fridgeID);
            break;

            case NAME_AZ:
                prods = dbHelp.getProductsByAlphabetical(fridgeID);
            break;

            default:
                prods = dbHelp.getProductsByDateAdded(fridgeID);
            break;
        }

        loadAdapter();

        adapter = new productAdapter(getActivity(), productNames, arr, productDescriptions);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ProductPage.class);
                intent.putExtra("prodID", Fragment_ProductView.this.prods.get(position).getId());
                startActivity(intent);
            }
        });

        return view;
    }

    void loadAdapter()
    {
        if(prods == null)
        {
            // must initialize empty string array
            productNames = new String[0];
            productDescriptions = new String[0];
            arr = new Bitmap[0];
            emptyElement.setVisibility(View.VISIBLE);
            list.setVisibility(View.INVISIBLE);
        }
        else
        {
            productNames = new String[prods.size()];
            productDescriptions = new String[prods.size()];
            arr = new Bitmap[prods.size()];
            emptyElement.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);

            for (int i = 0; i < prods.size(); i++) {
                productNames[i] = prods.get(i).getName();
                productDescriptions[i] = stringDate(prods.get(i).getExpDate());
                arr[i] = prods.get(i).getImage();
            }
        }
    }

    String stringDate(Date tempDate)
    {
        Calendar exp = Calendar.getInstance();
        exp.setTime(tempDate);
        String expirationDate = "Expiration Date: " + (exp.get(Calendar.MONTH)+1) + "/"
                + (exp.get(Calendar.DAY_OF_MONTH)) + "/" + (exp.get(Calendar.YEAR));
        return  expirationDate;
    }
}

class productAdapter extends ArrayAdapter<String>
{
    Context context;
    Bitmap[] images;
    String[] titleArray;
    String[] descriptionArray;
    productAdapter(Context c, String[] titles, Bitmap[] imgs, String[] desc)
    {
        super(c, R.layout.single_productview, R.id.textViewTitle, titles);
        this.context = c;
        this.images = imgs;
        this.titleArray = titles;
        this.descriptionArray = desc;

    }

    @Override public int getCount() { return titleArray.length; }

    public void changeProductItemList(String[] tempTitles, Bitmap[] tempImgs, String[] tempDesc)
    {
        titleArray = tempTitles;
        descriptionArray = tempDesc;
        images = tempImgs;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // layout inflater object to converts xml appearance description into java object
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // look up how inflation/inflater works (watch video)
        View row = inflater.inflate(R.layout.single_productview, parent, false);

        ImageView myImage = row.findViewById(R.id.productView);
        TextView myTitle = row.findViewById(R.id.textViewTitle);
        TextView myDescription = row.findViewById(R.id.textViewDescription);

        myTitle.setText(titleArray[position]);
        myDescription.setText(descriptionArray[position]);
        if (images[position] == null)
            myImage.setImageResource(R.drawable.ic_food_default);
        else
            myImage.setImageBitmap(images[position]);

        return row;
    }
}