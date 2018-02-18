package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 1/30/18.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.view.Menu;
import android.app.Activity;

public class Fragment_Settings extends Fragment {

    public static Fragment_Settings newInstance() {
        Fragment_Settings fragment = new Fragment_Settings();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}
