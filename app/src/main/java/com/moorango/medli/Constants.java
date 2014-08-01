package com.moorango.medli;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 */
public class Constants {

    private static MakeDateTimeHelper dt = new MakeDateTimeHelper();

    public static final String CREATE_MEDLIST_DB = "CREATE TABLE medlist (" +
            "ID_UNIQUE INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "dose_int INTEGER NOT NULL, " +
            "dose_measure_type TEXT NOT NULL, " +
            "admin_type TEXT NOT NULL, " +
            "status TEXT NOT NULL, " +
            "startDate TEXT NOT NULL, " +
            "dose_count INTEGER NOT NULL, " +
            "fillDate TEXT, " +
            "dose_times TEXT, " +
            "dose_frequency TEXT)";

    public static final String CREATE_MEDLOGS_DB = "CREATE TABLE med_logs (" +
            "ID_UNIQUE TEXT UNIQUE NOT NULL, " +
            "name TEXT NOT NULL, " +
            "dose TEXT NOT NULL, " +
            "timestamp TEXT NOT NULL, " +
            "late BOOLEAN, " +
            "missed BOOLEAN, " +
            "manual_entry BOOLEAN)";

    public static final String GET_MEDLIST_ROUTINE = "SELECT medlist.name as name, medlist.dose_int as dose_int, medlist.dose_measure_type as dose_type, medlist.dose_count as max_count, medlist.dose_times as dose_times, "
            + "COUNT(CASE WHEN DATE(med_logs.timestamp) = '"
            + dt.getDate()
            + "' THEN 'ok' END) as actual_count, admin_type as type from medlist "
            + "LEFT OUTER JOIN med_logs ON medlist.name = med_logs.name WHERE medlist.admin_type = 'routine' "
            + "GROUP BY medlist.name ORDER By medlist.name ASC";
}
