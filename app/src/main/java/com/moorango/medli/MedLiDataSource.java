package com.moorango.medli;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.security.Timestamp;
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
    // Database fields
    private SQLiteDatabase database;
    private final SQLiteHelper dbHelper;
    private static MedLiDataSource instance;
    private static MakeDateTimeHelper dt = new MakeDateTimeHelper();

    public MedLiDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public static synchronized MedLiDataSource getHelper(Context context) {
        if (instance == null)
            instance = new MedLiDataSource(context);

        return instance;
    }

    public void open() throws SQLException {
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

    public List<Medication> getAllRoutineMeds() {

        this.open();
        List<Medication> list = new ArrayList<Medication>();
        //Cursor cursor = database.query("medlist", null, "admin_type='routine'", null, null, null, "name");
        Cursor cursor = database.rawQuery(Constants.GET_MEDLIST_ROUTINE, null);


        while (cursor.moveToNext()) {
            Medication medication = cursorToRoutine(cursor);
            Log.d("MedliData", medication.toString());
            list.add(medication);

        }
        cursor.close();


        return list;
    }

    public List<Medication> getAllPrnMeds() {

        this.open();
        List<Medication> list = new ArrayList<Medication>();
        Cursor cursor = database.query("medlist", null, "admin_type='prn'", null, null, null, "name");


        while (cursor.moveToNext()) {
            Medication medication = cursorToRoutine(cursor);
            Log.d("MedliData", medication.toString());
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
        }

        return medication;
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
            cv.put("late", isDoseLate(dt.getTime24().toString(), medication.getNextDue()));
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
