package com.moorango.medli.utils;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.moorango.medli.Application;

public class PreferenceUtil {

    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(Application.getContext());
    }

    public static SharedPreferences.Editor getPreferenceEditor() {
        return getPreferences().edit();
    }

    public static String getPassword() {
        return getPreferences().getString("encryptedPassword", null);
    }

    public static void setPassword(String password) {
        getPreferenceEditor().putString("encryptedPassword", password).commit();
    }

    public static Boolean getLoggedIn() {
        return getPreferences().getBoolean("loggedIn", false);
    }

    public static void setLoggedIn(Boolean value) {
        getPreferenceEditor().putBoolean("loggedIn", value).commit();
    }

    public static String getEmail() {
        try {
            //noinspection ConstantConditions
            return getPreferences().getString("email", null) != null ? getPreferences().getString("email", null).trim() : null;
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return (null);
        }
    }

    public static void setEmail(String email) {
        if (email != null) {
            getPreferenceEditor().putString("email", email.trim()).commit();
        } else {
            getPreferenceEditor().putString("email", null).commit();
        }
    }
}