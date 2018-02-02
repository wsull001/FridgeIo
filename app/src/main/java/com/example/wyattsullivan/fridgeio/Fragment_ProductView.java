package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 1/30/18.
 */

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Fragment_ProductView extends Fragment {

    private String[] productNames;
    private String[] productDescriptions;
    private int[] arr;
    ArrayList<Product> prods;
    DbHelper dbHelp;


    public static Fragment_ProductView newInstance() {
        Fragment_ProductView fragment = new Fragment_ProductView();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productview, container, false);

        dbHelp = new DbHelper(getActivity());

        prods = dbHelp.getProductsByDateAdded();
        productNames = new String[prods.size()];
        productDescriptions = new String[prods.size()];
        arr = new int[prods.size()];

        for (int i = 0; i < prods.size(); i++) {
            productNames[i] = prods.get(i).getName();
            productDescriptions[i] = prods.get(i).getDesc();
            arr[i] = R.drawable.test;

        }

        //String[] productItems = {"Banana", "Orange", "Apple", "Banana", "Orange", "Apple", "Banana", "Orange", "Apple", "Banana", "Orange", "Apple", "Banana", "Orange", "Apple", "Banana", "Orange", "Apple"};

        ListView list = (ListView) view.findViewById(R.id.listView);

//        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(
//          getActivity(), android.R.layout.simple_list_item_1, productItems);

        productAdapter adapter = new productAdapter(getActivity(), productNames, arr, productDescriptions);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ProductPage.class);
                intent.putExtra("prodID", Fragment_ProductView.this.prods.get(position).getId());
                startActivity(intent);
            }
        });

        // TODO: WORK ON CREATING AN ONCLICKLISTENER FOR EACH LISTVIEW

        return view;
    }
}

class productAdapter extends ArrayAdapter<String>
{
    Context context;
    int[] images;
    String[] titleArray;
    String[] descriptionArray;
    productAdapter(Context c, String[] titles, int imgs[], String[] desc)
    {
        super(c, R.layout.single_productview, R.id.textViewTitle, titles);
        this.context = c;
        this.images = imgs;
        this.titleArray = titles;
        this.descriptionArray = desc;

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

        myImage.setImageResource(images[position]);
        myTitle.setText(titleArray[position]);
        myDescription.setText(descriptionArray[position]);

        return row;
    }

}

/*
Intent intent = new Intent(context, ProductPage.class);
intent.putExtra("prodID", product.getID_at_position)
*/


