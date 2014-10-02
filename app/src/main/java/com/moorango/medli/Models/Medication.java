package com.moorango.medli.Models;

import com.moorango.medli.Helpers.DataCheck;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 */
public class Medication {

    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "Medication";

    /**
     * Medication Status constants
     */

    public static final int DELETED = 0;
    public static final int ACTIVE = 1;
    public static final int DISCONTINUED = 2;
    public static final int NEW = 3;

    private int idUnique;
    private String medName;
    private float doseMeasure; // numeric dose measure.
    private String doseMeasureType; // ex. ml tsp mg
    private String adminType; // prn or routine
    private int status; // active, deleted, discontinued.
    private String startDate; // date medication first started.
    private String doseForm; // the form of the dose user entered.
    private int doseCount; // max doses per day of medication.
    private String doseTimes; // times of day to give dose. only applicable to routine meds.
    private boolean isSubHeading = false;
    private boolean isForEditDisplay;
    private String nextDue; // next due time.
    private int actualDoseCount; // actual count of doses given today.
    private int doseFrequency; // prn only frequency meds can be taken in hours.
    private List<String> doseInfo = new ArrayList<String>();

    public void setDoseInfo(List<String> doseInfo) { this.doseInfo = doseInfo; }

    public List<String> getDoseInfo() { return this.doseInfo; }

    public void setIdUnique(int id) {
        this.idUnique = id;
    }

    public int getIdUnique() {
        return this.idUnique;
    }

    public boolean isSubHeading() {
        return isSubHeading;
    }

    public void setSubHeading() {
        this.isSubHeading = true;
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

    public void setForEditDisplay() {
        this.isForEditDisplay = true;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
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

    public int compareNextDue(Medication med) {

        if (this.getNextDue().equalsIgnoreCase("NOW")) {
            return -1;
        } else if (med.getNextDue().equalsIgnoreCase("NOW")) {
            return 1;
        } else if (!DataCheck.isToday(this.getNextDue())) {
            return 1;

        } else if (!DataCheck.isToday(med.getNextDue())) {
            return -1;

        } else {
            return this.getNextDue().compareTo(med.getNextDue());
        }
        //return 0;
    }

}
