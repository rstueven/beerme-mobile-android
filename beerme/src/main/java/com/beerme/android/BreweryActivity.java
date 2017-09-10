package com.beerme.android;

import android.animation.Animator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beerme.android.model.Brewery;
import com.beerme.android.model.Services;
import com.beerme.android.model.Status;
import com.beerme.android.util.DownloadImageTask;
import com.beerme.android.util.ImageZoomer;

public class BreweryActivity extends BeerMeActivity {
    int id = -1;
    Brewery brewery;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewery);

        final Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);
        if (id <= 0) {
            throw new IllegalArgumentException("invalid brewery ID: " + id);
        }

        try {
            brewery = new Brewery(this, id);
            final int status = brewery.getStatus();

            final TextView nameView = (TextView) findViewById(R.id.name);
            nameView.setText(brewery.getName());

            final TextView addressView = (TextView) findViewById(R.id.address);
            final String address = brewery.getAddress();
            addressView.setText(address);
            if ((address == null) || address.isEmpty()) {
                addressView.setVisibility(View.GONE);
            } else {
                addressView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final Intent intent = new Intent(BreweryActivity.this, MainActivity.class);
                        intent.putExtra("latitude", brewery.getLatitude());
                        intent.putExtra("longitude", brewery.getLongitude());
                        startActivity(intent);
                    }
                });
            }

            final TextView hoursView = (TextView) findViewById(R.id.hours);
            final String hours = brewery.getHours();
            hoursView.setText(hours);
            if ((hours == null) || hours.isEmpty() || (status == Status.CLOSED)) {
                hoursView.setVisibility(View.GONE);
            }

            final TableLayout svcView = Services.serviceView(this, brewery.getServices());
            ((FrameLayout) findViewById(R.id.services)).addView(svcView);
            if (status == Status.CLOSED) {
                svcView.setVisibility(View.GONE);
            }

            final TextView phoneView = (TextView) findViewById(R.id.phone);
            final String phone = brewery.getPhone();
            if ((phone == null) || phone.isEmpty() || (status == Status.CLOSED)) {
                phoneView.setVisibility(View.GONE);
            } else {
                phoneView.setText(phone);
                phoneView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phone));
                        startActivity(intent);
                    }
                });
            }

            final TextView webView = (TextView) findViewById(R.id.web);
            final String web = brewery.getWebForDisplay();
            if ((web == null) || web.isEmpty() || (status == Status.CLOSED)) {
                webView.setVisibility(View.GONE);
            } else {
                webView.setText(web);
                webView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(brewery.getWeb()));
                        startActivity(intent);
                    }
                });
            }

            final TextView beerListView = (TextView) findViewById(R.id.beerlist);
            beerListView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Intent intent = new Intent(BreweryActivity.this, BeerListActivity.class);
                    intent.putExtra("id", brewery.getId());
                    intent.putExtra("name", brewery.getName());
                    startActivity(intent);
                }
            });

            final ImageView imageView = (ImageView) findViewById(R.id.image);
            final String breweryImage = brewery.getImage();
            if ((breweryImage == null) || breweryImage.isEmpty()) {
                imageView.setVisibility(View.GONE);
            } else {
                final StringBuilder urlBuilder = new StringBuilder("http://beerme.com/graphics/brewery/" + (brewery.getId() / 1000) + "/" + brewery.getId() + "/");
                switch (breweryImage.charAt(0)) {
                    case 'P':
                        // Premises
                        urlBuilder.append("premises.png");
                        break;
                    case 'G':
                        // Generic
                        urlBuilder.append("generic.png");
                        break;
                    default:
                        urlBuilder.delete(0, urlBuilder.length());
                        imageView.setVisibility(View.GONE);
                }

                if (urlBuilder.length() > 0) {
                    final String imageUrl = urlBuilder.toString();
                    new DownloadImageTask(imageView).execute(imageUrl);
                }


                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        ImageZoomer.zoomImageFromThumb(BreweryActivity.this, imageView, (ImageView) findViewById(R.id.image_zoomed));
                    }
                });
            }
        } catch (final IllegalArgumentException e) {
            Toast.makeText(this, "Database error: Illegal brewery ID " + id, Toast.LENGTH_LONG).show();
            Log.e("beerme", e.getLocalizedMessage());
            this.finish();
        }
    }

    public void editBrewery(final View v) {
        final Intent intent = new Intent(this, EditBreweryActivity.class);
        intent.putExtra("brewery", brewery.getId());
        startActivity(intent);
    }
}