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

import java.util.Date;
import java.util.Locale;

import static com.beerme.android.BreweryActivity.getAppVersion;
import static com.beerme.android.BreweryActivity.getPlatformVersion;

public class EditBreweryActivity extends LocationActivity {
    Brewery brewery;
    EditText nameView;
    EditText addressView;
    TextView latView;
    TextView lngView;
    EditText phoneView;
    Spinner statusView;
    EditText webView;
    CheckBox publicView;
    EditText hoursView;
    ToggleButton barView;
    ToggleButton beergardenView;
    ToggleButton foodView;
    ToggleButton giftshopView;
    ToggleButton hotelView;
    ToggleButton retailView;
    ToggleButton toursView;
    ToggleButton wifiView;
    EditText commentsView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_brewery);

        final TextView titleView = findViewById(R.id.title);
        titleView.setText(R.string.add_a_brewery);
        nameView = findViewById(R.id.name_view);
        addressView = findViewById(R.id.address_view);
        latView = findViewById(R.id.lat_view);
        lngView = findViewById(R.id.lng_view);
        phoneView = findViewById(R.id.phone_view);
        statusView = findViewById(R.id.status_view);
        final Status.StatusAdapter statusAdapter = new Status.StatusAdapter(this);
        statusView.setAdapter(statusAdapter);
        webView = findViewById(R.id.web_view);
        publicView = findViewById(R.id.public_view);
        hoursView = findViewById(R.id.hours_view);
        barView = svcBtnSetup(R.id.bar_button);
        beergardenView = svcBtnSetup(R.id.beergarden_button);
        foodView = svcBtnSetup(R.id.food_button);
        giftshopView = svcBtnSetup(R.id.giftshop_button);
        hotelView = svcBtnSetup(R.id.hotel_button);
        retailView = svcBtnSetup(R.id.retail_button);
        toursView = svcBtnSetup(R.id.tours_button);
        wifiView = svcBtnSetup(R.id.wifi_button);
        commentsView = findViewById(R.id.comments_view);
        final Button submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(submit);

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
            barView.setChecked(brewery.hasBar());
            beergardenView.setChecked(brewery.hasBeergarden());
            foodView.setChecked(brewery.hasFood());
            giftshopView.setChecked(brewery.hasGiftshop());
            hotelView.setChecked(brewery.hasHotel());
            retailView.setChecked(brewery.hasRetail());
            toursView.setChecked(brewery.hasTours());
            wifiView.setChecked(brewery.hasInternet());
        }
    }

    private ToggleButton svcBtnSetup(final int id) {
        ToggleButton btn = findViewById(id);
        btn.setChecked(false);

        return btn;
    }

    private View.OnClickListener submit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("beerme", "SUBMIT");
            String s;
            boolean isNew = (brewery == null);

            StringBuilder body = new StringBuilder("Beer Me! Brewery Update\n");

            Date now = new Date();
            body.append("Submitted: " + now.toString() + "\n");
            body.append("Referer: " + getAppVersion() + " (" + getPlatformVersion() + ")\n");
            body.append("Brewery ID: " + (isNew ? "-1" : brewery.getId()) + "\n");

            s = nameView.getText().toString();
            if (isNew || !brewery.getName().equals(s)) {
                body.append("Brewery Name: " + s + "\n");
            }

            s = addressView.getText().toString();
            if (isNew || !brewery.getAddress().equals(s)) {
                body.append("Address: " + s + "\n");
            }

            s = latView.getText().toString();
            if (isNew || Double.toString(brewery.getLatitude()).equals(s)) {
                body.append("Latitude: " + s + "\n");
            }

            s = lngView.getText().toString();
            if (isNew || Double.toString(brewery.getLongitude()).equals(s)) {
                body.append("Longitude: " + s + "\n");
            }

            s = phoneView.getText().toString();
            if (isNew || !brewery.getPhone().equals(s)) {
                body.append("Telephone: " + s + "\n");
            }

            s = statusView.getSelectedItem().toString();
            if (isNew || !Status.STATUS.get(brewery.getStatus()).equals(s)) {
                body.append("Status: " + s + "\n");
            }

            s = webView.getText().toString();
            if (isNew || !brewery.getWebForDisplay().equals(s)) {
                body.append("Web Site(s): " + s + "\n");
            }

            boolean b;

            b = publicView.isChecked();
            if (isNew || brewery.isOpen() != b) {
                body.append("Open to the public: " + (b ? "Yes" : "No") + "\n");
            }

            s = hoursView.getText().toString();
            if (isNew || !brewery.getHours().equals(s)) {
                body.append("Hours of Operation: " + s + "\n");
            }

            b = barView.isChecked();
            if (isNew || brewery.hasBar() != b) {
                body.append("Bar: " + (b ? "Yes" : "No") + "\n");
            }

            b = beergardenView.isChecked();
            if (isNew || brewery.hasBeergarden() != b) {
                body.append("Beer Garden: " + (b ? "Yes" : "No") + "\n");
            }

            b = foodView.isChecked();
            if (isNew || brewery.hasFood() != b) {
                body.append("Food: " + (b ? "Yes" : "No") + "\n");
            }

            b = giftshopView.isChecked();
            if (isNew || brewery.hasGiftshop() != b) {
                body.append("Gift Shop: " + (b ? "Yes" : "No") + "\n");
            }

            b = hotelView.isChecked();
            if (isNew || brewery.hasHotel() != b) {
                body.append("Hotel: " + (b ? "Yes" : "No") + "\n");
            }

            b = retailView.isChecked();
            if (isNew || brewery.hasRetail() != b) {
                body.append("Retail: " + (b ? "Yes" : "No") + "\n");
            }

            b = toursView.isChecked();
            if (isNew || brewery.hasTours() != b) {
                body.append("Tours: " + (b ? "Yes" : "No") + "\n");
            }

            b = wifiView.isChecked();
            if (isNew || brewery.hasInternet() != b) {
                body.append("Wifi: " + (b ? "Yes" : "No") + "\n");
            }

            s = commentsView.getText().toString();
            if (!"".equals(s)) {
                body.append("Comments: " + s + "\n");
            }

            Log.d("beerme", body.toString());

            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "beermeupdate@gmail.com" });
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Beer Me! Brewery Update");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body.toString());
            startActivity(Intent.createChooser(emailIntent, "Send update to Beer Me!™"));
            EditBreweryActivity.this.finish();
        }
    };

    public void useMyLocation(final View view) {
        Location location = mCurrentLocation;
        latView.setText(String.format(Locale.getDefault(), "%.6f", location.getLatitude()));
        lngView.setText(String.format(Locale.getDefault(), "%.6f", location.getLongitude()));
    }
}