package com.beerme.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

        // TODO: Download database updates in a background Thread.
        final Intent mDbUpdate = new Intent(this, DBUpdateService.class);
        startService(mDbUpdate);

        // TODO: Ask for permission(s) here?

        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}