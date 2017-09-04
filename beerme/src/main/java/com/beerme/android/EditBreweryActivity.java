package com.beerme.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.beerme.android.model.Status;

public class EditBreweryActivity extends BeerMeActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_brewery);

        final Intent intent = getIntent();
        final int breweryId = intent.getIntExtra("brewery", -1);

        final TextView titleView = (TextView) findViewById(R.id.title);
        if (breweryId > 0) {
            titleView.setText(R.string.edit_brewery_information);
        } else {
            titleView.setText(R.string.add_a_brewery);
        }

        final AutoCompleteTextView regionView = (AutoCompleteTextView) findViewById(R.id.region_view);
        final ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this, R.array.regions_array, android.R.layout.simple_spinner_item);
        regionView.setAdapter(regionAdapter);

        final Spinner statusView = (Spinner) findViewById(R.id.status_view);
        final Status.StatusAdapter statusAdapter = new Status.StatusAdapter(this);
        statusView.setAdapter(statusAdapter);

        final Spinner capUnitView = (Spinner) findViewById(R.id.capacity_unit_view);
        final ArrayAdapter<CharSequence> capUnitAdapter = ArrayAdapter.createFromResource(this, R.array.capacity_units_array, android.R.layout.simple_spinner_item);
        capUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capUnitView.setAdapter(capUnitAdapter);
    }
}