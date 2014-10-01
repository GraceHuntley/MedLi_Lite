package com.moorango.medli.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.moorango.medli.Constants;
import com.moorango.medli.Helpers.DataCheck;
import com.moorango.medli.Helpers.DateTime;
import com.moorango.medli.Models.Medication;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 */
@SuppressWarnings("WeakerAccess")
public class Helper_SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "Helper_SQLiteHelper";
    private static final String DATABASE_NAME = "medli.db";
    private static final int DATABASE_VERSION = 2;

    public Helper_SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Constants.CREATE_MEDLIST_DB);
        sqLiteDatabase.execSQL(Constants.CREATE_MEDLOGS_DB);
        sqLiteDatabase.execSQL(Constants.CREATE_PREFERENCES_DB);
        sqLiteDatabase.execSQL(Constants.CREATE_MED_RT_DOSE_DB);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch(newVersion) {
            case 2:
                Log.d(TAG, "Adding Med Dose table");
                sqLiteDatabase.execSQL(Constants.CREATE_MED_RT_DOSE_DB);
                copyDoseTimesToNewTable(sqLiteDatabase);

                break;
            default:
                throw new IllegalStateException(
                        "onUpgrade() with unknown newVersion" + newVersion);
        }
       // onCreate(sqLiteDatabase);
    }

    public void copyDoseTimesToNewTable(SQLiteDatabase database) {
        //this.open();

        String query = "SELECT ID_UNIQUE, name, dose_form, dose_times, dose_int, dose_measure_type FROM medlist WHERE admin_type = 'routine' AND STATUS = '" + Medication.ACTIVE;

        Cursor cs = database.rawQuery(query, null);

        while (cs.moveToNext()) { // iterate through medication's.

            String splitTimes[] = cs.getString(3).split(";");

            String doseForm;

            String readyDoseForm[] = DataCheck.getDoseFormNewTable(cs.getString(2)).split(";");

            double doseDouble = Double.parseDouble(readyDoseForm[0]);
            // TODO might throw a try in here for good luck. But should be pretty sanitary without.
            if (readyDoseForm.length > 1) {
                doseForm = readyDoseForm[1];
            } else {
                doseForm = "";
            }

            for (String time : splitTimes) {


                ContentValues cv = new ContentValues();

                cv.put("ID_UNIQUE", DataCheck.createUniqueID(cs.getString(1)));
                cv.put("KEY_FK", cs.getInt(0));
                cv.put("dose_time", DateTime.convertToTime24(time));
                cv.put("dose_dbl", doseDouble);
                cv.put("dose_form", doseForm.length() > 0 ? doseForm : "EDIT_THIS");
                cv.put("status", Medication.ACTIVE);

                database.insert("med_doses", "KEY_FK", cv);

            }
        }
    }
}
