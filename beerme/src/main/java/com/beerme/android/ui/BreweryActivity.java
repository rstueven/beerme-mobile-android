package com.beerme.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.db.Beer;
import com.beerme.android.db.Brewery;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class BreweryActivity extends AppCompatActivity implements BeerListViewAdapter.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewery);

//        // TODO: DRY the actionbar stuff -> BeerMeActivity
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        Intent intent = getIntent();
        Brewery brewery = (Brewery) intent.getSerializableExtra("brewery");

        if (brewery == null) {
            throw new IllegalStateException("BreweryActivity.onCreate(): null brewery");
        }

        TextView nameView = findViewById(R.id.name_view);
        TextView addressView = findViewById(R.id.address_view);
        TextView phoneView = findViewById(R.id.phone_view);
        TextView webView = findViewById(R.id.web_view);

        nameView.setText(brewery.name);

        addressView.setText(brewery.address);
        addressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Go to brewery map.
            }
        });
        
        phoneView.setText(brewery.phone);
        phoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + ((TextView)v).getText()));
                BreweryActivity.this.startActivity(intent);
            }
        });
        
        webView.setText(brewery.web);
        webView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(((TextView)v).getText().toString()));
                BreweryActivity.this.startActivity(intent);
            }
        });

        ViewPager viewPager = findViewById(R.id.tab_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        // https://c1ctech.com/android-sliding-views-using-viewpager-with-pageradapterfragmentpageradapter/
        BreweryTabAdapter tabAdapter = new BreweryTabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(BreweryInfoFragment.getInstance(brewery), "Info");
        tabAdapter.addFragment(BeerListFragment.getInstance(brewery.id), "Beer List");
        tabAdapter.addFragment(new BreweryNotesFragment(), "Notes");
        viewPager.setAdapter(tabAdapter);
    }

    @Override
    public void onItemClick(@NonNull Beer beer) {
        Log.d("beerme", "BreweryActivity.onItemClick()");
        Log.d("beerme", beer.toString());
    }
}