package com.beerme.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.beerme.android.model.Brewery;
import com.beerme.android.model.Status;

public class EditBreweryActivity extends BeerMeActivity {
    Brewery brewery;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_brewery);

        final Intent intent = getIntent();
        final int breweryId = intent.getIntExtra("brewery", -1);

        if (breweryId > 0) {
            brewery = new Brewery(this, breweryId);
        }

        final TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(R.string.add_a_brewery);
        final TextView nameView = (TextView) findViewById(R.id.name_view);
        final TextView name2View = (TextView) findViewById(R.id.name2_view);
        final TextView addressView = (TextView) findViewById(R.id.address_view);
        final TextView address2View = (TextView) findViewById(R.id.address2_view);
        final TextView cityView = (TextView) findViewById(R.id.city_view);
        final AutoCompleteTextView regionView = (AutoCompleteTextView) findViewById(R.id.region_view);
        final ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this, R.array.regions_array, android.R.layout.simple_spinner_item);
        regionView.setAdapter(regionAdapter);
        final TextView zipView = (TextView) findViewById(R.id.zip_view);
        final TextView latView = (TextView) findViewById(R.id.lat_view);
        final TextView lngView = (TextView) findViewById(R.id.lng_view);
        final TextView phoneView = (TextView) findViewById(R.id.phone_view);
        final Spinner statusView = (Spinner) findViewById(R.id.status_view);
        final Status.StatusAdapter statusAdapter = new Status.StatusAdapter(this);
        statusView.setAdapter(statusAdapter);
        final TextView openedView = (TextView) findViewById(R.id.opened_view);
        final TextView brewersView = (TextView) findViewById(R.id.brewers_view);
        final TextView capacityView = (TextView) findViewById(R.id.capacity_view);
        final Spinner capUnitView = (Spinner) findViewById(R.id.capacity_unit_view);
        final ArrayAdapter<CharSequence> capUnitAdapter = ArrayAdapter.createFromResource(this, R.array.capacity_units_array, android.R.layout.simple_spinner_item);
        capUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capUnitView.setAdapter(capUnitAdapter);
        final TextView webView = (TextView) findViewById(R.id.web_view);
        final TextView emailView = (TextView) findViewById(R.id.email_view);
        final CheckBox publicView = (CheckBox) findViewById(R.id.public_view);
        final TableRow svcsRow = (TableRow) findViewById(R.id.svcs_row);
        final CheckBox barView = (CheckBox) findViewById(R.id.bar_view);
        final CheckBox beergardenView = (CheckBox) findViewById(R.id.beergarden_view);
        final CheckBox foodView = (CheckBox) findViewById(R.id.food_view);
        final CheckBox giftshopView = (CheckBox) findViewById(R.id.giftshop_view);
        final CheckBox hotelView = (CheckBox) findViewById(R.id.hotel_view);
        final CheckBox retailView = (CheckBox) findViewById(R.id.retail_view);
        final CheckBox toursView = (CheckBox) findViewById(R.id.tours_view);
        final CheckBox wifiView = (CheckBox) findViewById(R.id.wifi_view);
        final EditText commentsView = (EditText) findViewById(R.id.comments_view);
        final Button submitBtn = (Button) findViewById(R.id.submit_btn);

        if (brewery != null) {
            titleView.setText(R.string.edit_brewery_information);
        }
    }
}