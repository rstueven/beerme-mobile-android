package com.beerme.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.beerme.android.model.Beer;

import java.util.Locale;

public class BeerActivity extends BeerMeActivity {
    int beerId = -1;
    String breweryName;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer);

        final Intent intent = getIntent();
        beerId = intent.getIntExtra("id", -1);
        breweryName = intent.getStringExtra("brewery_name");

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (beerId <= 0) {
            throw new IllegalArgumentException("BeerActivity: invalid beerId (" + beerId + ")");
        }

        final Beer beer = new Beer(this, beerId);

        final TextView breweryNameView = (TextView) findViewById(R.id.brewery_name);
        breweryNameView.setText(breweryName);

        final TextView nameView = (TextView) findViewById(R.id.beer_name);
        nameView.setText(beer.getName());

        final TextView styleView = (TextView) findViewById(R.id.beer_style);
        styleView.setText(beer.getStyle());

        if (beer.getAbv() > 0) {
            final TextView abvView = (TextView) findViewById(R.id.beer_abv);
            abvView.setText(String.format(Locale.getDefault(), "%.2f%% abv", beer.getAbv()));
        }
    }
}