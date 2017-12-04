package com.beerme.android;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.beerme.android.model.Brewery;
import com.beerme.android.model.Status;

import java.util.Locale;

public class EditBreweryActivity extends LocationActivity {
    Brewery brewery;
    EditText latView;
    EditText lngView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_brewery);

        final TextView titleView = findViewById(R.id.title);
        titleView.setText(R.string.add_a_brewery);
        final EditText nameView = findViewById(R.id.name_view);
        final EditText addressView = findViewById(R.id.address_view);
        latView = findViewById(R.id.lat_view);
        lngView = findViewById(R.id.lng_view);
        final EditText phoneView = findViewById(R.id.phone_view);
        final Spinner statusView = findViewById(R.id.status_view);
        final Status.StatusAdapter statusAdapter = new Status.StatusAdapter(this);
        statusView.setAdapter(statusAdapter);
        final EditText webView = findViewById(R.id.web_view);
        final CheckBox publicView = findViewById(R.id.public_view);
        final EditText hoursView = findViewById(R.id.hours_view);
        final ToggleButton barView = svcBtnSetup(R.id.bar_button);
        final ToggleButton beergardenView = svcBtnSetup(R.id.beergarden_button);
        final ToggleButton foodView = svcBtnSetup(R.id.food_button);
        final ToggleButton giftshopView = svcBtnSetup(R.id.giftshop_button);
        final ToggleButton hotelView = svcBtnSetup(R.id.hotel_button);
        final ToggleButton retailView = svcBtnSetup(R.id.retail_button);
        final ToggleButton toursView = svcBtnSetup(R.id.tours_button);
        final ToggleButton wifiView = svcBtnSetup(R.id.wifi_button);
        final EditText commentsView = findViewById(R.id.comments_view);
        final Button submitBtn = findViewById(R.id.submit_btn);

        final Intent intent = getIntent();
        final int breweryId = intent.getIntExtra("brewery", -1);

        if (breweryId > 0) {
            brewery = new Brewery(this, breweryId);
        }

        if (brewery != null) {
            titleView.setText(R.string.edit_brewery_information);
            nameView.setText(brewery.getName());
            addressView.setText(brewery.getAddress());
            latView.setText(String.format(Locale.getDefault(), "%.6f", brewery.getLatitude()));
            lngView.setText(String.format(Locale.getDefault(), "%.6f", brewery.getLongitude()));
            phoneView.setText(brewery.getPhone());
            statusView.setSelection(Status.getIndex(brewery.getStatus()));
            webView.setText(brewery.getWebForDisplay());
            publicView.setChecked(brewery.isOpen());
            hoursView.setText(brewery.getHours());
//            final int services = brewery.getServices();
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

    private ToggleButton svcBtnSetup(final int id) {
        ToggleButton btn = findViewById(id);
        btn.setChecked(false);
        btn.setOnClickListener(svcBtnListener);

        return btn;
    }

    private View.OnClickListener svcBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("beerme", ((ToggleButton) view).isChecked() ? "ON" : "OFF");
        }
    };

    public void useMyLocation(final View view) {
        Location location = mCurrentLocation;
        latView.setText(String.format(Locale.getDefault(), "%.6f", location.getLatitude()));
        lngView.setText(String.format(Locale.getDefault(), "%.6f", location.getLongitude()));
    }
}