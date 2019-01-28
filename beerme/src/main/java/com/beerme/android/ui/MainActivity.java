package com.beerme.android.ui;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.beerme.android.db.BreweryListViewModel;
import com.beerme.android.util.LocationActivity;
import com.beerme.android.util.ToolbarIconTinter;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends LocationActivity {
    private BreweryListViewAdapter breweryListViewAdapter;
    private List<Brewery> breweryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.brewery_list_view);
        breweryListViewAdapter = new BreweryListViewAdapter(this, new ArrayList<Brewery>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        recyclerView.setAdapter(breweryListViewAdapter);

        BreweryListViewModel breweryListViewModel = ViewModelProviders.of(this).get(BreweryListViewModel.class);

        breweryListViewModel.getBreweryList().observe(MainActivity.this, new Observer<List<Brewery>>() {
            @Override
            public void onChanged(List<Brewery> breweries) {
                breweryList = breweries;
                breweryListViewAdapter.addItems(breweryList);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.brewerylist_menu, menu);

        ToolbarIconTinter.tintIcons(this, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.status_filter:
                Log.d("beerme", "STATUS FILTER");
                new StatusFilterDialog(this).build().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onLocationUpdated(Location location) {
        if (location != null && breweryList != null) {
            breweryListViewAdapter.addItems(breweryList);
        } else {
            Log.w("beerme", "MainActivity.onLocationUpdated(): null location or breweryList");
        }
    }
}