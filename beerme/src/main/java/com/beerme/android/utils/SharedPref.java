package com.beerme.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPref {
    private static SharedPreferences mSharedPref;

    private SharedPref() {
    }

    public static void init(Context context) {
        if (mSharedPref == null) {
            mSharedPref = context.getSharedPreferences("CurrentUser", MODE_PRIVATE);
        }
    }

    public static String read(Pref key, String defVal) {
        return mSharedPref.getString(key.key(), defVal);
    }

    public static boolean read(Pref key, boolean defVal) {
        return mSharedPref.getBoolean(key.key(), defVal);
    }

    public static void write(Pref key, String value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putString(key.key(), value);
        prefsEditor.apply();
    }

    public static void write(Pref key, boolean value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putBoolean(key.key(), value);
        prefsEditor.apply();
    }

    public enum Pref {
        KEY_DB_UPDATING("db_updating"),
        KEY_DB_LAST_UPDATE("db_last_update");

        private final String key;

        Pref(String s) {
            key = s;
        }

        private String key() {
            return key;
        }
    }
}