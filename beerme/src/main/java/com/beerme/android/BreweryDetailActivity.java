package com.beerme.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beerme.android.model.Brewery;

public class BreweryDetailActivity extends AppCompatActivity {
    int id = -1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewery_detail);

        final Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);
        if (id <= 0) {
            throw new IllegalArgumentException("invalid brewery ID: " + id);
        }

        try {
            final Brewery brewery = new Brewery(this, id);

            final TextView nameView = (TextView) findViewById(R.id.name);
            nameView.setText(brewery.getName());

            final TextView addressView = (TextView) findViewById(R.id.address);
            final String address = brewery.getAddress();
            addressView.setText(address);
            if ((address == null) || address.isEmpty()) {
                addressView.setVisibility(View.GONE);
            }

            final TextView phoneView = (TextView) findViewById(R.id.phone);
            final String phone = brewery.getPhone();
            phoneView.setText(phone);
            if ((phone == null) || phone.isEmpty()) {
                phoneView.setVisibility(View.GONE);
            }

            final TextView webView = (TextView) findViewById(R.id.web);
            final String web = brewery.getWebForDisplay();
            webView.setText(web);
            if ((web == null) || web.isEmpty()) {
                webView.setVisibility(View.GONE);
            }
        } catch (final IllegalArgumentException e) {
            Toast.makeText(this, "Database error: Illegal brewery ID " + id, Toast.LENGTH_LONG).show();
            this.finish();
        }
    }
}
