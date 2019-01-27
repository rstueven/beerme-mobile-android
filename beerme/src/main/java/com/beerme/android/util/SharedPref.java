package com.beerme.android.util;

// https://stackoverflow.com/a/40347393/7746286

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;

public class SharedPref {

    public enum Pref {
        DB_LAST_UPDATE("dbLastUpdate"),
        IS_REQUESTING_LOCATION_UPDATES("isRequestingLocationUpdates");

        private final String key;

        Pref(String s) {
            key = s;
        }

        private String key() {
            return key;
        }
    }

    private static SharedPreferences mInstance;

    private SharedPref() {
    }

    public static void init(Context context) {
        Log.d("beerme", "SharedPref.init()");
        if (mInstance == null) {
            mInstance = context.getSharedPreferences("CurrentUser", MODE_PRIVATE);
        }
    }

    // TODO: DRY these methods. Use generics?

    public static String read(@NonNull Pref key, String defVal) {
        return mInstance.getString(key.key(), defVal);
    }

    public static void write(@NonNull Pref key, String value) {
        SharedPreferences.Editor prefsEditor = mInstance.edit();
        prefsEditor.putString(key.key(), value);
        prefsEditor.apply();
    }

    public static long read(@NonNull Pref key, long defVal) {
        return mInstance.getLong(key.key(), defVal);
    }

    public static void write(@NonNull Pref key, long value) {
        SharedPreferences.Editor prefsEditor = mInstance.edit();
        prefsEditor.putLong(key.key(), value);
        prefsEditor.apply();
    }

    public static boolean read(@NonNull Pref key, boolean defVal) {
        return mInstance.getBoolean(key.key(), defVal);
    }

    public static void write(@NonNull Pref key, boolean value) {
        SharedPreferences.Editor prefsEditor = mInstance.edit();
        prefsEditor.putBoolean(key.key(), value);
        prefsEditor.apply();
    }
}