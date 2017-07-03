package com.beerme.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

        Log.d("beerme", "onCreate(" + id + ")");

        final Brewery brewery = new Brewery(this, id);

        final TextView nameView = (TextView) findViewById(R.id.name);
        nameView.setText(brewery.getName());

        final TextView addressView = (TextView) findViewById(R.id.address);
        addressView.setText(brewery.getAddress());
    }
}
