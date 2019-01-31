package com.beerme.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BreweryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewery);

        // TODO: DRY the actionbar stuff -> BeerMeActivity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        Brewery brewery = (Brewery) intent.getSerializableExtra("brewery");

        if (brewery == null) {
            throw new IllegalStateException("BreweryActivity.onCreate(): null brewery");
        }

        TextView nameView = findViewById(R.id.name_view);
        TextView addressView = findViewById(R.id.address_view);
        TextView hoursView = findViewById(R.id.hours_view);

        nameView.setText(brewery.name);
        addressView.setText(brewery.address);
        hoursView.setText(brewery.address);
    }
}