package com.beerme.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.beerme.android.model.Beer;
import com.beerme.android.model.Brewery;

import java.util.Date;

import static com.beerme.android.BreweryActivity.getAppVersion;
import static com.beerme.android.BreweryActivity.getPlatformVersion;

/**
 * Created by rstueven on 1/28/18.
 * <p>
 *     Add/Edit beer information.
 * </p>
 */

public class EditBeerActivity extends BeerMeActivity {
    Beer beer;
    int breweryId;
    EditText nameView;
    Spinner styleView;
    EditText abvView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_beer);

        final TextView titleView = findViewById(R.id.title);
        titleView.setText(R.string.add_a_beer);
        nameView = findViewById(R.id.name_view);
        styleView = findViewById(R.id.style_view);
        abvView = findViewById(R.id.abv_view);
        final Button submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(submit);

        final Intent intent = getIntent();
        final int beerId = intent.getIntExtra("beer", -1);

        if (beerId > 0) {
            beer = new Beer(this, beerId);
            breweryId = beer.getBreweryid();
        } else {
            breweryId = intent.getIntExtra("brewery", -1);
        }

        if (beer != null) {
            titleView.setText(R.string.edit_beer_information);
            nameView.setText(beer.getName());
            styleView.setSelection(beer.getStyleId());
            abvView.setText(beer.getAbv() + "%");
        }
    }

    private View.OnClickListener submit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO: POST request that actually updates the database and returns the new ID as appropriate.
            Log.d("beerme", "SUBMIT");
            String s;
            boolean isNew = (beer == null);

            StringBuilder body = new StringBuilder("Beer Me! Beer Update\n");
            Date now = new Date();
            body.append("Submitted: " + now.toString() + "\n");
            body.append("Referer: " + getAppVersion() + " (" + getPlatformVersion() + ")\n");
            body.append("Brewery ID: " + breweryId + "\n");
            body.append("Beer ID: " + (isNew ? "-1" : beer.getId()) + "\n");

            s = nameView.getText().toString();
            if (isNew || !beer.getName().equals(s)) {
                body.append("Beer Name: " + s + "\n");
            }

//            s = styleView.getSelectedItem();

            Double d = Double.parseDouble(abvView.getText().toString());
            if (isNew || beer.getAbv() != d) {
                body.append("ABV: " + s + "\n");
            }
        }
    };
}