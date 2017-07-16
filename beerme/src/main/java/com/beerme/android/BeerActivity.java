package com.beerme.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.beerme.android.model.Beer;

public class BeerActivity extends BeerMeActivity {
    int beerId = -1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer);

        final Intent intent = getIntent();
        beerId = intent.getIntExtra("id", -1);

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
    }

}
