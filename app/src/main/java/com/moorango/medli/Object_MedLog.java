package com.moorango.medli;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Colin on 8/5/2014.
 * Copyright 2014
 */
public class Object_MedLog {

    private static List<Object_MedLog> medLogList = new ArrayList<Object_MedLog>();

    private String name;
    private String timestamp;
    private String dose;

    private boolean isLate;
    private boolean wasMissed;
    private boolean wasManual;

    private boolean isSubHeading;

    private static String lastDate;

    private String uniqueID;

    public Object_MedLog() {
        // empty constructor.
    }

    public Object_MedLog(String uniqueID, String name, String dose, String timestamp, boolean isLate, boolean wasMissed, boolean wasManual) {
        this.uniqueID = uniqueID;
        this.name = name;
        this.timestamp = timestamp;
        this.dose = dose;
        this.isLate = isLate;
        this.wasMissed = wasMissed;
        this.wasManual = wasManual;

        if (lastDate == null) {
            lastDate = this.getTimestamp().split(" ")[0];
        }


        medLogList.add(this);

    }

    /**
     * Delete object from List.
     *
     * @param id
     */
    public void deleteObject(String id) {
        List<Object_MedLog> tempList = new ArrayList<Object_MedLog>();
        for (Object_MedLog log : this.medLogList) {
            if (!log.getUniqueID().equals(id)) {
                tempList.add(log);
            }
        }
        this.medLogList = tempList;
    }

    public String toString() {
        Helper_DateTime dt = new Helper_DateTime();

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
            String wasMedLate = isLate ? "\nLATE" : "";
            lastDate = this.getTimestamp().split(" ")[0];
            return dt.convertToTime12(this.getTimestamp().split(" ")[1]) + " " + this.getName().toUpperCase() + wasMedLate;

        }
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
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

    public boolean isLate() {
        return isLate;
    }

    public void setLate(boolean isLate) {
        this.isLate = isLate;
    }

    public boolean isWasMissed() {
        return wasMissed;
    }

    public void setWasMissed(boolean wasMissed) {
        this.wasMissed = wasMissed;
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
