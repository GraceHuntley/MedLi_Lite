package com.moorango.medli;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Colin on 9/8/2014.
 * Copyright 2014
 */
@SuppressLint("Registered")
public class DataPullerIntent extends IntentService {

    public DataPullerIntent() {
        super("DataPullerIntent");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

    }
}
