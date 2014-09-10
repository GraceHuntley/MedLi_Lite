package com.moorango.medli.Helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.moorango.medli.NotifyService;

import java.text.ParseException;

/**
 * Created by Colin on 9/8/2014.
 * Copyright 2014
 */
public class AlarmHelpers {

    private final String TAG = "AlarmHelpers";

    Context context;

    public AlarmHelpers(Context context) {
        this.context = context;
    }

    public void setAlarm(String time, String name, int uniqueID) {
        Intent intent = new Intent(context, NotifyService.class);
        intent.putExtra(NotifyService.INTENT_NOTIFY, true);
        intent.putExtra(NotifyService.MEDICATION_NAME, name);

        PendingIntent pendingIntent = PendingIntent.getService(context, uniqueID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        try {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                alarm.setWindow(AlarmManager.RTC_WAKEUP, DateTime.getUTCTimeMillis(time), 60 * 1000, pendingIntent);
            } else {
                alarm.set(AlarmManager.RTC_WAKEUP, DateTime.getUTCTimeMillis(time), pendingIntent);
            }
        } catch (ParseException pe) {
            Log.e(TAG, pe.toString());
        }
    }

    public void clearAlarm(String name, int uniqueID) {

        Intent intent = new Intent(context, NotifyService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, uniqueID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);

    }
}
