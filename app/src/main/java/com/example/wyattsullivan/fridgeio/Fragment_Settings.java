package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 1/30/18.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.Calendar;

public class Fragment_Settings extends Fragment {

    Switch enableNotifications;
    TimePicker timePicker;
    RadioGroup radioGroup;
    RadioButton radioButton;
    DbHelper dbHelp;
    Button Save;
    String fridgeID;

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
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);

        fridgeID = getActivity().getIntent().getStringExtra("FridgeID");
        dbHelp = new DbHelper(getActivity());
        final Notification notif = dbHelp.getNotification(fridgeID);

        //Switch
        enableNotifications = view.findViewById(R.id.switch2);
        Boolean enabled = true;
        if (notif.getEnabled() == 0) {
            enabled = false;
        }
        enableNotifications.setChecked(enabled);

        //TimePicker
        timePicker = view.findViewById(R.id.timePicker2);
        //Dumb deprecated methods, use as a check to use right method
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.setHour(notif.getHour());
            timePicker.setMinute(notif.getMinute());
        } else {
            timePicker.setCurrentHour(notif.getHour());
            timePicker.setCurrentMinute(notif.getMinute());
        }

        //RadioGroup
        radioGroup = view.findViewById(R.id.radioGroup);
        switch (notif.getFrequency()) {
            case 1:
                radioGroup.check(R.id.OneDay);
                break;
            case 2:
                radioGroup.check(R.id.ThreeDays);
                break;
            case 3:
                radioGroup.check(R.id.OneWeek);
                break;
        }

        //Save Button
        Save = view.findViewById(R.id.SaveButton);
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newEnabled = 1;
                if (!enableNotifications.isChecked()) {
                    newEnabled = 0;
                }

                int newHour;
                int newMinute;
                if (Build.VERSION.SDK_INT >= 23) {
                    newHour = timePicker.getHour();
                    newMinute = timePicker.getMinute();
                } else {
                    newHour = timePicker.getCurrentHour();
                    newMinute = timePicker.getCurrentMinute();
                }

                int ID = radioGroup.getCheckedRadioButtonId();
                int newFrequency = 0;
                switch (ID) {
                    case R.id.OneDay:
                        newFrequency = 1;
                        break;
                    case R.id.ThreeDays:
                        newFrequency = 2;
                        break;
                    case R.id.OneWeek:
                        newFrequency = 3;
                        break;
                }

                dbHelp.editNotificationEnabled(fridgeID, newEnabled);
                dbHelp.editNotificationHour(fridgeID, newHour);
                dbHelp.editNotificationMinute(fridgeID, newMinute);
                dbHelp.editNotificationFrequency(fridgeID, newFrequency);

                //Create new Notification to replace old with new settings
                Intent notificationIntent = new Intent(getContext(), NotificationPublisher.class);
                notificationIntent.putExtra("FridgeID", fridgeID);

                PendingIntent PI = PendingIntent.getBroadcast(getContext(), notif.getNotifID(),
                        notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, newHour);
                cal.set(Calendar.MINUTE, newMinute);
                cal.set(Calendar.SECOND, 0);
                if (!cal.after(Calendar.getInstance())) {
                    cal.add(Calendar.DATE, 1);
                }
                long delay = cal.getTimeInMillis();

                AlarmManager almMn = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
                almMn.set(AlarmManager.RTC_WAKEUP, delay, PI);

                Toast.makeText(getContext(), "Settings Saved!", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
}
