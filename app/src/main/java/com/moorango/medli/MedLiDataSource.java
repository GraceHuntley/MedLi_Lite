package com.moorango.medli;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 *
 *
 */
public class MedLiDataSource {

    private static final MakeDateTimeHelper dt = new MakeDateTimeHelper();
    private static MedLiDataSource instance;
    final String TAG = "MedLiDataSource";
    private final SQLiteHelper dbHelper;
    // Database fields
    private SQLiteDatabase database;

    public MedLiDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
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

    public List<Medication> getAllMeds(String tag) {
        List<Medication> list = new ArrayList<Medication>();
        this.open(); // open db.

        Cursor cursor = database.rawQuery((tag.equals("routine")) ? Constants.GET_MEDLIST_ROUTINE : Constants.GET_MEDLIST_PRN, null);

        while (cursor.moveToNext()) {
            Medication medication = cursorToRoutine(cursor);
            list.add(medication);
        }
        cursor.close();
        return list;
    }

    private Medication cursorToRoutine(Cursor cursor) {
        final Medication medication = new Medication();

        medication.setMedName(cursor.getString(0));
        medication.setDoseMeasure(cursor.getInt(1));
        medication.setDoseMeasureType(cursor.getString(2));
        medication.setDoseCount(cursor.getInt(3));
        medication.setDoseTimes(cursor.getString(4));
        medication.setActualDoseCount(cursor.getInt(5));
        medication.setAdminType(cursor.getString(6));

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
                medication.setNextDue("MAXED DOSES!");
            } else {
                // TODO get last dose then see if it was recent.

                medication.setNextDue(getPrnNextDose(medication.getMedName(), medication.getDoseFrequency()));

            }
        }

        return medication;
    }

    public int getPrnDoseCount24Hours(String medName) {

        this.open();

        Cursor cs = database.rawQuery(Constants.GET_COUNT_LAST_24HOURS(medName), null);

        return (cs.moveToFirst()) ? cs.getInt(0) : 0;

    }

    public String getPrnNextDose(String medName, int freq) {

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
            int lastDosePlusHour = lastDoseHour + 1; // just for testing purposes.

            int currentHour = Integer.valueOf(dt.getTime24().split(":")[0]);

            if ((lastDosePlusHour - currentHour) > 0) {
                String nextDoseHour = "" + (lastDoseHour + freq);
                String minutes = "" + nextDose.split(" ")[1].split(":")[1];
                nextDose = dt.convertToTime12(nextDoseHour + ":" + minutes + ":" + "00");
            }
        }

        return nextDose;
    }

    public void submitNewMedication(Medication medication) {

        ContentValues cv = new ContentValues();
        cv.put("name", medication.getMedName());
        cv.put("dose_int", medication.getDoseMeasure());
        cv.put("dose_measure_type", medication.getDoseMeasureType());
        cv.put("admin_type", medication.getAdminType());
        cv.put("status", "active");
        cv.put("dose_count", medication.getDoseCount());
        cv.put("fillDate", medication.getFillDate());
        cv.put("startDate", medication.getStartDate());

        if (medication.getAdminType().equals("routine")) {
            cv.put("dose_times", medication.getDoseTimes());
        } else {
            cv.put("dose_frequency", medication.getDoseFrequency());
        }
        this.open();
        database.insert("medlist", "name", cv);


    }

    public void submitMedicationAdmin(Medication medication, String manualTime) {

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
                cv.put("late", isDoseLate(dt.getTime24(), medication.getNextDue()));
            }
            cv.put("manual_entry", 0);
        }

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
}
