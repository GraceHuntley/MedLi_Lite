package com.moorango.medli.Models;

import com.moorango.medli.Helpers.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Colin on 8/5/2014.
 * Copyright 2014
 */
public class MedLog {

    /**
     * Status Constants
     */

    public static int DELETED = 0;
    public static int ACTIVE = 1;
    public static int DISCONTINUED = 2;
    public static int MISSED = 3;
    public static int SKIPPED = 4;
    public static int SPACE_FILLER = 5;

    public static int EARLY = 0;
    public static int ON_TIME = 1;
    public static int LATE = 2;

    public static String TIME_FRAME_TEXT[] = {"EARLY", "ON-TIME", "LATE"};


    private String name;
    private String timestamp;
    private String dose;
    private static String lastDate;
    private String uniqueID;

    private String dueTime;

    private int timeFrame;
    private boolean wasMissed;
    private boolean wasManual;
    private boolean isSubHeading;

    public boolean isWasManual() {
        return wasManual;
    }

    public MedLog() {
        // empty constructor.
    }

    public MedLog(String uniqueID, String name, String dose, String timestamp, int timeFrame, boolean wasMissed, boolean wasManual, String dueTime) {
        this.uniqueID = uniqueID;
        this.name = name;
        this.timestamp = timestamp;
        this.dose = dose;
        this.timeFrame = timeFrame;
        this.wasMissed = wasMissed;
        this.wasManual = wasManual;
        this.dueTime = dueTime;

        if (lastDate == null) {
            lastDate = this.getTimestamp().split(" ")[0];
        }

    }

    public String toString() {

        Date date;
        if (this.isSubHeading()) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


            try {
                date = sdf.parse(this.getTimestamp().split(" ")[0]);
            } catch (ParseException p) {
                return "ERROR";
            }
            String dateSplit[] = date.toString().split(" ");
            return dateSplit[0] + " " + dateSplit[1] + " " + dateSplit[2];
            //return this.getTimestamp().split(" ")[0];

        } else {
            String wasMedLate = TIME_FRAME_TEXT[timeFrame];
            lastDate = this.getTimestamp().split(" ")[0];
            return DateTime.convertToTime12(this.getTimestamp().split(" ")[1]) + " " + this.getName().toUpperCase() + wasMedLate;

        }
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String timeFrame() {
        return TIME_FRAME_TEXT[timeFrame];
    }

    public boolean isWasMissed() {
        return wasMissed;
    }

    public boolean isSubHeading() {
        return isSubHeading;
    }

    public void setSubHeading(boolean isSubHeading) {
        this.isSubHeading = isSubHeading;
    }

    public String getDateOnly() {
        return this.getTimestamp().split(" ")[0];
    }

    public String getTimeOnly() {
        return this.getTimestamp().split(" ")[1];
    }

}
