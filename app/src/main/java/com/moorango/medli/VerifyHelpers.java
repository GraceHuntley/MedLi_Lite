package com.moorango.medli;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Colin on 7/17/2014.
 * Copyright 2014
 */
@SuppressWarnings("WeakerAccess")
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

    public static String capitalizeTitles(String toCapitalize) {
        String splitString[] = toCapitalize.split(" ");
        String readyForReturn = "";

        for (String titleWord : splitString) {
            readyForReturn += titleWord.substring(0, 1).toUpperCase() + titleWord.substring(1, titleWord.length());
        }

        return readyForReturn;
    }

    public static boolean isDoseLate(String due) {
        // TODO test whether admin time is late.
        MakeDateTimeHelper dt = new MakeDateTimeHelper();
        final String TAG = "VerifyHelpers";
        int nextDoseHour = Integer.valueOf(dt.convertToTime24(due).split(":")[0]);
        int loggedHour = Integer.valueOf(dt.getTime24().split(":")[0]);
        Log.d(TAG, "" + loggedHour);
        int difference = loggedHour - nextDoseHour;

        if (difference > 1) {
            return true;
        } else {
            return false;
        }
    }
}
