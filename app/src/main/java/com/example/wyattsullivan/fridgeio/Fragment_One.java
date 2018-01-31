package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 1/30/18.
 */

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.zip.Inflater;

public class Fragment_One extends Fragment {

    private String[] memeTitles;
    private String[] memeDescriptions;
    private int[] arr = {R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test};


    public static Fragment_One newInstance() {
        Fragment_One fragment = new Fragment_One();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);

        Resources res = getResources();
        memeTitles = res.getStringArray(R.array.titles);
        memeDescriptions = res.getStringArray(R.array.descriptions);

        //String[] productItems = {"Banana", "Orange", "Apple", "Banana", "Orange", "Apple", "Banana", "Orange", "Apple", "Banana", "Orange", "Apple", "Banana", "Orange", "Apple", "Banana", "Orange", "Apple"};

        ListView list = (ListView) view.findViewById(R.id.listView);

//        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(
//          getActivity(), android.R.layout.simple_list_item_1, productItems);

        productAdapter adapter = new productAdapter(getActivity(), memeTitles, arr, memeDescriptions);
        list.setAdapter(adapter);

        //list.setOnItemClickListener(new );

        //list.setAdapter(listViewAdapter);

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
        super(c, R.layout.single_row, R.id.textViewTitle, titles);
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
        View row = inflater.inflate(R.layout.single_row, parent, false);

        ImageView myImage = row.findViewById(R.id.imageView2);
        TextView myTitle = row.findViewById(R.id.textViewTitle);
        TextView myDescription = row.findViewById(R.id.textViewDescription);

        myImage.setImageResource(images[position]);
        myTitle.setText(titleArray[position]);
        myDescription.setText(descriptionArray[position]);

        return row;
    }
}


