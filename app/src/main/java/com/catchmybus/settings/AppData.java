package com.catchmybus.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class AppData {
    public static String get(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(AppSettings.prefFile,
                Context.MODE_PRIVATE);
        return pref.getString(key, "none");
    }

    public static void put(Context context, String key, String message) {
        SharedPreferences pref = context.getSharedPreferences(AppSettings.prefFile,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(key, message);
        edit.commit();
    }
}
