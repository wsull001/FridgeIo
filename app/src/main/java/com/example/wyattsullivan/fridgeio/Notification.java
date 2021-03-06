package com.example.wyattsullivan.fridgeio;

/**
 * Created by samhwang on 3/8/18.
 */

public class Notification {
    private int notifID;
    private String fridgeID;
    private int enabled;
    private int hour;
    private int minute;
    private int frequency;

    public Notification(String fid, int nid, int en, int h, int m, int f) {
        notifID = nid;
        fridgeID = fid;
        enabled = en;
        hour = h;
        minute = m;
        frequency = f;
    }

    public int getNotifID() { return notifID; }

    public int getEnabled() {
        return enabled;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() { return minute; }

    public String getFridgeId() {
        return fridgeID;
    }

    public int getFrequency() { return frequency; }
}