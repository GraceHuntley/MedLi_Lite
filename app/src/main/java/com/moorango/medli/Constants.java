package com.moorango.medli;

/**
 * Created by Colin on 7/31/2014.
 * Copyright 2014
 */

public class Constants {

    public static final String CREATE_MEDLIST_DB = "CREATE TABLE medlist (" +
            "ID_UNIQUE INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "dose_int REAL NOT NULL, " +
            "dose_measure_type TEXT NOT NULL, " +
            "admin_type TEXT NOT NULL, " +
            "status TEXT NOT NULL, " +
            "startDate TEXT NOT NULL, " +
            "dose_count INTEGER NOT NULL, " +
            "fillDate TEXT, " +
            "dose_times TEXT, " +
            "dose_frequency TEXT, " +
            "last_modified TEXT)";


    public static final String CREATE_MEDLOGS_DB = "CREATE TABLE med_logs (" +
            "ID_UNIQUE TEXT UNIQUE NOT NULL, " +
            "name TEXT NOT NULL, " +
            "dose TEXT NOT NULL, " +
            "timestamp TEXT NOT NULL, " +
            "late BOOLEAN, " +
            "missed BOOLEAN, " +
            "manual_entry BOOLEAN, " +
            "status TEXT)";

    public static final String CREATE_DOSE_MEASURE_DB = "CREATE TABLE med_dose_measures (" +
            "med_name TEXT, " +
            "dose_measure_int TEXT, " +
            "dose_measure_type TEXT NOT NULL, " +
            "user_added boolean DEFAULT 0)";

    public static final String GET_MEDLIST_ROUTINE =
            "SELECT medlist.name as name, "
                    + "medlist.dose_int as dose_int, "
                    + "medlist.dose_measure_type as dose_type, "
                    + "medlist.dose_count as max_count, "
                    + "medlist.dose_times as dose_times, "
                    + "COUNT(CASE WHEN med_logs.status = 'active' AND DATE(med_logs.timestamp) = DATE('now', 'localtime') THEN 'ok' END) as actual_count, "
                    + "medlist.admin_type as type, "
                    + "medlist.dose_frequency as frequency, "
                    + "medlist.status as status "
                    + "from medlist "
                    + "LEFT OUTER JOIN med_logs ON medlist.name = med_logs.name "
                    + "WHERE medlist.status = 'active' "
                    + "OR medlist.status = 'new' "
                    + "GROUP BY medlist.name ORDER By medlist.name ASC";

    public static final String GET_TODAYS_MED_ADMIN_LOGS =
            "SELECT med_logs.ID_UNIQUE as id, "
                    + "med_logs.name as name, "
                    + "med_logs.dose as dose, "
                    + "med_logs.timestamp as time, "
                    + "med_logs.late as late, "
                    + "med_logs.missed as missed, "
                    + "med_logs.manual_entry as manual, "
                    + "DATE(strftime('%s',timestamp), 'unixepoch', 'localtime') as date "
                    + "FROM med_logs "
                    + "WHERE status = 'active' "
                    + "ORDER BY DATETIME(strftime('%s',timestamp), 'unixepoch', 'localtime') DESC ";

    public static String GET_COUNT_LAST_24HOURS(String name) {
        return "SELECT COUNT(timestamp) FROM med_logs "
                + "WHERE status='active' "
                + "AND "
                + "name='" + name + "' "
                + "AND "
                + "DATE(timestamp) > datetime('now','-1 day', 'localtime')";
    }

    public static String GET_LAST_PRN_DOSE(String name) {
        return "SELECT timestamp FROM med_logs "
                + "WHERE status='active' "
                + "AND "
                + "name='" + name + "' "
                + "AND "
                + "DATE(timestamp) > datetime('now', '-1 day', 'localtime') "
                + "ORDER BY timestamp DESC "
                + "LIMIT 1";
    }

    public static final String RX_ATTRIBUTES =
            "consists_of+"
                    + "constitutes+"
                    + "contains+"
                    + "dose_form_of+"
                    + "has_dose_form+"
                    + "has_form+"
                    + "has_ingredient+"
                    + "has_ingredients+"
                    + "has_part+"
                    + "has_precise_ingredient+"
                    + "has_tradename+"
                    + "has_doseformgroup+"
                    + "ingredient_of+"
                    + "ingredients_of+"
                    + "inverse_isa+"
                    + "isa+"
                    + "precise_ingredient_of+"
                    + "quantified_form_of+"
                    + "reformulation_of+"
                    + "doseformgroup_of";

    public static final String GET_TOTAL_MED_COUNT =
            "SELECT COUNT(name) FROM medlist "
                    + "WHERE status='active'";
}



