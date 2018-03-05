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
import android.widget.TextView;

import java.util.ArrayList;

public class Fragment_ProductView extends Fragment {

    private String[] productNames;
    private String[] productDescriptions;
    private Bitmap[] arr;
    private String fridgeID;
    ArrayList<Product> prods;
    DbHelper dbHelp;


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
        inflater.inflate(R.menu.action_bar_menu_w_home, menu);
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
        } else if (id == R.id.home_button) {
            Intent intent = new Intent(getActivity(), FragmentManagerFridge.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productview, container, false);
        ListView list = (ListView) view.findViewById(R.id.listView);
        TextView emptyElement = view.findViewById(R.id.emptyElementProduct);
        fridgeID = getActivity().getIntent().getStringExtra("FridgeID");


        dbHelp = new DbHelper(getActivity());
        prods = dbHelp.getProductsByDateAdded(fridgeID);

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
                productDescriptions[i] = prods.get(i).getDesc();
                arr[i] = prods.get(i).getImage();

            }
        }

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

        return view;
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

