package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 1/30/18.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Fragment_Settings extends Fragment {

    public static Fragment_Settings newInstance() {
        Fragment_Settings fragment = new Fragment_Settings();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /* Custom Menu Bar Boilerplate
     *   Create custom menu xml (or use already existing)
     *   Populate code below for each button in menu
     */

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.***menu activity***, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.***button id***) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}
