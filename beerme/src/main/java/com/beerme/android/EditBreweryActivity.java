package com.beerme.android;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.beerme.android.model.Brewery;
import com.beerme.android.model.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;

public class EditBreweryActivity extends LocationActivity {
    Brewery brewery;
    EditText latView;
    EditText lngView;

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
        latView = findViewById(R.id.lat_view);
        lngView = findViewById(R.id.lng_view);
        final EditText phoneView = findViewById(R.id.phone_view);
        final Spinner statusView = findViewById(R.id.status_view);
        final Status.StatusAdapter statusAdapter = new Status.StatusAdapter(this);
        statusView.setAdapter(statusAdapter);
        final EditText webView = findViewById(R.id.web_view);
        final Button publicView = findViewById(R.id.public_view);
        final EditText hoursView = findViewById(R.id.hours_view);
        final Button barView = svcBtnSetup(R.id.bar_button, brewery.hasBar());
        final Button beergardenView = svcBtnSetup(R.id.beergarden_button, brewery == null && brewery.hasBeergarden());
        final Button foodView = svcBtnSetup(R.id.food_button, brewery == null && brewery.hasFood());
        final Button giftshopView = svcBtnSetup(R.id.giftshop_button, brewery == null && brewery.hasGiftshop());
        final Button hotelView = svcBtnSetup(R.id.hotel_button, brewery == null && brewery.hasHotel());
        final Button retailView = svcBtnSetup(R.id.retail_button, brewery == null && brewery.hasRetail());
        final Button toursView = svcBtnSetup(R.id.tours_button, brewery == null && brewery.hasTours());
        final Button wifiView = svcBtnSetup(R.id.wifi_button, brewery == null && brewery.hasInternet());
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

    private Button svcBtnSetup(final int id, final boolean tag) {
        Button btn = findViewById(id);
        btn.setTag(tag);
        btn.setOnClickListener(svcBtnListener);

        return btn;
    }

    private View.OnClickListener svcBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("beerme", ((ToggleButton)view).isChecked() ? "ON" : "OFF");
            view.setTag(!(boolean)view.getTag());
        }
    };

    public void useMyLocation(final View view) {
        Location location = mCurrentLocation;
        latView.setText(String.format(Locale.getDefault(),"%.6f", location.getLatitude()));
        lngView.setText(String.format(Locale.getDefault(),"%.6f", location.getLongitude()));
    }
}