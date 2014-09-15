package com.moorango.medli.Helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.moorango.medli.NotifyService;

/**
 * Created by Colin on 9/8/2014.
 * Copyright 2014
 */
public class AlarmHelpers {

    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "AlarmHelpers";

    final Context context;

    public AlarmHelpers(Context context) {
        this.context = context;
    }

    public void setAlarm(String rawTime, String name, int uniqueID) {

        long time = DateTime.getUTCTimeMillis(rawTime);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int earlyAlarm = Integer.valueOf(prefs.getString("early_alarm_preference", "0"));
        if (earlyAlarm > 0) {
            time = time - (60 * 1000 * earlyAlarm);
        }

        if (DateTime.getNowInMillisec() < time) {
            Intent intent = new Intent(context, NotifyService.class);
            intent.putExtra(NotifyService.INTENT_NOTIFY, true);
            intent.putExtra(NotifyService.MEDICATION_NAME, name);
            intent.putExtra(NotifyService.EARLY_ALARM, earlyAlarm);


            PendingIntent pendingIntent = PendingIntent.getService(context, uniqueID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                alarm.setWindow(AlarmManager.RTC_WAKEUP, time, 60 * 1000, pendingIntent);
            } else {
                alarm.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        }

    }

    public void clearAlarm(int uniqueID) {

        Intent intent = new Intent(context, NotifyService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, uniqueID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);

    }
}
