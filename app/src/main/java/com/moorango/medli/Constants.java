package com.moorango.medli;

import com.moorango.medli.Models.MedLog;
import com.moorango.medli.Models.Medication;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 */

public class Constants {

    public static final String CREATE_PREFERENCES_DB = "CREATE TABLE prefs (" +
            "last_modified DATE DEFAULT (datetime('now','localtime')), " + // 0
            "pref_name TEXT PRIMARY KEY NOT NULL, " + // 1
            "pref_string TEXT, " + // 2
            "pref_int INTEGER, " + // 3
            "pref_bool BOOLEAN)";// 4

    public static final String CREATE_MEDLIST_DB = "CREATE TABLE medlist (" +
            "ID_UNIQUE INTEGER PRIMARY KEY NOT NULL, " + // 0
            "name TEXT NOT NULL, " + // 1
            "dose_int REAL NOT NULL, " + // 2
            "dose_measure_type TEXT NOT NULL, " + // 3
            "dose_form TEXT NOT NULL, " + // 4
            "admin_type TEXT NOT NULL, " + // 5
            "status INTEGER NOT NULL, " + // 6
            "startDate TEXT NOT NULL, " + // 7
            "dose_count INTEGER NOT NULL, " + // 8
            "fillDate TEXT, " + // 9
            "dose_times TEXT, " + // 10
            "dose_frequency TEXT, " + // 11
            "last_modified TEXT)"; // 12

    // TODO institute new db on update.
    public static final String CREATE_MED_RT_DOSE_DB = "CREATE TABLE med_doses (" +
            "ID_UNIQUE INTEGER PRIMARY KEY NOT NULL, " + // 0"
            "KEY_FK INTEGER, " + // 1
            "dose_time, " + // 2
            "dose_dbl, " + // 3
            "dose_measure, " + // 4
            "status)";

    public static final String CREATE_MEDLOGS_DB = "CREATE TABLE med_logs (" +
            "ID_UNIQUE INTEGER PRIMARY KEY NOT NULL, " +
            "name TEXT NOT NULL, " +
            "dose TEXT NOT NULL, " +
            "timestamp TEXT NOT NULL, " +
            "time_frame INTEGER NOT NULL, " +
            "missed BOOLEAN, " +
            "manual_entry BOOLEAN, " +
            "status INTEGER NOT NULL, " +
            "ID_FK INTEGER NOT NULL, " +
            "due_time TEXT NOT NULL, " +
            "admin_type INTEGER NOT NULL)";


    public static final String GET_MEDLIST_ROUTINE =
            "SELECT "
                    + "medlist.name as name, " // 0
                    + "medlist.dose_int as dose_int, " // 1
                    + "medlist.dose_measure_type as dose_type, " // 2
                    + "medlist.dose_count as max_count, " // 3
                    + "medlist.dose_times as dose_times, " // 4
                    + "COUNT(CASE WHEN med_logs.status !=" + MedLog.DELETED
                    + " AND DATE(med_logs.timestamp) = DATE('now', 'localtime') THEN 'ok' END) as actual_count, " // 5
                    + "medlist.admin_type as type, " // 6
                    + "medlist.dose_frequency as frequency, " // 7
                    + "medlist.status as status, " // 8
                    + "medlist.dose_form as doseform, " // 9
                    + "medlist.ID_UNIQUE as unique_id " // 10
                    + "from medlist "
                    + "LEFT OUTER JOIN med_logs ON medlist.ID_UNIQUE = med_logs.ID_FK "
                    + "WHERE medlist.status = '" + Medication.ACTIVE + "'"
                    + " OR medlist.status = '" + Medication.NEW + "'"
                    + " GROUP BY medlist.ID_UNIQUE ORDER By medlist.name ASC";


    public static final String GET_TODAYS_MED_ADMIN_LOGS =
            "SELECT med_logs.ID_UNIQUE as id, " // 0
                    + "med_logs.name as name, " // 1
                    + "med_logs.dose as dose, " // 2
                    + "med_logs.timestamp as time, " // 3
                    + "med_logs.time_frame as timeframe, " // 4
                    + "med_logs.missed as missed, " // 5
                    + "med_logs.manual_entry as manual, "  // 6
                    + "DATE(strftime('%s',timestamp), 'unixepoch', 'localtime') as date, " // 7
                    + "med_logs.due_time as duetime, " // 8
                    + "med_logs.admin_type as type " // 9
                    + "FROM med_logs "
                    + "WHERE status !=" + MedLog.DELETED
                    + " AND status !=" + MedLog.SPACE_FILLER
                    + " ORDER BY DATETIME(strftime('%s',timestamp), 'unixepoch', 'localtime') DESC ";

    public static String GET_SINGLE_MED_BY_ID(int id) {

        return "SELECT "
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
                + "WHERE ID_UNIQUE = " + id + " "
                + "AND status = " + Medication.ACTIVE
                + " OR status = " + Medication.NEW
                + " LIMIT 1";
    }

    public static String GET_COUNT_LAST_24HOURS(int fk) {
        return "SELECT COUNT(timestamp) FROM med_logs "
                + "WHERE status !=" + MedLog.DELETED
                + " AND "
                + "ID_FK=" + fk
                + " AND "
                + "DATE(timestamp) > datetime('now','-1 day', 'localtime')";
    }

    public static String GET_LAST_PRN_DOSE(int fk) {
        return "SELECT timestamp FROM med_logs "
                + "WHERE status=" + MedLog.ACTIVE
                + " AND "
                + "ID_FK='" + fk + "' "
                + "AND "
                + "DATE(timestamp) > datetime('now', '-1 day', 'localtime') "
                + "ORDER BY timestamp DESC "
                + "LIMIT 1";
    }

    public static final String GET_TOTAL_MED_COUNT =
            "SELECT COUNT(ID_UNIQUE) FROM medlist "
                    + "WHERE status=" + Medication.ACTIVE
                    + " OR status=" + Medication.NEW;

    public static final String PRN_ALARM_MESSAGE = "This feature allows you to log a non-routine medication, no questions asked. " +
            "It also sets an alarm for the next time you are able to take the medication. " +
            "This alarm is a one time alarm that will only be reset again when you activate this. \n\n" +
            "In order to activate it long press the icon\n\n" +
            "You will only be able to use this feature when you have doses available to be taken.";
}





