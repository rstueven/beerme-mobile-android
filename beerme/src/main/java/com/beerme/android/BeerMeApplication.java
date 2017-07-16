package com.beerme.android;

import android.app.Application;

/**
 * Created by rstueven on 7/16/17.
 * <p/>
 * Global application stuff and things.
 */

public class BeerMeApplication extends Application {
    private static BeerMeApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        //noinspection AssignmentToStaticFieldFromInstanceMethod
        instance = this;
    }

    public static synchronized BeerMeApplication getInstance() {
        return instance;
    }
}