package com.example.wyattsullivan.fridgeio;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Michael on 2/28/2018.
 */

public class NotificationPublisher extends BroadcastReceiver {
    DbHelper dbHelp;
    static String fridgeID = "0";

    public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //TODO: Need a check here to see if fridge exists if we add option to remove fridges
        dbHelp = new DbHelper(context);
        fridgeID = intent.getStringExtra("FridgeID");
        com.example.wyattsullivan.fridgeio.Notification notif = dbHelp.getNotification(fridgeID);

        //Create alarm for next day
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra("FridgeID", fridgeID);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, notif.getHour());
        cal.set(Calendar.MINUTE, notif.getMinute());
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DATE,  1);
        long delay = cal.getTimeInMillis();

        PendingIntent PendIn = PendingIntent.getBroadcast(context, notif.getNotifID(),
                notificationIntent, 0);
        AlarmManager almMn = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        almMn.set(AlarmManager.RTC_WAKEUP, delay, PendIn);

        //Check if user has notifications enabled, if they don't, don't send a notification
        if (notif.getEnabled() == 0) {
            return;
        }

        Intent temp = new Intent(context, FragmentManagerProduct.class);
        temp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        temp.putExtra("FridgeID", fridgeID);

        PendingIntent PI = PendingIntent.getActivity(context, notif.getNotifID(),
                temp, PendingIntent.FLAG_CANCEL_CURRENT);
        //Get a count of how many products user has going to expire
        ArrayList<Product> prods = dbHelp.getProductsByExpDate(fridgeID);
        int count = 0;
        if (prods != null) {
            for (int i = 0; i < prods.size(); ++i) {
                Calendar exp = new GregorianCalendar();
                exp.setTime(prods.get(i).getExpDate());
                Calendar current = Calendar.getInstance();
                switch (notif.getFrequency()) {
                    case 1:
                        current.add(Calendar.DATE, 1);
                        break;
                    case 2:
                        current.add(Calendar.DATE, 3);
                        break;
                    case 3:
                        current.add(Calendar.DATE, 7);
                        break;
                }
                if (current.after(exp)) {
                    ++count;
                } else {
                    break;
                }
            }
        }

        //If there are no products going to expire soon, don't send a notification
        if (count == 0) {
            return;
        }
        FridgeList fridgeList = dbHelp.getFridges();
        String name = "DEBUG";
        String[] fridge_names = fridgeList.getNames();
        String[] keys = fridgeList.getIds();
        //Get fridge name
        for (int i = 0; i < fridgeList.getSize(); ++i) {
            if (keys[i].equals(fridgeID)){
                name = fridge_names[i];
                break;
            }
        }
        //Create and send notification
        Notification.Builder not = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_fridge)
                .setContentTitle(name + " has items that will expire soon!")
                .setContentText(Integer.toString(count) + " items will expire soon!")
                .setContentIntent(PI)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis());
        nm.notify(notif.getNotifID(), not.build());
    }
}