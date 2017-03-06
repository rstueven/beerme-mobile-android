package com.beerme.android;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by rstueven on 3/5/17.
 * <p/>
 * Settings UI.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}