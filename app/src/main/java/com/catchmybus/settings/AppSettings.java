package com.catchmybus.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class AppSettings {
    public static String apiUrl = "http://192.168.0.100:8000";
    public static String prefFile = "datafile";

    public static JSONObject getAuthJson(Context context) {
        JSONObject data = new JSONObject();
        SharedPreferences pref = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        String token = pref.getString("token", "none");
        try {
            data.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
}
