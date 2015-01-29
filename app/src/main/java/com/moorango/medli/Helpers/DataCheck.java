package com.moorango.medli.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Models.MedLog;
import com.moorango.medli.Models.Medication;
import com.moorango.medli.R;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Random;

/**
 * Created by Colin on 7/17/2014.
 * Copyright 2014
 */
@SuppressWarnings("WeakerAccess")
public class DataCheck {

    private static final String TAG = "DataCheck";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getCountVerbage(int count) {
        switch (count) {
            case 1:
                return "" + count + "st";

            case 2:
                return "" + count + "nd";

            case 3:
                return "" + count + "rd";

            default:
                return "" + count + "th";

        }
    }

    public static String capitalizeTitles(String toCapitalize) {

        String splitString[] = toCapitalize.trim().split(" ");
        StringBuilder readyForReturn = new StringBuilder();

        for (String titleWord : splitString) {
            if (titleWord.length() > 0) {
                readyForReturn.append(titleWord.substring(0, 1).toUpperCase())
                        .append(titleWord.substring(1, titleWord.length()))
                        .append(" ");
            }
        }
        if (readyForReturn.toString().length() > 0) {
            return readyForReturn.toString().substring(0, readyForReturn.length() - 1);
        } else {
            return "";
        }

    }


    /**
     * Matches current hour to corresponding dose.
     *
     * @param con        Context
     * @param medication Medication Object.
     * @return
     */
    @SuppressWarnings("UnusedReturnValue")
    public static String findNextDoseNewMed(Context con, Medication medication) {

        MedLiDataSource dataSource = MedLiDataSource.getHelper(con);

        String availableDoses[] = medication.getDoseTimes().split(";");

        String nextDose = null;
        for (String dose : availableDoses) {
            String doseTS = DateTime.getCurrentTimestamp(true, dose);
            if (isDoseLate(doseTS)) {
                dataSource.submitMissedDose(medication, doseTS);

            } else {

                return doseTS;
            }
            nextDose = doseTS;
        }
        dataSource.changeMedicationStatus(medication.getIdUnique(), Medication.ACTIVE);

        return (nextDose != null && !isDoseLate(nextDose)) ? nextDose : "COMPLETE";
    }

    /**
     * Returns a semi-unique id .
     * <p/>
     * UNIT-TESTED
     *
     * @param medName
     * @return
     */
    public static int createUniqueID(String medName) {

        return (new Random().nextInt() + medName + DateTime.getNowInMillisec()).hashCode();
    }

    public static void clearForm(ViewGroup group) {

        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {
                if (view.getId() == R.id.admin_count_edittext) {
                    ((EditText) view).setText("0");
                } else {
                    ((EditText) view).setText("");
                }
                ((EditText) view).setError(null);

            }

            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                clearForm((ViewGroup) view);
        }

    }

    public static void clearFormErrors(ViewGroup group) {

        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {

                ((EditText) view).setError(null);

            }

            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                clearFormErrors((ViewGroup) view);
        }

    }

    public static boolean isDoseLate(String dueTime) {
        // TODO test whether admin time is late.

        return dueTime.compareTo(DateTime.getCurrentTimestamp(false, null)) < 0;
    }

    /**
     * Get time frame of dosing ie. Late, early, or on-time.
     * <p/>
     * UNIT-TESTED
     *
     * @param time String yyyy-MM-dd HH:mm:ss
     * @param due  String yyyy-MM-dd HH:mm:ss
     * @return int 400 for error, or Constant value from MedLogs object.
     */
    public static int getDoseTimeFrame(String time, String due) {
        if (validateDateEntry(time) && validateDateEntry(due)) {

            if (!DataCheck.isToday(due)) {
                return MedLog.EXTRA_DOSE;
            } else {

                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                long curTimeMillis = formatter.parseDateTime(time).getMillis();
                long dueTimeMillis = formatter.parseDateTime(due).getMillis();

                long timeWindowMillis = 20; // time frame window.

                int differenceMinutes = (int) ((curTimeMillis - dueTimeMillis) / (60 * 1000));

                if (differenceMinutes < 0 && Math.abs(differenceMinutes) >= timeWindowMillis) { // early
                    return MedLog.EARLY;
                } else if (differenceMinutes > 0 && Math.abs(differenceMinutes) >= timeWindowMillis) { // late
                    return MedLog.LATE;
                } else { // on-time.
                    return MedLog.ON_TIME;
                }
            }
        }
        return 400;
    }

    /**
     * Checks if provided String is today. returns true if date equal to day
     * false otherwise.
     *
     * @param nextDue
     * @return
     */
    public static boolean isToday(String nextDue) {

        return nextDue.split(" ")[0].compareTo(DateTime.getCurrentTimestamp(false, null).split(" ")[0]) == 0;
    }

    public static String getNumberVerbage(int number, String starter) {

        switch (number) {
            case 1:
                return starter;
            default:
                return starter += "s";

        }
    }

    public static boolean validateDateEntry(String date) {

        return date.matches("^(20[1-9][1-9])-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]) (0[0-9]|1[0-9]|2[0-4]):([0-5][0-9]):([0-5][0-9])");
    }
}
