package com.beerme.android;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by rstueven on 3/5/17.
 * <p/>
 * Settings UI/
 */

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}