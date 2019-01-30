package com.beerme.android;

import android.app.Application;

import com.beerme.android.db.BeerMeDatabase;
import com.beerme.android.util.NetworkRequestQueue;
import com.beerme.android.util.SharedPref;

public class BeerMeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // SharedPreferences singleton
        SharedPref.init(getApplicationContext());

        // Volley RequestQueue singleton
        NetworkRequestQueue.init(getApplicationContext());

        // Database singleton
        BeerMeDatabase.init(getApplicationContext());
    }
}