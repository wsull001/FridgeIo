package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 2/16/18.
 */

import android.content.Context;
import android.graphics.Bitmap;
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

public class Fragment_FridgeView extends Fragment {
    private String[] fridgeNames = {"Fridge 1", "Fridge 2", "Fridge 3"};
    private String[] fridgeDescriptions = {"Des 1", "Des 2", "Des 3"};

    public static Fragment_FridgeView newInstance() {
        Fragment_FridgeView fragment = new Fragment_FridgeView();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fridgeview, container, false);

        ListView list = (ListView) view.findViewById(R.id.listViewFridge);

        fridgeAdapter adapter = new fridgeAdapter(getActivity(), fridgeNames, fridgeDescriptions);
        list.setAdapter(adapter);

        return view;
    }
}

class fridgeAdapter extends ArrayAdapter<String>
{
    Context context;
    String[] titleArray;
    String[] descriptionArray;
    fridgeAdapter(Context c, String[] titles, String[] desc)
    {
        super(c, R.layout.single_productview, R.id.textViewTitle, titles);
        this.context = c;
        this.titleArray = titles;
        this.descriptionArray = desc;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // layout inflater object to converts xml appearance description into java object
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // look up how inflation/inflater works (watch video)
        View row = inflater.inflate(R.layout.single_fridgeview, parent, false);

        TextView myTitle = row.findViewById(R.id.fridgeTitle);
        TextView myDescription = row.findViewById(R.id.fridgeDescription);

        myTitle.setText(titleArray[position]);
        myDescription.setText(descriptionArray[position]);

        return row;
    }

}
