package com.beerme.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class BreweryActivity extends AppCompatActivity {

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

        nameView.setText(brewery.name);
        addressView.setText(brewery.address);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.tab_pager);
        tabLayout.setupWithViewPager(viewPager);

        // https://c1ctech.com/android-sliding-views-using-viewpager-with-pageradapterfragmentpageradapter/
        BreweryTabAdapter tabAdapter = new BreweryTabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(BreweryInfoFragment.getInstance(brewery), "Info");
        tabAdapter.addFragment(new BeerListFragment(), "Beer List");
        tabAdapter.addFragment(new BreweryNotesFragment(), "Notes");
        viewPager.setAdapter(tabAdapter);
    }
}