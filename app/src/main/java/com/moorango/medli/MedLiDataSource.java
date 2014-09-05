package com.moorango.medli;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.format.Time;
import android.util.Log;

import com.moorango.medli.Models.Object_MedLog;
import com.moorango.medli.Models.Object_Medication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 */
public class MedLiDataSource {

    private final String TAG = "MedLiDataSource";
    private static final Helper_DateTime dt = new Helper_DateTime();

    // Database fields
    private SQLiteDatabase database;
    private final Helper_SQLiteHelper dbHelper;
    private static MedLiDataSource instance;

    public MedLiDataSource(Context context) {
        dbHelper = new Helper_SQLiteHelper(context);
    }

    public static synchronized MedLiDataSource getHelper(Context context) {
        if (instance == null)
            instance = new MedLiDataSource(context);

        return instance;
    }

    void open() throws SQLException {
        try {
            database = dbHelper.getWritableDatabase();

        } catch (SQLiteException ex) {
            Log.e("Open database exception caught", ex.getMessage());
            database = dbHelper.getReadableDatabase();
        }
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * check if medlist has entries for main_activity.
     *
     * @return true if active entries exist, else false.
     */
    public boolean medListHasEntries() {

        this.open(); // open db.

        Cursor cursor = database.rawQuery(Constants.GET_TOTAL_MED_COUNT, null);

        if (cursor.moveToFirst())
            return (cursor.getInt(0) > 0) ? true : false;
        else
            return false;

    }

    public List<Object_Medication> getAllMeds(String tag) {
        List<Object_Medication> routineList = new ArrayList<Object_Medication>();
        List<Object_Medication> prnList = new ArrayList<Object_Medication>();
        this.open(); // open db.

        Cursor cursor = database.rawQuery(Constants.GET_MEDLIST_ROUTINE, null);

        while (cursor.moveToNext()) {
            Object_Medication medication = cursorToRoutine(cursor);
            if (medication.getAdminType().equalsIgnoreCase("routine")) {
                routineList.add(medication);
            } else {
                prnList.add(medication);
            }

        }
        Collections.sort(routineList, new Comparator<Object_Medication>() {
            @Override
            public int compare(Object_Medication lhs, Object_Medication rhs) {
                return lhs.compareNextDue(rhs);
            }
        });

        Collections.sort(prnList, new Comparator<Object_Medication>() {
            @Override
            public int compare(Object_Medication lhs, Object_Medication rhs) {
                return lhs.compareNextDue(rhs);
            }
        });

        Object_Medication routineHeader = new Object_Medication();
        routineHeader.setSubHeading(true);
        routineHeader.setMedName("Routine Medications");
        routineList.add(0, routineHeader);
        Object_Medication headerMed = new Object_Medication();
        headerMed.setSubHeading(true);
        headerMed.setMedName("Non-Routine Medications");
        routineList.add(headerMed);
        for (Object_Medication med : prnList) {
            routineList.add(med);
        }
        cursor.close();
        return routineList;
    }

    private Object_Medication cursorToRoutine(Cursor cursor) {
        final Object_Medication medication = new Object_Medication();

        medication.setMedName(cursor.getString(0));
        medication.setDoseMeasure(cursor.getFloat(1));
        medication.setDoseMeasureType(cursor.getString(2));
        medication.setDoseCount(cursor.getInt(3));
        medication.setDoseTimes(cursor.getString(4));
        medication.setActualDoseCount(cursor.getInt(5));
        medication.setAdminType(cursor.getString(6));
        medication.setStatus(cursor.getString(8));
        medication.setDoseForm(cursor.getString(9));
        medication.setIdUnique(cursor.getInt(10));

        Log.d(TAG, "DoseCount: " + medication.getDoseCount() + " actual: " + medication.getActualDoseCount());
        if (medication.getAdminType().equals("routine")) {
            medication.setNextDue(new Object() {

                String setTime() {

                    if (medication.getDoseCount() > medication.getActualDoseCount()) {
                        String split[] = medication.getDoseTimes().split(";");

                        return split[medication.getActualDoseCount()];
                    } else {
                        return "COMPLETE";
                    }
                }
            }.setTime());
        } else {

            medication.setDoseFrequency(Integer.valueOf(cursor.getString(7)));
            if (getPrnDoseCount24Hours(medication.getMedName()) >= medication.getDoseCount()) { // all doses have been taken.
                // TODO might make this show the next dose with date.
                medication.setNextDue("COMPLETE");
            } else {
                // TODO get last dose then see if it was recent.

                medication.setNextDue(getPrnNextDose(medication.getMedName(), medication.getDoseFrequency()));

            }
        }

        return medication;
    }

    private int getPrnDoseCount24Hours(String medName) {

        this.open();

        Cursor cs = database.rawQuery(Constants.GET_COUNT_LAST_24HOURS(medName), null);

        return (cs.moveToFirst()) ? cs.getInt(0) : 0;

    }

    private String getPrnNextDose(String medName, int freq) {

        String nextDose = null;

        this.open();

        Cursor cs = database.rawQuery(Constants.GET_LAST_PRN_DOSE(medName), null);
        while (cs.moveToNext()) {
            nextDose = cs.getString(0);
        }

        if (nextDose == null) {
            nextDose = "PRN";
        } else {
            int lastDoseHour = Integer.valueOf(nextDose.split(" ")[1].split(":")[0]);
            int lastDosePlusHour = lastDoseHour + freq; // just for testing purposes.

            int currentHour = Integer.valueOf(dt.getTime24().split(":")[0]);

            if ((lastDosePlusHour - currentHour) >= 0) {
                String nextDoseHour = "" + (lastDoseHour + freq);
                String minutes = "" + nextDose.split(" ")[1].split(":")[1];
                nextDose = dt.convertToTime12(nextDoseHour + ":" + minutes + ":" + "00");
            } else {
                nextDose = "PRN";
            }
        }

        return nextDose;
    }


    public List<Object_MedLog> getMedHistory(int howManyDays) {

        // TODO for now just getting todays data.
        List<Object_MedLog> loggedMeds = new ArrayList<Object_MedLog>();
        this.open();
        String lastDate = null;
        boolean makeHeader = false;
        Object_MedLog medLog = null;
        Cursor cs = database.rawQuery(Constants.GET_TODAYS_MED_ADMIN_LOGS, null);

        while (cs.moveToNext()) {

            String thisDate = cs.getString(3).split(" ")[0];
            if (lastDate == null) {
                lastDate = thisDate;
            }

            if (thisDate.equals(lastDate)) {
                medLog = new Object_MedLog(cs.getString(0), cs.getString(1), cs.getString(2), cs.getString(3), (cs.getInt(4) == 1), (cs.getInt(5) == 1), (cs.getInt(6) == 1));
                medLog.setSubHeading(false);
                makeHeader = false;
                lastDate = thisDate;
            } else if (!makeHeader) {
                medLog = new Object_MedLog();
                medLog.setSubHeading(true);
                medLog.setTimestamp(cs.getString(3));
                makeHeader = true;
                lastDate = thisDate;

                cs.moveToPrevious();

            }

            loggedMeds.add(medLog);

        }
        return loggedMeds;
    }

    public void submitNewMedication(Object_Medication medication, boolean doUpdate) {

        ContentValues cv = new ContentValues();
        cv.put("name", medication.getMedName());
        cv.put("dose_int", medication.getDoseMeasure());
        cv.put("dose_measure_type", medication.getDoseMeasureType());
        cv.put("dose_form", medication.getDoseForm());
        cv.put("admin_type", medication.getAdminType());
        if (doUpdate) {
            Time now = new Time();
            now.setToNow();

            Log.d(TAG, now.format("mm-dd-yyyy hh:mm:ss"));
            cv.put("last_modified", now.format("%Y-%m-%d %H:%M:%S").toString());
        } else {
            cv.put("status", medication.getAdminType().equalsIgnoreCase("routine") ? "new" : "active");
        }
        cv.put("dose_count", medication.getDoseCount());
        //cv.put("fillDate", medication.getFillDate()); // will add this for next roll-out
        cv.put("startDate", medication.getStartDate());

        if (medication.getAdminType().equals("routine")) {
            cv.put("dose_times", medication.getDoseTimes());
        } else {
            cv.put("dose_frequency", medication.getDoseFrequency());
        }
        this.open();
        if (doUpdate) {

            database.update("medlist", cv, "ID_UNIQUE='" + medication.getIdUnique() + "'", null);
        } else {
            database.insert("medlist", "name", cv);
        }


    }

    public int deleteMedEntry(String uniqueId) {
        ContentValues cv = new ContentValues();
        cv.put("status", "del");
        return database.update("med_logs", cv, "ID_UNIQUE='" + uniqueId + "'", null);

    }

    public int updateMedicationAdmin(Object_MedLog medLog) {
        ContentValues cv = new ContentValues();
        cv.put("timestamp", medLog.getTimestamp());
        cv.put("dose", medLog.getDose());
        cv.put("manual_entry", 1);
        cv.put("missed", 0);

        return database.update("med_logs", cv, "ID_UNIQUE='" + medLog.getUniqueID() + "'", null);
    }

    public void submitMedicationAdmin(Object_Medication medication, String manualTime) {

        ContentValues cv = new ContentValues();
        cv.put("ID_UNIQUE", medication.getMedName() + new Date().getTime());
        cv.put("name", medication.getMedName());
        cv.put("dose", medication.getDoseMeasure() + " " + medication.getDoseMeasureType());

        if (manualTime != null) { // dose time being entered manually.
            cv.put("manual_entry", 1);
            cv.put("timestamp", dt.getDate() + " " + dt.convertToTime24(manualTime));

            cv.put("late", isDoseLate(manualTime, medication.getNextDue()));
        } else { // no manual entry.
            cv.put("timestamp", dt.getDate() + " " + dt.getTime24());
            if (medication.getAdminType().equals("routine")) {
                //cv.put("late", isDoseLate(dt.getTime24(), medication.getNextDue()));
                cv.put("late", isDoseLate(dt.getTime12(), medication.getNextDue()));
            }
            cv.put("manual_entry", 0);
        }
        cv.put("status", "active");

        this.open();

        database.insert("med_logs", "name", cv);

    }

    public void submitMissedDose(Object_Medication medication, String time) {

        ContentValues cv = new ContentValues();
        cv.put("ID_UNIQUE", medication.getMedName() + new Date().getTime());
        cv.put("name", medication.getMedName());
        cv.put("dose", medication.getDoseMeasure() + " " + medication.getDoseMeasureType());
        cv.put("timestamp", dt.getDate() + " " + dt.convertToTime24(time));
        cv.put("missed", 1);
        cv.put("status", "active");

        this.open();
        database.insert("med_logs", "name", cv);

        if (medication.getStatus().equalsIgnoreCase("new")) {
            cv.clear();
            cv.put("status", "active");

            database.update("medlist", cv, "name='" + medication.getMedName() + "'", null);
        }

    }

    /**
     * this is just for filling medlogs for testing.
     *
     * @return
     */

    public void populateMedAdminTest(String manualTime) {

        ContentValues cv = new ContentValues();
        cv.put("ID_UNIQUE", new Date().getTime());
        cv.put("name", "clobazam");
        cv.put("dose", "10mg");


        cv.put("manual_entry", 1);
        cv.put("timestamp", "2014-08-" + String.format("%02d", (int) ((Math.random() * 30) + 1)) + " " + String.format("%02d", (int) ((Math.random() * 23) + 1)) + ":00:00");

        cv.put("late", false);

        this.open();
        database.insert("med_logs", "name", cv);
    }

    private int isDoseLate(String time, String due) {
        // TODO test whether admin time is late.
        int nextDoseHour = Integer.valueOf(dt.convertToTime24(due).split(":")[0]);
        int loggedHour = Integer.valueOf(dt.convertToTime24(time).split(":")[0]);

        int difference = loggedHour - nextDoseHour;

        if (difference > 1) {
            return 1;
        } else {
            return 0;
        }
    }

    public List<Object_Medication> getMedListForEditing() {
        List<Object_Medication> medList = new ArrayList<Object_Medication>();

        String tempQuery = "SELECT "
                + "name, "
                + "dose_int, "
                + "dose_measure_type, "
                + "admin_type, "
                + "dose_count, "
                + "dose_times, "
                + "dose_frequency "
                + "FROM medlist "
                + "WHERE status = 'active' "
                + "ORDER BY name ASC";

        this.open();

        Cursor cs = database.rawQuery(tempQuery, null);

        while (cs.moveToNext()) {
            Object_Medication medication = new Object_Medication();
            medication.setForEditDisplay(true);
            medication.setMedName(cs.getString(0));
            medication.setDoseMeasure(cs.getFloat(1));
            medication.setDoseMeasureType(cs.getString(2));
            medication.setAdminType(cs.getString(3));
            medication.setDoseCount(cs.getInt(4));
            medication.setDoseTimes(cs.getString(5));
            medication.setDoseFrequency(cs.getInt(6));

            medList.add(medication);

        }

        // TODO need to give notice if no meds are added yet.
        return medList;

    }

    public Object_Medication getSingleMedByName(int uniqueID) {

        this.open();

        String tempQuery = "SELECT "
                + "name, "
                + "dose_int, "
                + "dose_measure_type, "
                + "admin_type, "
                + "dose_count, "
                + "dose_times, "
                + "dose_frequency, "
                + "dose_form, "
                + "ID_UNIQUE "
                + "FROM medlist "
                + "WHERE ID_UNIQUE = '" + uniqueID + "' "
                + "AND status = 'active' OR status = 'new' "
                + "LIMIT 1";

        Object_Medication medication = new Object_Medication();
        Cursor cs = database.rawQuery(tempQuery, null);

        while (cs.moveToNext()) {

            medication.setForEditDisplay(true);
            medication.setMedName(cs.getString(0));
            medication.setDoseMeasure(cs.getFloat(1));
            medication.setDoseMeasureType(cs.getString(2));
            medication.setAdminType(cs.getString(3));
            medication.setDoseCount(cs.getInt(4));
            medication.setDoseTimes(cs.getString(5));
            medication.setDoseFrequency(cs.getInt(6));
            medication.setDoseForm(cs.getString(7));
            medication.setIdUnique(cs.getInt(8));
        }

        return medication;
    }

    public void changeMedicationStatus(String name, String newStatus) {
        this.open();

        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);

        database.update("medlist", cv, "name='" + name + "'", null);

    }

    public void addDoseMeasureTypeToDB(String name, String intMeasure, String measureType) {
        this.open();

        ContentValues cv = new ContentValues();

        cv.put("med_name", name);
        cv.put("dose_measure_int", intMeasure);
        cv.put("dose_measure_type", measureType);
        cv.put("user_added", 1);

        //if (database.query("med_dose_measures", ))
    }
}
