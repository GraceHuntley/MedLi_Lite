package com.moorango.medli.utils;

/**
 * Created by cmac147 on 3/2/16.
 */

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.moorango.medli.Application;


public class PreferencesUtil {
    public static final int NOTIFICATIONS_ON = 1;
    public static final int NOTIFICATIONS_OFF = 0;
    public static final int NOTIFICATIONS_NOT_SET = -1;

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

    public static Boolean getHasUnreadNotifications() {
        return getPreferences().getBoolean("unreadNotifications", false);
    }

    public static void setHasUnreadNotifications(Boolean value) {
        getPreferenceEditor().putBoolean("unreadNotifications", value).commit();
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

    public static String getFirstName() {
        return getPreferences().getString("firstName", null);
    }

    public static void setFirstName(String firstName) {
        getPreferenceEditor().putString("firstName", firstName).commit();
    }

    public static String getLastName() {
        return getPreferences().getString("lastName", null);
    }

    public static void setLastName(String lastName) {
        getPreferenceEditor().putString("lastName", lastName).commit();
    }

    public static String getCountryCode() {
        return getPreferences().getString("countryCode", null);
    }

    public static void setCountryCode(String countryCode) {
        getPreferenceEditor().putString("countryCode", countryCode).commit();
    }

    public static String getPhoneNumber() {
        return getPreferences().getString("phoneNumber", null);
    }

    public static void setPhoneNumber(String phoneNumber) {
        getPreferenceEditor().putString("phoneNumber", phoneNumber).commit();
    }

    public static String getUserId() {
        return getPreferences().getString("userId", null);
    }

    public static void setUserId(String userId) {
        getPreferenceEditor().putString("userId", userId).commit();
    }

    public static String getAccessToken() {
        return getPreferences().getString("accessToken", null);
    }

    public static void setAccessToken(String accessToken) {
        getPreferenceEditor().putString("accessToken", accessToken).commit();
    }

    public static String getProfilePicPath() {
        return getPreferences().getString("profilePicPath", null);
    }

    public static void setProfilePicPath(String path) {
        getPreferenceEditor().putString("profilePicPath", path).commit();
    }

    public static int getNotifications() {
        return getPreferences().getInt("notifications", NOTIFICATIONS_NOT_SET);
    }

    public static void setNotifications(int value) {
        getPreferenceEditor().putInt("notifications", value).commit();
    }

    public static Boolean getSentGcmTokenToServer() {
        return getPreferences().getBoolean("sentGcmTokenToServer", false);
    }

    public static void setSentGcmTokenToServer(Boolean value) {
        getPreferenceEditor().putBoolean("sentGcmTokenToServer", value).commit();
    }

    public static String getGcmToken() {
        return getPreferences().getString("gcmToken", null);
    }

    public static void setGcmToken(String gcmToken) {
        getPreferenceEditor().putString("gcmToken", gcmToken).commit();
    }

    public static void deleteGcmToken() {
        getPreferenceEditor().remove("gcmToken").apply();
    }
}