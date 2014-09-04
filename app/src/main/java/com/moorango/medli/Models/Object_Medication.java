package com.moorango.medli.Models;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 */
public class Object_Medication {

    private final String TAG = "Medication";
    private String medName;
    private float doseMeasure; // numeric dose measure.
    private String doseMeasureType; // ex. ml tsp mg
    private String adminType; // prn or routine
    private String status; // active, deleted, discontinued.
    private String startDate; // date medication first started.
    private String doseForm; // the form of the dose user entered.
    private int doseCount; // max doses per day of medication.
    private String fillDate; // date medication last filled.
    private String doseTimes; // times of day to give dose. only applicable to routine meds.
    private boolean isSubHeading = false;
    private boolean isForEditDisplay;
    private String nextDue; // next due time.
    private int actualDoseCount; // actual count of doses given today.
    private int doseFrequency; // prn only frequency meds can be taken in hours.

    public boolean isSubHeading() {
        return isSubHeading;
    }

    public void setSubHeading(boolean isSubHeading) {
        this.isSubHeading = isSubHeading;
    }

    public void setDoseForm(String doseForm) {
        this.doseForm = doseForm;
    }

    public String getDoseForm() {
        return this.doseForm;
    }

    boolean isForEditDisplay() {
        return isForEditDisplay;
    }

    public void setForEditDisplay(boolean isForEditDisplay) {
        this.isForEditDisplay = isForEditDisplay;
    }

    public String getMedName() {
        return medName;
    }

    public void setMedName(String medName) {
        this.medName = medName;
    }

    public float getDoseMeasure() {
        return doseMeasure;
    }

    public void setDoseMeasure(float doseMeasure) {
        this.doseMeasure = doseMeasure;
    }

    public String getDoseMeasureType() {
        return doseMeasureType;
    }

    public void setDoseMeasureType(String doseMeasureType) {
        this.doseMeasureType = doseMeasureType;
    }

    public String getAdminType() {
        return adminType;
    }

    public void setAdminType(String adminType) {
        this.adminType = adminType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getFillDate() {
        return fillDate;
    }

    public void setFillDate(String fillDate) {
        this.fillDate = fillDate;
    }

    public String getDoseTimes() {
        return doseTimes;
    }

    public void setDoseTimes(String doseTimes) {
        this.doseTimes = doseTimes;
    }

    public int getDoseCount() {
        return doseCount;
    }

    public void setDoseCount(int doseCount) {
        this.doseCount = doseCount;
    }

    public int getDoseFrequency() {
        return doseFrequency;
    }

    public void setDoseFrequency(int doseFrequency) {
        this.doseFrequency = doseFrequency;
    }

    public String getNextDue() {
        return nextDue;
    }

    public void setNextDue(String nextDue) {
        this.nextDue = nextDue;
    }

    public int getActualDoseCount() {
        return actualDoseCount;
    }

    public void setActualDoseCount(int actualDoseCount) {
        this.actualDoseCount = actualDoseCount;
    }

    @Override
    public String toString() {

        if (this.isSubHeading()) {
            return this.getMedName();
        } else {
            return this.getMedName().toUpperCase() + " " + this.getDoseMeasure() + this.getDoseMeasureType() + "\n" +
                    (!this.isForEditDisplay() ? "NEXT DUE: " + this.getNextDue() : "");
        }
    }

    public int compareNextDue(Object_Medication med) {

        if (this.getNextDue().equals("COMPLETE")) {
            return 1;

        } else if (med.getNextDue().equals("COMPLETE")) {
            return -1;
        } else if (this.getNextDue().equals("MAXED DOSES!")) {
            return 1;

        } else if (med.getNextDue().equals("MAXED DOSES!")) {
            return -1;
        } else if (this.getNextDue().equals("PRN")) {
            return 1;
        } else if (med.getNextDue().equals("PRN")) {
            return -1;
        } else {
            SimpleDateFormat df1 = new SimpleDateFormat("hh:mm aa");
            Date date1 = null;
            Date date2 = null;
            try {
                date1 = df1.parse(this.getNextDue());
                date2 = df1.parse(med.getNextDue());
            } catch (ParseException e) {
                Log.d("Medication", e.toString());
            }

            try {
                if (date1.getTime() < date2.getTime()) {
                    return -1;
                } else if (date1.getTime() > date2.getTime()) {
                    return 1;
                } else {
                    return 0;
                }
            } catch (NullPointerException e) {
                Log.d(TAG, e.toString());
            }
        }
        return 0;
    }

}
