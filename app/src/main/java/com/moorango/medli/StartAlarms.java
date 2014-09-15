package com.moorango.medli;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.moorango.medli.Data.MedLiDataSource;

public class StartAlarms extends BroadcastReceiver {
    public StartAlarms() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            MedLiDataSource dataSource = MedLiDataSource.getHelper(context);
            dataSource.getAllMeds(context, true);
        }

    }
}
