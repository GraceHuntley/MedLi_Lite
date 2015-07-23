package com.moorango.medli.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
    public static JSONObject parseString(String body) {
        if (body == null) return null;
        try {
            return new JSONObject(body);
        } catch (JSONException e) {
            return new JSONObject();
        }

    }

    public static String getString(String key, JSONObject body) {
        return getString(key, "", body);
    }

    public static String getString(String key, String defaultValue, JSONObject body) {
        try {
            return body.getString(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }


    public static Boolean getBoolean(String key, JSONObject body) {
        try {
            return body.getBoolean(key);
        } catch (JSONException e) {
            return false;
        }
    }
}