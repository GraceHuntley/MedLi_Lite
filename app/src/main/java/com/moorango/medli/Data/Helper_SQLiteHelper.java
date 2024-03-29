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
        Log.w(Helper_SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(sqLiteDatabase);
    }
}
