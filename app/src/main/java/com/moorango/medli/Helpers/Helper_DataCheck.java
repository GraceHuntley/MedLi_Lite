package com.moorango.medli.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Models.Object_Medication;

/**
 * Created by Colin on 7/17/2014.
 * Copyright 2014
 */
@SuppressWarnings("WeakerAccess")
public class Helper_DataCheck {

    Helper_DataCheck() {

    }

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
        Helper_DateTime dt = new Helper_DateTime();
        final String TAG = "VerifyHelpers";
        int nextDoseHour = Integer.valueOf(dt.convertToTime24(due).split(":")[0]);
        int loggedHour = Integer.valueOf(Helper_DateTime.getTime24().split(":")[0]);

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
    public static String findNextDoseNewMed(Context con, Object_Medication medication) {

        MedLiDataSource dataSource = MedLiDataSource.getHelper(con);

        String availableDoses[] = medication.getDoseTimes().split(";");

        String nextDose = null;
        for (String dose : availableDoses) {
            if (isDoseLate(dose)) {
                dataSource.submitMissedDose(medication, dose);
            }
            nextDose = dose;
        }
        return (nextDose != null) ? nextDose : "Complete";
    }
}
