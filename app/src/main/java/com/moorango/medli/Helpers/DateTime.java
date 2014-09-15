/*
 * Copyright 2011 Colin Maccannell
 * SeizureView is distributed under the terms of the GNU General Public License
 * Please see included LISCENSE file for details on the distribution/modification terms
 */

package com.moorango.medli.Helpers;

import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
    public static long getUTCTimeMillis(String dateTime) {


        String dateSplit[] = dateTime.split("[-: ]");
        int values[] = new int[dateSplit.length];

        for (int index = 0; index < dateSplit.length; index++) {
            values[index] = Integer.valueOf(dateSplit[index]);
        }

        int year = values[0];
        int month = values[1];
        int day = values[2];
        int hour = values[3];
        int minute = values[4];
        int second = values[5];

        org.joda.time.DateTime dt = new org.joda.time.DateTime(year, month, day, hour, minute, second);

        return dt.getMillis();
    }

    /**
     * From date string (SQL compatable time stamp) returns date only in presentable format.
     * ie. "Feb 3rd 2014" from "2014-02-03 01:00:23"
     *
     * @param date sqLite compatable timestamp structure. "2014-02-03 01:00:23" format.
     * @return fully formated String with date only.
     * @throws ParseException
     */
    public static String getReadableDate(String date) {

        Time incoming = new Time();
        incoming.parse3339(date);

        if (DateUtils.isToday(incoming.toMillis(true))) {
            return "Today";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {

                String dateSplit[] = sdf.parse(date.split(" ")[0]).toString().split(" ");
                return dateSplit[0] + " " + dateSplit[1] + " " + dateSplit[2];
            } catch (ParseException p) {
                return "Parse Error";
            }
        }

    }

    private static SimpleDateFormat getSDFormat(String param) {
        return new SimpleDateFormat(param);
    }

    public static String convertToTime12(String time) {
        String[] splitTime = time.split(":");
        int hours = Integer.parseInt(splitTime[0]);
        boolean nextDay = false;

        if (hours > 24) { // then it is tommorow.
            hours = hours - 24;
            nextDay = true;
        }
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

        return hours + ":" + minutes + " " + amPm;
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
     * returns proper sql timestamp. if justDate is true uses provided time with current date for timestamp.
     * else returns current timestamp and date.
     *
     * @param justDate boolean true if time will be provided for timestamp.
     * @param time     12 hour time will be injected into timestmap.
     * @return
     */
    public static String getCurrentTimestamp(boolean justDate, String time) {

        Calendar cal = Calendar.getInstance();

        Date now = cal.getTime();
        Timestamp ts = new Timestamp(now.getTime());


        String tsNoMill = ts.toString().split("\\.")[0];

        return justDate ? tsNoMill.split(" ")[0] + " " + convertToTime24(time) : tsNoMill;
    }

    public static String getNextDayTimestamp(String time) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date now = cal.getTime();
        Timestamp ts = new Timestamp(now.getTime());

        String tsNoMill = ts.toString().split("\\.")[0];

        return tsNoMill.split(" ")[0] + " " + DateTime.convertToTime24(time);
    }

}
