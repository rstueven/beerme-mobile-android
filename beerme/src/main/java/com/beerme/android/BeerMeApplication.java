package com.beerme.android;

import android.app.Application;

import com.beerme.android.db.BeerMeDatabase;
import com.beerme.android.db.Brewery;
import com.beerme.android.util.NetworkRequestQueue;
import com.beerme.android.util.SharedPref;

import java.util.Calendar;

public class BeerMeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // SharedPreferences singleton
        SharedPref.init(getApplicationContext());
        int statusFilter[] = SharedPref.read(SharedPref.Pref.STATUS_FILTER);
        if (statusFilter.length == 0) {
            SharedPref.write(SharedPref.Pref.STATUS_FILTER, new int[]{Brewery.Status.OPEN.code, Brewery.Status.PLANNED.code});
        }

        // Change this whenever a new database file is installed.
        // OR
        // Get the last update date from the database itself.
        long updated = SharedPref.read(SharedPref.Pref.DB_LAST_UPDATE, 0L);
        if (updated <= 0L) {
            Calendar cal = Calendar.getInstance();
            cal.set(2019, 0, 21);
            SharedPref.write(SharedPref.Pref.DB_LAST_UPDATE, cal.getTimeInMillis());
        }

        // Volley RequestQueue singleton
        NetworkRequestQueue.init(getApplicationContext());

        // Database singleton
        BeerMeDatabase.init(getApplicationContext());

//        // See NFS AgSimplifiedActivity.java
//        LocationClient.init(getApplicationContext());
    }
}