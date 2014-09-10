/*
 * Copyright 2011 Colin Maccannell
 * SeizureView is distributed under the terms of the GNU General Public License
 * Please see included LISCENSE file for details on the distribution/modification terms
 */

package com.moorango.medli.Helpers;


import android.text.format.Time;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class DateTime {

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
     * @param dateTime Date and time in form yyyy-mm-dd hh:mm:ss
     * @return milliseconds UTC
     * @throws ParseException
     */
    public static long getUTCTimeMillis(String dateTime) throws ParseException {

        String dateSplit[] = dateTime.split("[-: ]");
        int values[] = new int[dateSplit.length];

        for (int index = 0; index < dateSplit.length; index++) {
            values[index] = Integer.valueOf(dateSplit[index]);
        }


        int hour = values[3];
        int minute = values[4];
        int second = values[5];

        Time prepTime = new Time();
        prepTime.setToNow();
        prepTime.hour = hour;
        prepTime.minute = minute;
        prepTime.second = second;

        return prepTime.toMillis(true);
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

    public static String getFormattedDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return df.format(new Date());
    }

    public static String convertToTime24(String time) {

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

    public static long getNowInMillisec() {
        Time time = new Time();
        time.setToNow();
        return time.toMillis(true);
    }

    /**
     * @param timeToConvert
     * @param dateToConvert will be null for now until i figure out the date issues.
     * @return
     */
    public static long convert12HrToTimeMillis(String timeToConvert, String dateToConvert) {
        Time time = new Time();
        time.setToNow();

        String splitTime[] = convertToTime24(timeToConvert).split(":");
        time.hour = Integer.valueOf(splitTime[0]);
        time.minute = Integer.valueOf(splitTime[1]);
        time.second = Integer.valueOf(splitTime[2]);

        return time.toMillis(true);
    }

}
