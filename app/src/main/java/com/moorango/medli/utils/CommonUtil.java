package com.moorango.medli.utils;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

import com.moorango.medli.Application;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {

    public static final String TAG = "CommonUtil";
    private static Toast styledToast;

    public static Point deviceDimensions() {
        WindowManager manager =
                (WindowManager) Application.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static float getDensity() {
        return Application.getContext().getResources().getDisplayMetrics().density;
    }

    public static boolean checkNetworkState() {
        ConnectivityManager conMgr = (ConnectivityManager) Application.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            if (conMgr != null) {
                if ((conMgr.getNetworkInfo(0) != null && conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED)
                        || (conMgr.getNetworkInfo(1) != null && conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED)) {
                    //LogUtil.log("CommonUtils", "connected");
                    return true;

                } else if ((conMgr.getNetworkInfo(0) != null && conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED)
                        || (conMgr.getNetworkInfo(1) != null && conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED)) {

                    return false;
                }
            }
        } catch (NoSuchFieldError nfE) {
            nfE.printStackTrace();
            return false;
        }
        return false;
    }

    public static void checkNetworkStateAndWait(final Context context) {
        if (context != null) {
            if (!checkNetworkState()) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        if (Application.getActivityContext() != null) {
                            //new NetworkDisconnectModal(Application.getActivityContext()).show();
                        }

                    }
                });
                while (!checkNetworkState()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException iee) {
                        iee.printStackTrace();
                    }
                }
            }
        } else {
            LogUtil.log("NOX", "DIDNT WORK");
        }
    }

    public static void styledToast(Context context, String message) {
        styledToast(context, message, Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL);
    }

    public static void styledToast(Context context, String message, int toastLength, int gravity) {
        if (context != null && message != null) {
            if (styledToast != null) styledToast.cancel();
            styledToast = Toast.makeText(context, message, toastLength);
            styledToast.setGravity(gravity, 0, 0);
            styledToast.show();
        }
    }

    public static void cancelToast() {
        if (styledToast != null) styledToast.cancel();
    }

    public static String parseForURL(String toParse) {

        if (toParse.contains("_")) {
            return toParse.replace("_", "/");
        } else {
            return toParse;
        }
    }

    public static String parseForKey(String toParse) {

        if (toParse.contains("/")) {
            return toParse.replace("/", "_");
        } else {
            return toParse;
        }
    }

    public static String styleDate(String date) {

        if (date != null) {

            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMM yyyy");

            Date localeDate = dateParser.parse(date, new ParsePosition(0));
            StringBuffer localeReleaseDate = new StringBuffer();
            dateFormatter.format(localeDate, localeReleaseDate, new FieldPosition(0));

            return localeReleaseDate.toString();
        }
        return "";
    }


    public static String getCacheTag(String url) {
        String[] split = url.split("/");
        StringBuilder builder = new StringBuilder();

        for (int i = 5; i < split.length; i++) {

            builder.append(split[i]);
        }
        return String.valueOf(builder.toString().replaceAll("[?=&]", "").toLowerCase().hashCode());
    }


    public static String changeToSecureUrl(String url) {
        if (null != url) {
            url = url.replace("http", "https");
        }
        return url;
    }

    public static String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        int osVersion = Build.VERSION.SDK_INT;
        return "Android-" + manufacturer + ":" + osVersion;
    }
}
