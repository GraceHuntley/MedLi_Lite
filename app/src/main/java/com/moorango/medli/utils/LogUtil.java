package com.moorango.medli.utils;

import android.util.Log;

import com.moorango.medli.lib.Constants;


public class LogUtil {

    public static void log(String tag, String message) {
        if (Constants.ENABLE_LOGGING && message != null) {
            if (message.length() > 4000) {
                Log.wtf(tag, message.substring(0, 4000));

                log(tag, message.substring(4000));
            } else {
                Log.wtf(tag, message);
            }
        }
    }
}