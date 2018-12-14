package com.beerme.android.ui;

import android.app.Application;

import com.beerme.android.utils.SharedPref;

public class BeerMe extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Database singleton
//        DbOpenHelper.init(getApplicationContext());

        // SharedPreferences singleton
        SharedPref.init(getApplicationContext());

        // Volley RequestQueue singleton
//        NetworkRequestQueue.init(getApplicationContext());
    }
}