package com.beerme.android;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.beerme.android.db.DBHelper;
import com.beerme.android.db.DBUpdateService;

/**
 * Created by rstueven on 2/24/17.
 * <p/>
 * Splash screen, application initializers.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This creates the database if it hasn't already been created.
        final DBHelper dbHelper = DBHelper.getInstance(this);
        dbHelper.getReadableDatabase();

        // Download database updates in a background Thread.
        final Intent mDbUpdate = new Intent(this, DBUpdateService.class);
        startService(mDbUpdate);

        // TODO: Ask for permission(s) here?

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}