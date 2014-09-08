package com.moorango.medli;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
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
    private String medicationName;
    // The system notification manager
    private NotificationManager mNM;

    private final String TAG = "NotifyService";

    @Override
    public void onCreate() {
        Log.i("NotifyService", "onCreate()");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        Log.d(TAG, intent.getStringExtra("med_name"));
        medicationName = intent.getStringExtra("med_name");
        // If this service was started by out AlarmTask intent then we want to show our notification
        if (intent.getBooleanExtra(INTENT_NOTIFY, false)) {
            showNotification();
            Log.d(TAG, "inonStartCommand");
        }

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
        // This is the icon to use on the notification
        int icon = R.drawable.ic_launcher;
        // This is the scrolling text of the notification

        CharSequence text = "You are due for " + medicationName;
        // What time to show on the notification
        long time = System.currentTimeMillis();

        Notification notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            notification = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentText(text)
                    .setVibrate(new long[]{100, 250, 100, 500})
                    .build();
        } else {
            notification = new Notification(icon, text, time);
            //notification.vibrate(new long[]{100, 250, 100,500});
        }


        // The PendingIntent to launch our activity if the user selects this notification
        Intent backIntent = new Intent(this, Activity_MedLi_light.class);
        backIntent.putExtra(MEDICATION_NAME, medicationName);
        backIntent.putExtra(INTENT_FROM_NOTIFICATION, true);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, backIntent, 0);


        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, title, text, contentIntent);


        // Clear the notification when it is pressed
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Send the notification to the system.
        mNM.notify(NOTIFICATION, notification);

        // Stop the service when we are finished
        stopSelf();
    }

}
