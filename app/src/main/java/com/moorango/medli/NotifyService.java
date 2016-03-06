package com.moorango.medli;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Colin on 9/8/2014.
 * Copyright 2014
 */
public class NotifyService extends Service {

    /**
     * Class for clients to access
     */
    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    // Unique id to identify the notification.
    private static final int NOTIFICATION = 123;
    // Name of an intent extra we can use to identify if this service was started to create a notification
    public static final String INTENT_NOTIFY = "com.moorango.medli.INTENT_NOTIFY";
    public static final String INTENT_FROM_NOTIFICATION = "com.moorango.medli.INTENT_FROM_NOTIFICATION";
    public static final String MEDICATION_NAME = "med_name";
    public static final String EARLY_ALARM = "early_alarm";
    private String medicationName;
    // The system notification manager
    private NotificationManager mNM;
    private int earlyDose = 0;
    private final String TAG = "NotifyService";

    @Override
    public void onCreate() {

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        medicationName = intent.getStringExtra("med_name");
        // If this service was started by out AlarmTask intent then we want to show our notification
        if (intent.getBooleanExtra(INTENT_NOTIFY, false)) {
            showNotification();

        }
        earlyDose = intent.getIntExtra(EARLY_ALARM, 0);

        // We don't care if this service is stopped as we have already delivered our notification
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients
    private final IBinder mBinder = new ServiceBinder();

    /**
     * Creates a notification and shows it in the OS drag-down status bar
     */
    private void showNotification() {
        // This is the 'title' of the notification
        CharSequence title = "Medication Reminder!!";

        CharSequence text = "You are due for Medications " + ((earlyDose > 0) ? "in: " + earlyDose + " minute(s)" : "now.");

        Notification notification;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isVibrator = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            String vs = Context.VIBRATOR_SERVICE;
            Vibrator mVibrator = (Vibrator) getSystemService(vs);

            isVibrator = mVibrator.hasVibrator();

        }

        /***
         * if user ignores alarm set new alarms in background.
         */
        Intent intent = new Intent(this, StartAlarms.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setSmallIcon(R.drawable.ic_launcher);

        if (prefs.getBoolean("sound_preference", true)) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        if (prefs.getBoolean("vibrate_preference", true) && isVibrator) {
            builder.setVibrate(new long[]{100, 250, 100, 500});
        } else if (prefs.getBoolean("vibrate_preference", true) && !isVibrator) {

            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        }
        builder.setDeleteIntent(pendingIntent);
        
        notification = builder.build(); // available from API level 11 and onwards

        // The PendingIntent to launch our activity if the user selects this notification
        Intent backIntent = new Intent(this, Activity_MedLi_light.class);
        backIntent.putExtra(MEDICATION_NAME, medicationName);
        backIntent.putExtra(INTENT_FROM_NOTIFICATION, true);

        // Clear the notification when it is pressed
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Send the notification to the system.
        mNM.notify(NOTIFICATION, notification);

        // Stop the service when we are finished
        stopSelf();
    }

}
