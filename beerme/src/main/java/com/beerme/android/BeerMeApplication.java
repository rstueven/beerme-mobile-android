package com.beerme.android;

import android.app.Application;

import com.beerme.android.db.BeerMeDatabase;
import com.beerme.android.util.NetworkRequestQueue;
import com.beerme.android.util.SharedPref;

import java.util.Calendar;

public class BeerMeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // SharedPreferences singleton
        SharedPref.init(getApplicationContext());

        // Change this whenever a new database file is installed.
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