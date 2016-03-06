package com.moorango.medli.utils;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.text.Spanned;
import android.view.Display;
import android.view.WindowManager;

import com.moorango.medli.Application;

/**
 * Created by cmac147 on 5/19/15.
 */
public class CommonUtil {
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
        return false;
    }

    public static String buildFullAddress(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value != null) {
                builder.append(value.trim());
                builder.append(", ");
            }
        }
        String fullAddr;
        if (builder.toString().length() > 2)
            fullAddr = builder.toString().substring(0, builder.toString().length() - 2);
        else
            fullAddr = builder.toString();
        return fullAddr;

    }

    public static Spanned cleanDescription(String description) {
        if ((description != null) && (description.length() > 0)) {
            description = description.replaceAll("<br>", " ");
            description = description.replaceAll("<br/>", " ");
            description = description.replaceAll("<br />", " ");
            description = description.replaceAll("<BR>", " ");
            description = description.replaceAll("<BR/>", " ");
            description = description.replaceAll("<BR />", " ");
            return Html.fromHtml(description);
        } else return Html.fromHtml("");
    }

}
