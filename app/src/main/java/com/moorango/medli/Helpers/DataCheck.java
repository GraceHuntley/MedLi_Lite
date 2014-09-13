package com.moorango.medli.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Models.MedLog;
import com.moorango.medli.Models.Medication;
import com.moorango.medli.R;

import java.util.Map;
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
        String splitString[] = toCapitalize.split(" ");
        String readyForReturn = "";

        for (String titleWord : splitString) {
            readyForReturn += titleWord.substring(0, 1).toUpperCase() + titleWord.substring(1, titleWord.length());
        }

        return readyForReturn;
    }


    /**
     * Matches current hour to corresponding dose.
     *
     * @param con        Context
     * @param medication Medication Object.
     * @return
     */
    public static String findNextDoseNewMed(Context con, Medication medication) {

        MedLiDataSource dataSource = MedLiDataSource.getHelper(con);

        String availableDoses[] = medication.getDoseTimes().split(";");

        String nextDose = null;
        for (String dose : availableDoses) {
            if (isDoseLate(dose, true)) {
                dataSource.submitMissedDose(medication, dose);
            } else {
                dataSource.changeMedicationStatus(medication.getIdUnique(), Medication.ACTIVE);
                return dose;
            }
            nextDose = dose;
        }
        dataSource.changeMedicationStatus(medication.getIdUnique(), Medication.ACTIVE);

        return (nextDose != null && !isDoseLate(nextDose, false)) ? nextDose : "Complete";
    }

    public static int createUniqueID(String medName) {


        return (new Random().nextInt() + medName + DateTime.getNowInMillisec()).hashCode();
    }

    /**
     * Function compares current time, along with doses today, and available doses to fill in missed ones.
     * meant only for Routine meds.
     */
    public static void fillInMissedDoses(Context context, int idUnique) {

        Map<String, Boolean> map = MedLiDataSource.getHelper(context).getMedDosesByID(idUnique);


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

    public static boolean isDoseLate(String due, boolean filler) {
        // TODO test whether admin time is late.

        String nextDose24Hour[] = DateTime.convertToTime24(due).split(":");
        String loggedDose24Hour[] = DateTime.getTime24().split(":");

        int nextDoseHour = Integer.valueOf(nextDose24Hour[0]);
        int loggedHour = Integer.valueOf(loggedDose24Hour[0]);

        int nextDoseMinute = Integer.valueOf(nextDose24Hour[1]);
        int loggedMinute = Integer.valueOf(loggedDose24Hour[1]);

        int nextDoseTotalMinutes = (nextDoseHour * 60) + nextDoseMinute;
        int loggedDoseTotalMinutes = (loggedHour * 60) + loggedMinute;

        int difference = loggedDoseTotalMinutes - nextDoseTotalMinutes;

        if (filler) {
            return difference > 1;
        }

        return difference > 2;
    }

    public static int getDoseTimeFrame(String time, String due) {

        if (due.equalsIgnoreCase("complete")) {
            return MedLog.EXTRA_DOSE;
        }
        try {
            String nextDose24Hour[] = DateTime.convertToTime24(due).split(":");
            String loggedDose24Hour[] = DateTime.convertToTime24(time).split(":");

            int nextDoseHour = Integer.valueOf(nextDose24Hour[0]);
            int nextDoseMinute = Integer.valueOf(nextDose24Hour[1]);

            int loggedDoseHour = Integer.valueOf(loggedDose24Hour[0]);
            int loggedDoseMinute = Integer.valueOf(loggedDose24Hour[1]);

            int nextDoseTotalMinutes = (nextDoseHour * 60) + nextDoseMinute;
            int loggedDoseTotalMinutes = (loggedDoseHour * 60) + loggedDoseMinute;

            if ((nextDoseTotalMinutes - loggedDoseTotalMinutes) > 30) {
                return MedLog.EARLY;
            } else if ((loggedDoseTotalMinutes - nextDoseTotalMinutes) > 30) {
                return MedLog.LATE;
            }
        } catch (NumberFormatException nfe) {
            Log.e(TAG + " getDoseTimeFrame", nfe.toString());
        }
        return MedLog.ON_TIME;
    }
}
