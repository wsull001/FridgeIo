package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 2/16/18.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Fragment_FridgeView extends Fragment {

    private String[] keys;

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

        DbHelper dbHelp = new DbHelper(getContext());

        FridgeList fridgeList = dbHelp.getFridges();
        if(fridgeList == null) return view;
        keys = fridgeList.getIds();


        ListView list = (ListView) view.findViewById(R.id.listViewFridge);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), FragmentManagerProduct.class);
                intent.putExtra("FridgeID", Fragment_FridgeView.this.keys[position]);
                startActivity(intent);
            }
        });

        fridgeAdapter adapter = new fridgeAdapter(getActivity(), fridgeList.getNames());
        list.setAdapter(adapter);

        return view;
    }
}

class fridgeAdapter extends ArrayAdapter<String>
{
    Context context;
    String[] titleArray;
    fridgeAdapter(Context c, String[] titles)
    {
        super(c, R.layout.single_productview, R.id.textViewTitle, titles);
        this.context = c;
        this.titleArray = titles;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // layout inflater object to converts xml appearance description into java object
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // look up how inflation/inflater works (watch video)
        View row = inflater.inflate(R.layout.single_fridgeview, parent, false);

        TextView myTitle = row.findViewById(R.id.fridgeTitle);

        myTitle.setText(titleArray[position]);

        return row;
    }

}
