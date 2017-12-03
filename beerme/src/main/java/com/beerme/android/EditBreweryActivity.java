package com.beerme.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.beerme.android.model.Brewery;
import com.beerme.android.model.Status;

import java.util.Locale;

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

        final TextView titleView = findViewById(R.id.title);
        titleView.setText(R.string.add_a_brewery);
        final EditText nameView = findViewById(R.id.name_view);
        final EditText addressView = findViewById(R.id.address_view);
        // TODO: Split address into city + region + zip.
//        final EditText cityView = (EditText) findViewById(R.id.city_view);
//        final AutoCompleteEditText regionView = (AutoCompleteEditText) findViewById(R.id.region_view);
//        final ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this, R.array.regions_array, android.R.layout.simple_spinner_item);
//        regionView.setAdapter(regionAdapter);
//        final EditText zipView = (EditText) findViewById(R.id.zip_view);
        final EditText latView = findViewById(R.id.lat_view);
        final EditText lngView = findViewById(R.id.lng_view);
        final EditText phoneView = findViewById(R.id.phone_view);
        final Spinner statusView = findViewById(R.id.status_view);
        final Status.StatusAdapter statusAdapter = new Status.StatusAdapter(this);
        statusView.setAdapter(statusAdapter);
        // TODO: Maybe add these fields.
//        final EditText openedView = (EditText) findViewById(R.id.opened_view);
//        final EditText brewersView = (EditText) findViewById(R.id.brewers_view);
//        final EditText capacityView = (EditText) findViewById(R.id.capacity_view);
//        final Spinner capUnitView = (Spinner) findViewById(R.id.capacity_unit_view);
//        final ArrayAdapter<CharSequence> capUnitAdapter = ArrayAdapter.createFromResource(this, R.array.capacity_units_array, android.R.layout.simple_spinner_item);
//        capUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        capUnitView.setAdapter(capUnitAdapter);
        final EditText webView = findViewById(R.id.web_view);
        // TODO: And this one.
//        final EditText emailView = (EditText) findViewById(R.id.email_view);
        final CheckBox publicView = findViewById(R.id.public_view);
        final EditText hoursView = findViewById(R.id.hours_view);
        final CheckBox barView = findViewById(R.id.bar_view);
        final CheckBox beergardenView = findViewById(R.id.beergarden_view);
        final CheckBox foodView = findViewById(R.id.food_view);
        final CheckBox giftshopView = findViewById(R.id.giftshop_view);
        final CheckBox hotelView = findViewById(R.id.hotel_view);
        final CheckBox retailView = findViewById(R.id.retail_view);
        final CheckBox toursView = findViewById(R.id.tours_view);
        final CheckBox wifiView = findViewById(R.id.wifi_view);
        final EditText commentsView = findViewById(R.id.comments_view);
        final Button submitBtn = findViewById(R.id.submit_btn);

        if (brewery != null) {
            titleView.setText(R.string.edit_brewery_information);
            nameView.setText(brewery.getName());
            addressView.setText(brewery.getAddress());
            latView.setText(String.format(Locale.getDefault(), "%.6f", brewery.getLatitude()));
            lngView.setText(String.format(Locale.getDefault(), "%.6f", brewery.getLongitude()));
            phoneView.setText(brewery.getPhone());
            Log.d("beerme", "getIndex(" + brewery.getStatus() + "): " + Status.getIndex(brewery.getStatus()));
            statusView.setSelection(Status.getIndex(brewery.getStatus()));
            webView.setText(brewery.getWebForDisplay());
            hoursView.setText(brewery.getHours());
            final int services = brewery.getServices();
            publicView.setChecked(brewery.isOpen());
            barView.setChecked(brewery.hasBar());
            beergardenView.setChecked(brewery.hasBeergarden());
            foodView.setChecked(brewery.hasFood());
            giftshopView.setChecked(brewery.hasGiftshop());
            hotelView.setChecked(brewery.hasHotel());
            retailView.setChecked(brewery.hasRetail());
            toursView.setChecked(brewery.hasTours());
            wifiView.setChecked(brewery.hasInternet());
        }

        // TODO: A simple way to mass show/hide the fields.
//        publicView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
//                setVis(isChecked);
//            }
//        });
//        setVis(publicView.isChecked());
    }

//    private void setVis(final boolean vis) {
//        //        svcsRow.setVisibility(publicView.isChecked() ? View.VISIBLE : View.GONE);
//
//    }
}