/*
 * Copyright 2011 Colin Maccannell
 * SeizureView is distributed under the terms of the GNU General Public License
 * Please see included LISCENSE file for details on the distribution/modification terms
 */

package com.moorango.medli.Helpers;

import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

        /***
         * fixer for period on end of timestamp. TEMP FIX TODO
         */

        String cleanDateTime = dateTime.split("\\.")[0];

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        org.joda.time.DateTime due = formatter.parseDateTime(cleanDateTime);

        /*String dateSplit[] = cleanDateTime.split("[-: ]");
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

        org.joda.time.DateTime dt = new org.joda.time.DateTime(year, month, day, hour, minute, second); */

        return due.getMillis();
    }

    public static long getNowInMillisec() {
        Time time = new Time();
        time.setToNow();
        return time.toMillis(true);
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

        Log.d(TAG, tsNoMill.split(" ")[0] + " " + DateTime.convertToTime24(time));

        return tsNoMill.split(" ")[0] + " " + DateTime.convertToTime24(time);
    }

    /**
     * Helper to increment days hours or minutes of timestamp.
     * <p/>
     * UNIT-TESTED
     *
     * @param timestamp "yyyy-MM-dd HH:mm:ss"
     * @param days      days to increment
     * @param hours     hours to increment
     * @param minutes   minutes to increment
     * @return
     */
    public static String getIncrementedTimestamp(String timestamp, int days, int hours, int minutes) {

        if (timestamp.matches("^(20[1-9][1-9])-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]) (0[0-9]|1[0-9]|2[0-4]):([0-5][0-9]):([0-5][0-9])")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            org.joda.time.DateTime dt = formatter.parseDateTime(timestamp);
            if (days > 0)
                dt = dt.plusDays(days);
            if (hours > 0)
                dt = dt.plusHours(hours);
            if (minutes > 0)
                dt = dt.plusMinutes(minutes);


            Timestamp ts = new Timestamp(dt.getMillis());
            String cleaned = ts.toString().split("\\.")[0];

            return cleaned;
        } else {
            return "error";
        }
    }

    public static String getTimeDifference(String dueTime) {
        if (dueTime.matches("^(20[1-9][1-9])-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]) (0[0-9]|1[0-9]|2[0-4]):([0-5][0-9]):([0-5][0-9])")) {

            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            org.joda.time.DateTime due = formatter.parseDateTime(dueTime);
            org.joda.time.DateTime currentTime = formatter.parseDateTime(getCurrentTimestamp(false, null));

            long difference = due.toDate().getTime() - currentTime.toDate().getTime();

            int hourDifference = (int) difference / (1000 * 60 * 60);
            int minutesDifference = (int) (difference / (1000 * 60)) % 60;

            String message = "You are early by ";

            if (hourDifference > 0)
                message += hourDifference + DataCheck.getNumberVerbage(hourDifference, " hour");
            if (hourDifference > 0 && minutesDifference > 0)
                message += " and ";
            if (minutesDifference > 0)
                message += minutesDifference + DataCheck.getNumberVerbage(minutesDifference, " minute");

            return message;

        }
        return "error";

    }

}
