package com.example.wyattsullivan.fridgeio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Michael on 2/28/2018.
 */

public class StartupService extends Service {
    DbHelper dbHelp;
    FridgeList fridgeList;
    private String[] keys;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Service should only begin on startup, not relaunch
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate(){
        //Wake Lock, keeps the cpu from sleeping, thats why we use partial wakelock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        //Get info
        dbHelp = new DbHelper(getApplicationContext());
        fridgeList = dbHelp.getFridges();
        //If there's no fridges, end
        if (fridgeList == null) {
            wakeLock.release();
            return;
        }
        keys = fridgeList.getIds();
        //Loop over each fridge and set alarm for each to go off at user set time
        for (int i = 0; i < fridgeList.getSize(); ++i) {
            Notification notif = dbHelp.getNotification(keys[i]);
            Intent notificationIntent = new Intent(this, NotificationPublisher.class);
            notificationIntent.putExtra("FridgeID", keys[i]);

            PendingIntent PI = PendingIntent.getBroadcast(this, notif.getNotifID(),
                    notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);


            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, notif.getHour());
            cal.set(Calendar.MINUTE, notif.getMinute());
            cal.set(Calendar.SECOND, 0);
            long delay = cal.getTimeInMillis();

            AlarmManager almMn = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            almMn.set(AlarmManager.RTC_WAKEUP, delay, PI);
        }
        wakeLock.release();
        stopSelf();
    }
}
