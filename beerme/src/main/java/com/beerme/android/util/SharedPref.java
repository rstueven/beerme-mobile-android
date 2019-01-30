package com.beerme.android.util;

// https://stackoverflow.com/a/40347393/7746286

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.beerme.android.db.Brewery;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;

public class SharedPref {

    public enum Pref {
        DB_LAST_UPDATE("dbLastUpdate"),
        STATUS_FILTER("statusFilter"),
        DISTANCE_UNIT("distanceUnit"),
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

        int statusFilter = SharedPref.read(SharedPref.Pref.STATUS_FILTER, 0);
        if (statusFilter == 0) {
            SharedPref.write(SharedPref.Pref.STATUS_FILTER, Brewery.Status.DEFAULT);
        }

        int distanceUnit = SharedPref.read(Pref.DISTANCE_UNIT, 0);
        if (distanceUnit == 0) {
            SharedPref.write(Pref.DISTANCE_UNIT, Measurer.DistanceUnit.DEFAULT);
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

    public static int read(@NonNull Pref key, int defVal) {
        return mInstance.getInt(key.key(), defVal);
    }

    public static void write(@NonNull Pref key, int value) {
        SharedPreferences.Editor prefsEditor = mInstance.edit();
        prefsEditor.putInt(key.key(), value);
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

//    public static int[] readIntArray(@NonNull Pref key) {
//        int intArray[] = new int[0];
//        Set<String> set = mInstance.getStringSet(key.key(), new ArraySet<String>());
//        if (set != null) {
//            String stringArray[] = new String[set.size()];
//            stringArray = set.toArray(stringArray);
//            intArray = new int[stringArray.length];
//            for (int i = 0; i < stringArray.length; i++) {
//                try {
//                    intArray[i] = Integer.parseInt(stringArray[i]);
//                } catch (NumberFormatException e) {
//                    Log.w("beerme", "SharedPref.read(" + key + "): " + e.getLocalizedMessage());
//                    intArray[i] = 0;
//                }
//            }
//        }
//
//        return intArray;
//    }
//
//    public static void writeIntArray(@NonNull Pref key, int[] value) {
//        SharedPreferences.Editor prefsEditor = mInstance.edit();
//        String stringArray[] = new String[value.length];
//        for (int i = 0; i < value.length; i++) {
//            stringArray[i] = Integer.toString(value[i]);
//        }
//
//        Set<String> set = new ArraySet<>(Arrays.asList(stringArray));
//
//        prefsEditor.putStringSet(key.key(), set);
//        prefsEditor.apply();
//    }
//
//    public static void write(@NonNull Pref key, int[] value) {
//        writeIntArray(key, value);
//    }
//
//    public static List<Integer> readIntList(@NonNull Pref key) {
//        List<Integer> intList = new ArrayList<>();
//        Set<String> set = mInstance.getStringSet(key.key(), new ArraySet<String>());
//        if (set != null) {
//            String stringArray[] = new String[set.size()];
//            stringArray = set.toArray(stringArray);
//            for (int i = 0; i < stringArray.length; i++) {
//                try {
//                    intList.add(Integer.parseInt(stringArray[i]));
//                } catch (NumberFormatException e) {
//                    Log.w("beerme", "SharedPref.read(" + key + "): " + e.getLocalizedMessage());
//                    intList.add(0);
//                }
//            }
//        }
//
//        return intList;
//    }
//
//    public static void writeIntList(@NonNull Pref key, List<Integer> value) {
//        SharedPreferences.Editor prefsEditor = mInstance.edit();
//        String stringArray[] = new String[value.size()];
//        for (int i = 0; i < value.size(); i++) {
//            stringArray[i] = Integer.toString(value.get(i));
//        }
//
//        Set<String> set = new ArraySet<>(Arrays.asList(stringArray));
//
//        prefsEditor.putStringSet(key.key(), set);
//        prefsEditor.apply();
//    }
//
//    public static void write(@NonNull Pref key, List<Integer> value) {
//        writeIntList(key, value);
//    }
}