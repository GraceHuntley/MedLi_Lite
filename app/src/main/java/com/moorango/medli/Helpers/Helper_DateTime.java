/*
 * Copyright 2011 Colin Maccannell
 * SeizureView is distributed under the terms of the GNU General Public License
 * Please see included LISCENSE file for details on the distribution/modification terms
 */

package com.moorango.medli.Helpers;


import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class Helper_DateTime {

    private static Calendar cal;
    private static SimpleDateFormat sdFormat;
    private static final String TAG = "MakeDateTimeHelper";

    public static String getTime24() {
        cal = Calendar.getInstance();
        sdFormat = getSDFormat("HH:mm:ss");

        return sdFormat.format(cal.getTime());
    }

    public static String getTime12() {
        cal = Calendar.getInstance();
        sdFormat = getSDFormat("hh:mm a");

        return sdFormat.format(cal.getTime());
    }

    public String getDate() {
        cal = Calendar.getInstance();
        sdFormat = getSDFormat("yyyy-MM-dd");

        return sdFormat.format(cal.getTime());
    }

    /**
     * From date string (SQL compatable time stamp) returns date only in presentable format.
     * ie. "Feb 3rd 2014" from "2014-02-03 01:00:23"
     *
     * @param date sqLite compatable timestamp structure. "2014-02-03 01:00:23" format.
     * @return fully formated String with date only.
     * @throws ParseException
     */
    public String getReadableDate(String date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {

            String dateSplit[] = sdf.parse(date.split(" ")[0]).toString().split(" ");
            return dateSplit[0] + " " + dateSplit[1] + " " + dateSplit[2];
        } catch (ParseException p) {
            return "Parse Error";
        }

    }

    private static SimpleDateFormat getSDFormat(String param) {
        return new SimpleDateFormat(param);
    }

    public static String convertToTime12(String time) {
        String[] splitTime = time.split(":");
        int hours = Integer.parseInt(splitTime[0]);
        String minutes = splitTime[1];
        String amPm;
        if (hours >= 12) {
            if (hours > 12) {
                hours = hours - 12;
            }

            amPm = "PM";
        } else {
            if (hours == 0) {
                hours = 12;
            }
            amPm = "AM";
        }

        return "" + hours + ":" + minutes + " " + amPm;
    }

    public String convertToTime24(String time) {

        String formattedDate = "error";
        SimpleDateFormat input = new SimpleDateFormat("hh:mm a");
        try {
            Date dt = input.parse(time);

            SimpleDateFormat output = new SimpleDateFormat("HH:mm:ss");
            formattedDate = output.format(dt);
        } catch (ParseException p) {
            Log.e(TAG, p.toString());
        }

        return formattedDate;

    }

}
