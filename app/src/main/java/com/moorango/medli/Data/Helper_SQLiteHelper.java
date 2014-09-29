package com.moorango.medli.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.moorango.medli.Constants;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 */
@SuppressWarnings("WeakerAccess")
public class Helper_SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "Helper_SQLiteHelper";
    private static final String DATABASE_NAME = "medli.db";
    private static final int DATABASE_VERSION = 1;

    public Helper_SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Constants.CREATE_MEDLIST_DB);
        sqLiteDatabase.execSQL(Constants.CREATE_MEDLOGS_DB);
        sqLiteDatabase.execSQL(Constants.CREATE_PREFERENCES_DB);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch(newVersion) {
            case 2:
                Log.d(TAG, "Adding Med Dose table");
                sqLiteDatabase.execSQL(Constants.CREATE_MED_RT_DOSE_DB);
                break;
            default:
                throw new IllegalStateException(
                        "onUpgrade() with unknown newVersion" + newVersion);
        }
       // onCreate(sqLiteDatabase);
    }
}
