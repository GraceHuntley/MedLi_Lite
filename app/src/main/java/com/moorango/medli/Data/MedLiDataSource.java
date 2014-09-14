package com.moorango.medli.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.format.Time;
import android.util.Log;

import com.moorango.medli.Constants;
import com.moorango.medli.Helpers.DataCheck;
import com.moorango.medli.Helpers.DateTime;
import com.moorango.medli.Models.MedLog;
import com.moorango.medli.Models.Medication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 */
public class MedLiDataSource {

    @SuppressWarnings("UnusedAssignment")
    private final String TAG = "MedLiDataSource";
    private static final DateTime dt = new DateTime();

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

        return cursor.moveToFirst() && (cursor.getInt(0) > 0);

    }

    public List<Medication> getAllMeds(Context context, boolean makeAlarms) {
        List<Medication> routineList = new ArrayList<Medication>();
        List<Medication> prnList = new ArrayList<Medication>();
        this.open(); // open db.

        Cursor cursor = database.rawQuery(Constants.GET_MEDLIST_ROUTINE, null);

        while (cursor.moveToNext()) {
            Medication medication = cursorToRoutine(cursor, context, makeAlarms);
            if (medication.getAdminType().equalsIgnoreCase("routine")) {
                routineList.add(medication);
            } else {
                prnList.add(medication);
            }

        }
        Collections.sort(routineList, new Comparator<Medication>() {
            @Override
            public int compare(Medication lhs, Medication rhs) {
                return lhs.compareNextDue(rhs);
            }
        });

        Collections.sort(prnList, new Comparator<Medication>() {
            @Override
            public int compare(Medication lhs, Medication rhs) {
                return lhs.compareNextDue(rhs);
            }
        });

        Medication routineHeader = new Medication();
        routineHeader.setSubHeading();
        routineHeader.setMedName("Routine Medications");
        routineList.add(0, routineHeader);
        Medication headerMed = new Medication();
        headerMed.setSubHeading();
        headerMed.setMedName("Non-Routine Medications");
        routineList.add(headerMed);
        for (Medication med : prnList) {
            routineList.add(med);
        }
        cursor.close();


        return routineList;
    }

    private Medication cursorToRoutine(Cursor cursor, final Context context, final boolean makeAlarms) {
        final Medication medication = new Medication();

        medication.setMedName(cursor.getString(0));
        medication.setDoseMeasure(cursor.getFloat(1));
        medication.setDoseMeasureType(cursor.getString(2));
        medication.setDoseCount(cursor.getInt(3));
        medication.setDoseTimes(cursor.getString(4));
        medication.setActualDoseCount(cursor.getInt(5));
        medication.setAdminType(cursor.getString(6));
        medication.setStatus(cursor.getInt(8));
        medication.setDoseForm(cursor.getString(9));
        medication.setIdUnique(cursor.getInt(10));

        if (medication.getAdminType().equalsIgnoreCase("routine")) {
            medication.setNextDue(new Object() {

                String setTime() {
                    String split[] = medication.getDoseTimes().split(";");
                    if (medication.getDoseCount() > medication.getActualDoseCount()) {

                        return DateTime.getCurrentTimestamp(true, split[medication.getActualDoseCount()]);
                    } else {
                        return DateTime.getNextDayTimestamp(split[0]);
                    }
                }
            }.setTime());
        } else {

            medication.setDoseFrequency(Integer.valueOf(cursor.getString(7)));
            if (getPrnDoseCount24Hours(medication.getIdUnique()) >= medication.getDoseCount()) { // all doses have been taken.
                // TODO might make this show the next dose with date.
                medication.setNextDue("COMPLETE");
            } else {
                // TODO get last dose then see if it was recent.
                String nextDose = getPrnNextDose(medication.getIdUnique(), medication.getDoseFrequency());

                medication.setNextDue(nextDose);
            }
        }

        return medication;
    }

    private int getPrnDoseCount24Hours(int fk) {

        this.open();

        Cursor cs = database.rawQuery(Constants.GET_COUNT_LAST_24HOURS(fk), null);

        return (cs.moveToFirst()) ? cs.getInt(0) : 0;

    }

    private String getPrnNextDose(int fk, int freq) {

        String nextDose = null;

        this.open();

        Cursor cs = database.rawQuery(Constants.GET_LAST_PRN_DOSE(fk), null);
        while (cs.moveToNext()) {
            nextDose = cs.getString(0);
        }

        if (nextDose == null) {
            nextDose = "PRN";
        } else {
            int lastDoseHour = Integer.valueOf(nextDose.split(" ")[1].split(":")[0]);
            int lastDosePlusHour = lastDoseHour + freq; // just for testing purposes.

            int currentHour = Integer.valueOf(DateTime.getTime24().split(":")[0]);

            if ((lastDosePlusHour - currentHour) >= 0) {
                String nextDoseHour = "" + (lastDoseHour + freq);
                String minutes = "" + nextDose.split(" ")[1].split(":")[1];

                nextDose = DateTime.getCurrentTimestamp(true, DateTime.convertToTime12(nextDoseHour + ":" + minutes + ":" + "00"));
            } else {
                nextDose = "PRN";
            }
        }

        return nextDose;
    }


    public List<MedLog> getMedHistory() {

        // TODO for now just getting todays data.
        List<MedLog> loggedMeds = new ArrayList<MedLog>();
        this.open();
        String lastDate = null;
        boolean makeHeader = false;
        MedLog medLog = null;
        Cursor cs = database.rawQuery(Constants.GET_TODAYS_MED_ADMIN_LOGS, null);

        while (cs.moveToNext()) {

            String thisDate = cs.getString(3).split(" ")[0];
            if (lastDate == null) {
                lastDate = thisDate;
            }

            if (thisDate.equalsIgnoreCase(lastDate)) {
                medLog = new MedLog(cs.getString(0), cs.getString(1), cs.getString(2), cs.getString(3), cs.getInt(4), (cs.getInt(5) == 1), (cs.getInt(6) == 1), cs.getString(8), cs.getInt(9));
                medLog.setSubHeading(false);
                makeHeader = false;
                lastDate = thisDate;
            } else if (!makeHeader) {
                medLog = new MedLog();
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

    public void submitNewMedication(Medication medication, boolean doUpdate) {

        ContentValues cv = new ContentValues();

        cv.put("name", medication.getMedName());
        cv.put("dose_int", medication.getDoseMeasure());
        cv.put("dose_measure_type", medication.getDoseMeasureType());
        cv.put("dose_form", medication.getDoseForm());
        cv.put("admin_type", medication.getAdminType());
        if (doUpdate) {
            Time now = new Time();
            now.setToNow();

            cv.put("last_modified", now.format("%Y-%m-%d %H:%M:%S"));
        } else {
            cv.put("status", medication.getAdminType().equalsIgnoreCase("routine") ? Medication.NEW : Medication.ACTIVE);
            cv.put("ID_UNIQUE", DataCheck.createUniqueID(medication.getMedName()));
        }
        cv.put("dose_count", medication.getDoseCount());
        //cv.put("fillDate", medication.getFillDate()); // will add this for next roll-out
        cv.put("startDate", medication.getStartDate());

        if (medication.getAdminType().equalsIgnoreCase("routine")) {
            cv.put("dose_times", medication.getDoseTimes());
        } else {
            cv.put("dose_frequency", medication.getDoseFrequency());
        }
        this.open();
        if (doUpdate) {

            database.update("medlist", cv, "ID_UNIQUE='" + medication.getIdUnique() + "'", null);
        } else {

            database.insert("medlist", "ID_UNIQUE", cv);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public int deleteMedEntry(String uniqueId) {
        ContentValues cv = new ContentValues();
        cv.put("status", MedLog.DELETED);
        return database.update("med_logs", cv, "ID_UNIQUE='" + uniqueId + "'", null);

    }

    public int updateMedicationAdmin(MedLog medLog) {
        ContentValues cv = new ContentValues();
        cv.put("timestamp", medLog.getTimestamp());
        cv.put("dose", medLog.getDose());
        cv.put("manual_entry", 1);
        cv.put("missed", 0);

        if (medLog.getAdminType() == MedLog.ROUTINE) {
            cv.put("time_frame", DataCheck.getDoseTimeFrame(DateTime.convertToTime12(medLog.getTimeOnly()), medLog.getDueTime()));
        } else {
            cv.put("time_frame", MedLog.ON_TIME);
        }

        return database.update("med_logs", cv, "ID_UNIQUE='" + medLog.getUniqueID() + "'", null);
    }

    public void submitMedicationAdmin(Medication medication, String manualTime) {

        ContentValues cv = new ContentValues();
        cv.put("ID_UNIQUE", DataCheck.createUniqueID(medication.getMedName()));
        cv.put("ID_FK", medication.getIdUnique());
        cv.put("name", medication.getMedName());
        cv.put("dose", medication.getDoseMeasure() + " " + medication.getDoseMeasureType());
        cv.put("due_time", medication.getNextDue());
        cv.put("admin_type", medication.getAdminType().equalsIgnoreCase("routine") ? MedLog.ROUTINE : MedLog.PRN);

        if (manualTime != null) { // dose time being entered manually.
            cv.put("manual_entry", 1);
            cv.put("timestamp", dt.getDate() + " " + DateTime.convertToTime24(manualTime));

            cv.put("time_frame", DataCheck.getDoseTimeFrame(manualTime, medication.getNextDue()));
        } else { // no manual entry.
            cv.put("timestamp", dt.getDate() + " " + DateTime.getTime24());
            if (medication.getAdminType().equalsIgnoreCase("routine")) {

                cv.put("time_frame", DataCheck.getDoseTimeFrame(DateTime.getTime12(), medication.getNextDue()));
            } else {
                cv.put("time_frame", MedLog.ON_TIME);
            }

            cv.put("manual_entry", 0);
        }
        cv.put("status", MedLog.ACTIVE);

        this.open();

        database.insert("med_logs", "ID_UNIQUE", cv);

    }

    public void submitMissedDose(Medication medication, String time) {

        ContentValues cv = new ContentValues();
        cv.put("ID_UNIQUE", DataCheck.createUniqueID(medication.getMedName()));
        cv.put("time_frame", MedLog.ON_TIME);
        cv.put("ID_FK", medication.getIdUnique());
        cv.put("name", medication.getMedName());
        cv.put("dose", medication.getDoseMeasure() + " " + medication.getDoseMeasureType());
        cv.put("timestamp", time);
        cv.put("missed", 1);
        cv.put("status", MedLog.SPACE_FILLER);
        cv.put("due_time", time);
        cv.put("admin_type", medication.getAdminType().equalsIgnoreCase("routine") ? MedLog.ROUTINE : MedLog.PRN);

        this.open();
        database.insert("med_logs", "ID_UNIQUE", cv);
    }

    public void submitSkippedDose(Medication medication) {
        ContentValues cv = new ContentValues();
        cv.put("ID_UNIQUE", DataCheck.createUniqueID(medication.getMedName()));
        cv.put("time_frame", MedLog.SKIPPED);
        cv.put("ID_FK", medication.getIdUnique());
        cv.put("name", medication.getMedName());
        cv.put("dose", medication.getDoseMeasure() + " " + medication.getDoseMeasureType());
        cv.put("timestamp", medication.getNextDue());
        cv.put("missed", 1);
        cv.put("status", MedLog.SKIPPED);
        cv.put("due_time", medication.getNextDue());
        cv.put("admin_type", medication.getAdminType().equalsIgnoreCase("routine") ? MedLog.ROUTINE : MedLog.PRN);

        this.open();
        database.insert("med_logs", "ID_UNIQUE", cv);
    }

    public Medication getSingleMedByName(int uniqueID) {

        this.open();

        Medication medication = new Medication();
        Cursor cs = database.rawQuery(Constants.GET_SINGLE_MED_BY_ID(uniqueID), null);

        while (cs.moveToNext()) {

            medication.setForEditDisplay();
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

    public void changeMedicationStatus(int idUnique, int newStatus) {
        this.open();

        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);

        database.update("medlist", cv, "ID_UNIQUE=" + idUnique, null);
        if (newStatus == Medication.DELETED) {
            database.delete("med_logs", "ID_FK = " + idUnique, null);
        }
    }

    private void insertPreference(String prefName, ContentValues cv) {
        this.open();
        Cursor cs = database.rawQuery("SELECT pref_name FROM prefs WHERE pref_name = '" + prefName + "'", null);
        if (cs.moveToFirst()) {
            database.update("prefs", cv, "pref_name = " + prefName, null);
        } else {
            database.insert("prefs", "pref_name", cv);
        }
    }

    public boolean getPreferenceBool(String name) {

        this.open();
        Cursor cs = database.rawQuery("SELECT pref_bool FROM prefs WHERE pref_name = '" + name + "'", null);

        if (cs.moveToFirst()) {
            return cs.getInt(0) == 1;
        }
        return true;

    }

    public void addOrUpdatePreference(String prefName, String prefValue) {
        ContentValues cv = new ContentValues();
        cv.put("pref_name", prefName);
        cv.put("pref_string", prefValue);
        insertPreference(prefName, cv);
    }

    public void addOrUpdatePreference(String prefName, boolean prefValue) {

        ContentValues cv = new ContentValues();
        cv.put("pref_name", prefName);
        cv.put("pref_bool", prefValue);
        insertPreference(prefName, cv);
    }

    public void addOrUpdatePreference(String prefName, int prefValue) {

        ContentValues cv = new ContentValues();
        cv.put("pref_name", prefName);
        cv.put("pref_int", prefValue);
        insertPreference(prefName, cv);
    }
}
