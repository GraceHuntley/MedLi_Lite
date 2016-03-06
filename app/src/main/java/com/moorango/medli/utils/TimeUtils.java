package com.moorango.medli.utils;

import android.text.format.DateFormat;

import com.moorango.medli.Application;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by cmac147 on 8/7/15.
 */
public class TimeUtils {

    public static final String TAG = "TimeUtils.java";

    private static final String TIME_24 = "24";

    public static String getFormattedDate(String input) {
        if (input == null) {
            return "";
        }
        Date date = parseTimeStamp(input);
        if (date != null)
            return DateFormat.format("EEEE, MMM d", date).toString();
        else return "";

    }

    public static String getFormattedTime(String input) {

        return DateFormat.format("h:mm a", parseTimeStamp(input)).toString();

    }

    private static Date parseTimeStamp(String input) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

            if (input.endsWith("Z")) {
                input = input.substring(0, input.length() - 1) + "GMT-00:00";
            } else {
                int inset = 6;

                String s0 = input.substring(0, input.length() - inset);
                String s1 = input.substring(input.length() - inset, input.length());

                input = s0 + "GMT" + s1;
            }

            return format.parse(input);
        } catch (Exception pe) {
            pe.printStackTrace();
            return null;
        }
    }


    public static String getJodaDateTimeAddOffset(String date) {
        String pattern = null;
        date = date.replaceAll("T", " ");
        if (date.contains(" ")) {
            if (date.length() == 25) {
                pattern = "yyyy-MM-dd HH:mm:ssZ";
            } else {
                pattern = "yyyy-MM-dd HH:mm:ss";
            }
        } else
            pattern = "yyyy-MM-dd";
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(pattern);
        DateTime dateTime = DateTime.parse(date, dateTimeFormatter);

        LogUtil.log(TAG, "test: " + dateTime.toString());
        return dateTime.toString();
    }

    public static String getAPIFormattedDate(int year, int month, int day) {
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy MM d");
        try {
            String inDateStr = year + " " + month + " " + day;
            Date inDate = outFormat.parse(inDateStr);
            return DateFormat.format("d MMMM yyyy", inDate).toString();
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return "";
    }

    public static String convertFrom24Hour(String time) {
        if (time == null) {
            return "";
        }
        SimpleDateFormat inFormat = new SimpleDateFormat("H:mm");
        try {
            Date inDate = inFormat.parse(time);
            if (Integer.valueOf(DateFormat.format("m", inDate).toString()) > 0)
                return DateFormat.format("h:mma", inDate).toString().toLowerCase().replaceAll("m", "");
            else
                return DateFormat.format("ha", inDate).toString().toLowerCase().replaceAll("m", "");
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return time;
    }

    public static String getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime()); // e.g, Monday
    }

    /**
     * This is a modified version of DateTimeUtils.getPostInterval()
     */
    public static String getPostInterval(String postDate) {
        if (postDate == null) return "";
        try {
            SimpleDateFormat sd = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
//            sd.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            DateTime dateTime;
            try {
                dateTime = new DateTime(sd.parse(postDate));
            } catch (ParseException e) {
                postDate = postDate.replace("T", " ").replace("Z", "");
                sd.applyPattern("yyyy-MM-d HH:mm:ss");
                dateTime = new DateTime(sd.parse(postDate));
            }

            Duration duration;
            if (dateTime.isAfter(System.currentTimeMillis())) {
                // Post time is ahead of current time
                return "1s ago";
            } else {
                Interval interval = new Interval(dateTime, new DateTime());
                duration = interval.toDuration();

                if (duration.getStandardSeconds() < 60)
//                return String.valueOf(duration.getStandardSeconds()) + (duration.getStandardSeconds() == 1 ? " second ago" : " seconds ago");
                    return String.valueOf(duration.getStandardSeconds()) + "s ago";
                else if (duration.getStandardMinutes() < 60)
//                return String.valueOf(duration.getStandardMinutes()) + (duration.getStandardMinutes() == 1 ? " minute ago" : " minutes ago");
                    return String.valueOf(duration.getStandardMinutes()) + "m ago";
                else if (duration.getStandardHours() < 24)
//                return String.valueOf(duration.getStandardHours()) + (duration.getStandardHours() == 1 ? " hour ago" : " hours ago");
                    return String.valueOf(duration.getStandardHours()) + "h ago";
                else if (duration.getStandardDays() < 365)
//                return String.valueOf(duration.getStandardDays()) + (duration.getStandardDays() == 1 ? " day ago" : " days ago");
                    return String.valueOf(duration.getStandardDays()) + "d ago";
                else
                    return "";
            }

        } catch (ParseException pse) {
            pse.printStackTrace();
            return "";
        }
    }

    public static String getFormattedDate(Calendar cal) {
//        Calendar calendar = Calendar.getInstance();
//        Date date = calendar.getTime();
        Date date = cal.getTime();
        return new SimpleDateFormat("d MMMM y", Locale.ENGLISH).format(date.getTime());
    }

    /**
     * Return true if To date is later than From date.
     */
    public static boolean toTimeIsLater(String eventDate, String from, String to) {
        // 1 January 2016
        SimpleDateFormat sd = new SimpleDateFormat("d LLLL yyyy h:mm a", Locale.US);
        sd.setTimeZone(TimeZone.getDefault());
        DateTime fromTime, toTime;
        try {
            fromTime = new DateTime(sd.parse(eventDate + " " + from));
            toTime = new DateTime(sd.parse(eventDate + " " + to));
        } catch (ParseException e) {
            return false;
        }

        Duration duration;
        if (fromTime.isBefore(toTime.getMillis())) {
            return true;
        } else
            return false;
    }

    /**
     * Return true if the date is later than the current time.
     */
    public static boolean isAfterNow(String eventDate, String to) {
        // 1 January 2016
//        SimpleDateFormat sd = new SimpleDateFormat("d LLLL yyyy h:mm a", Locale.US);
        SimpleDateFormat sd = new SimpleDateFormat("d LLLL yyyy h:mm a");
        sd.setTimeZone(TimeZone.getDefault());
        try {
            DateTime toTime = new DateTime(sd.parse(eventDate + " " + to));
            return toTime.isAfterNow();
        } catch (ParseException e) {
            return false;
        }
    }


    /**
     * http://www.java2s.com/Code/Java/Data-Type/Checksifacalendardateistoday.htm
     */

    /**
     * <p>Checks if a date is today.</p>
     *
     * @param date the date, not altered, not null.
     * @return true if the date is today.
     * @throws IllegalArgumentException if the date is <code>null</code>
     */
    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    /**
     * <p>Checks if a date is tomorrow.</p>
     */
    public static boolean isTomorrow(Date date) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        return isSameDay(date, tomorrow.getTime());
    }

    /**
     * <p>Checks if two dates are on the same day ignoring time.</p>
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either date is <code>null</code>
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    /**
     * <p>Checks if two calendars represent the same day ignoring time.</p>
     *
     * @param cal1 the first calendar, not altered, not null
     * @param cal2 the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * Get timezone in tzdata (tz database) format.
     * https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     */
    public static String getTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getID();
    }

    /***
     * @param cal Calendar that holds the given time
     * @return string format of the given time with the system time format
     */
    public static String getTimeString(Calendar cal) {
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(Application.getContext());
        return timeFormat.format(cal.getTime());
    }

    /**
     * @param cal
     * @param force12hformat if true, force 12-hour format
     * @return
     */
    public static String getTimeString(Calendar cal, boolean force12hformat) {
        if (force12hformat) {
            String pattern = "hh:mm aaa";
            return (String) DateFormat.format(pattern, cal);
        } else {
            return getTimeString(cal);
        }

    }

    /***
     * @param hours   hours in 24h format
     * @param minutes
     * @return string format of the given time with the system time format
     */
    public static String getTimeString(int hours, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(2015, 20, 7, hours, minutes);
        return getTimeString(cal);
        /*DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(Application.getContext());
        return timeFormat.format(cal.getTime());*/
    }

    /***
     * @param hours          hours in 24h format
     * @param minutes
     * @param force12hformat if true, force 12-hour format
     * @return string format of the given time with the system time format or 12h format
     */
    public static String getTimeString(int hours, int minutes, boolean force12hformat) {
        Calendar cal = Calendar.getInstance();
        cal.set(2015, 20, 7, hours, minutes);

        if (force12hformat) {
            String pattern = "hh:mm aaa";
            return (String) DateFormat.format(pattern, cal);
        } else {
            return getTimeString(cal);
        }
    }

    /**
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     * @return the string with the system short date format
     */
    public static String getShortDateString(int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        return getShortDateString(cal);
        /*DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(Application.getContext());
        return dateFormat.format(cal.getTime());*/
    }

    /**
     * @param cal
     * @return the string with the system short date format
     */
    public static String getShortDateString(Calendar cal) {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(Application.getContext());
        return dateFormat.format(cal.getTime());
    }

    /**
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     * @return the string with the system long date format
     */
    public static String getLongDateString(int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        return getLongDateString(cal);
        /*DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(Application.getContext());
        return dateFormat.format(cal.getTime());*/
    }

    /**
     * @param cal
     * @return the string with the system long date format
     */
    public static String getLongDateString(Calendar cal) {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(Application.getContext());
        return dateFormat.format(cal.getTime());
    }

    /**
     * @return true if the 24h format is used by the system
     */
    public static boolean is24H() {
        String timeFormat24h = android.provider.Settings.System.TIME_12_24;
        return timeFormat24h.equals(TIME_24);
    }
}
