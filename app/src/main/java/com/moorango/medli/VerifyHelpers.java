package com.moorango.medli;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Colin on 7/17/2014.
 * Copyright 2014
 */
public class VerifyHelpers {

    VerifyHelpers(Context con) {

    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getCountVerbage(int count) {
        switch (count) {
            case 1:
                return "" + count + "st";

            case 2:
                return "" + count + "nd";

            case 3:
                return "" + count + "rd";

            default:
                return "" + count + "th";

        }
    }
}
