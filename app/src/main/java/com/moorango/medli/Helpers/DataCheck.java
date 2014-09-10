package com.moorango.medli.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Models.MedLog;
import com.moorango.medli.Models.Medication;

import java.util.Map;

/**
 * Created by Colin on 7/17/2014.
 * Copyright 2014
 */
@SuppressWarnings("WeakerAccess")
public class DataCheck {

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

    public static boolean isDoseLate(String due) {
        // TODO test whether admin time is late.
        DateTime dt = new DateTime();
        @SuppressWarnings("UnusedAssignment") final String TAG = "VerifyHelpers";
        int nextDoseHour = Integer.valueOf(dt.convertToTime24(due).split(":")[0]);
        int loggedHour = Integer.valueOf(DateTime.getTime24().split(":")[0]);

        int difference = loggedHour - nextDoseHour;

        return difference > 1;
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
            if (isDoseLate(dose)) {
                dataSource.submitMissedDose(medication, dose);
            }
            nextDose = dose;
        }
        dataSource.changeMedicationStatus(medication.getIdUnique(), Medication.ACTIVE);
        return (nextDose != null) ? nextDose : "Complete";
    }

    public static int createUniqueID(String medName) {
        return (medName + DateTime.getNowInMillisec()).hashCode();
    }

    /**
     * Function compares current time, along with doses today, and available doses to fill in missed ones.
     * meant only for Routine meds.
     */
    public static void fillInMissedDoses(Context context, int idUnique) {

        Map<String, Boolean> map = MedLiDataSource.getHelper(context).getMedDosesByID(idUnique);


    }

    public static int isDoseLate(String time, String due) {
        // TODO test whether admin time is late.
        int nextDoseHour = Integer.valueOf(DateTime.convertToTime24(due).split(":")[0]);
        int loggedHour = Integer.valueOf(DateTime.convertToTime24(time).split(":")[0]);

        int difference = loggedHour - nextDoseHour;

        if (difference > 1) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int isDoseEarly(String time, String due) {
        int nextDoseHour = Integer.valueOf(DateTime.convertToTime24(due).split(":")[0]);
        int loggedHour = Integer.valueOf(DateTime.convertToTime24(time).split(":")[0]);

        int difference = nextDoseHour - loggedHour;

        if (difference > 1) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int getDoseTimeFrame(String time, String due) {
        int nextDoseHour = Integer.valueOf(DateTime.convertToTime24(due).split(":")[0]);
        int loggedHour = Integer.valueOf(DateTime.convertToTime24(time).split(":")[0]);

        if ((nextDoseHour - loggedHour) > 1) {
            return MedLog.EARLY;
        } else if ((loggedHour - nextDoseHour) > 1) {
            return MedLog.LATE;
        }
        return MedLog.ON_TIME;
    }
}
