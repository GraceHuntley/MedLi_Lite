package com.moorango.medli;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Colin on 8/5/2014.
 * Copyright 2014
 */
public class MedLog {

    private static List<MedLog> medLogList = new ArrayList<MedLog>();

    private String name;
    private String timestamp;
    private String dose;

    private boolean isLate;
    private boolean wasMissed;
    private boolean wasManual;

    private String uniqueID;

    public MedLog(String uniqueID, String name, String dose, String timestamp, boolean isLate, boolean wasMissed, boolean wasManual) {
        this.uniqueID = uniqueID;
        this.name = name;
        this.timestamp = timestamp;
        this.dose = dose;
        this.isLate = isLate;
        this.wasMissed = wasMissed;
        this.wasManual = wasManual;

        medLogList.add(this);

    }

    /**
     * Delete object from List.
     *
     * @param id
     */
    public void deleteObject(String id) {
        List<MedLog> tempList = new ArrayList<MedLog>();
        for (MedLog log : this.medLogList) {
            if (!log.getUniqueID().equals(id)) {
                tempList.add(log);
            }
        }
        this.medLogList = tempList;
    }

    public String toString() {
        MakeDateTimeHelper dt = new MakeDateTimeHelper();
        String test = isLate ? "LATE" : "";
        return dt.convertToTime12(this.getTimestamp().split(" ")[1]) + " " + this.getName().toUpperCase() + "\n" + test;

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

}
