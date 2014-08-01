/*
 * Copyright 2011 Colin Maccannell
 * SeizureView is distributed under the terms of the GNU General Public License
 * Please see included LISCENSE file for details on the distribution/modification terms
 */

package com.moorango.medli;

import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MakeDateTimeHelper {

    private int month;
    private Calendar cal;
    private SimpleDateFormat sdFormat;

    int getMonth() {
        return this.month;
    }

    void setMonth(int newMonth) {
        this.month = newMonth;
    }

    public String getTime24() {
        cal = Calendar.getInstance();
        sdFormat = getSDFormat("HH:mm:ss");

        return sdFormat.format(cal.getTime());
    }

    public String getDate() {
        cal = Calendar.getInstance();
        sdFormat = getSDFormat("yyyy-MM-dd");

        return sdFormat.format(cal.getTime());
    }

    public String getAdjustedDate(int days) {
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        sdFormat = getSDFormat("yyyy-MM-dd");

        return sdFormat.format(cal.getTime());
    }

    public boolean isLate(String time) {

        int toCompare = Integer.valueOf(convertToTime24(time).split(":")[0]);
        Time now = new Time();

        now.setToNow();

        return toCompare < now.hour;
    }

    SimpleDateFormat getSDFormat(String param) {
        return new SimpleDateFormat(param);
    }

    public String convertToTime12(String time) {
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
        String amPM = time.replaceAll("[^a-zA-Z]", "");
        String cleanTime = time.replaceAll("[^0-9:]", "");
        String readyTime;

        if (amPM.equals("AM")) {
            readyTime = cleanTime + ":00";
        } else {
            readyTime = "" + (Integer.valueOf(cleanTime.split(":")[0]) + 12) + ":" + cleanTime.split(":")[1] + ":" + "00";
        }

        return readyTime;
    }

    String getSyncStart() {

        String rawDate = getDate();
        String[] splitDate = rawDate.split("-");
        int day = Integer.parseInt(splitDate[2]);
        setMonth(Integer.parseInt(splitDate[1]));
        int newDay;

        if (day < 15) {
            setMonth(getMonth() - 1);
            newDay = 30 - (15 - day);
        } else {
            newDay = day - 15;
            if (newDay == 0) {
                newDay++;
            }
        }

        return splitDate[0] + "-" + getMonth() + "-" + newDay + " 00:00:00";
    }

    /**
     * Work in progress Algorithm.
     * Calculate when the next medication is due, based on factors.
     * -> Factor 1) New medication - if new (ie. never been entered). Next due time will be calculated
     * to be as close to a normal time within the dosing period as possible.
     * -> Factor 2) Next dose time is late if the current hour is greater than the next due time.
     * -> Factor 3) Dose would be marked as missed if the next due time runs into the next day.
     * -> Factor 4) Change of time zones.
     * -> Factor 5) If late entry give option to adjust next reminder.
     * ->
     */

    public boolean checkTimeForValidity(int frequency, String lastDose) {
        //TODO

        int lastDoseHour = Integer.valueOf(lastDose.split(":")[0]);

        return lastDoseHour + frequency <= 24;

    }

    /**
     * Currently used by DBManager.java makeButtonsIfDue
     * will work on deprecating.
     *
     * @param freq
     * @param time
     * @return
     */
    public String readyTime(int freq, String time) {

        String[] splitTime = time.split(":");
        int hours = Integer.parseInt(splitTime[0]) + freq;

        if (hours > 24) {
            hours = hours - 24;
        }
        String ready_to_convert = "" + hours + ":" + splitTime[1] + ":"
                + splitTime[2];


        return this.convertToTime12(ready_to_convert);
    }
}
