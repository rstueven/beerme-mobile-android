package com.beerme.android.ui;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.beerme.android.db.BreweryListViewModel;
import com.beerme.android.util.LocationActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BreweryListActivity extends LocationActivity
        implements StatusFilterDialog.StatusFilterListener, DistanceUnitDialog.DistanceUnitListener,
        BreweryListViewAdapter.OnItemClickListener, MapOrListDialog.MapOrListListener {
    private BreweryListViewAdapter breweryListViewAdapter;
    private List<Brewery> breweryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewery_list);

        RecyclerView recyclerView = findViewById(R.id.brewery_list_view);
        breweryListViewAdapter = new BreweryListViewAdapter(this, new ArrayList<Brewery>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        recyclerView.setAdapter(breweryListViewAdapter);

        BreweryListViewModel breweryListViewModel = ViewModelProviders.of(this).get(BreweryListViewModel.class);

        breweryListViewModel.getBreweryList().observe(this, new Observer<List<Brewery>>() {
            @Override
            public void onChanged(List<Brewery> breweries) {
                breweryList = breweries;
                breweryListViewAdapter.addItems(breweryList);
            }
        });
    }

    @Override
    protected void onLocationUpdated(Location location) {
        if (location != null && breweryList != null) {
            breweryListViewAdapter.addItems(breweryList);
        } else {
            Log.w("beerme", "MainActivity.onLocationUpdated(): null location or breweryList");
        }
    }

    @Override
    public void onStatusFilterChanged(int statusFilter) {
//        Log.d("beerme", "MainActivity.onStatusFilterChanged(" + statusFilter + ")");
        if (breweryList != null) {
            breweryListViewAdapter.addItems(breweryList);
        } else {
            Log.w("beerme", "MainActivity.onStatusFilterChanged(): null breweryList");
        }
    }

    @Override
    public void onDistanceUnitChanged(int distanceUnit) {
        if (breweryList != null) {
            breweryListViewAdapter.addItems(breweryList);
        } else {
            Log.w("beerme", "MainActivity.onDistanceUnitChanged(): null breweryList");
        }
    }

    @Override
    public void onItemClick(@NonNull Brewery brewery) {
//        Log.d("beerme", "onItemClick(" + brewery.id + ")");
        Intent intent = new Intent(this, BreweryActivity.class);
        intent.putExtra("brewery", brewery);
        startActivity(intent);
    }

    @Override
    public void onMapOrListChanged(@NonNull String mapOrList) {
        Log.d("beerme", "BreweryListActivity.onMapOrListChanged(" + mapOrList + ")");
        if (mapOrList.equals(MapOrListDialog.MAP)) {
            startActivity(new Intent(this, MapActivity.class));
            this.finish();
        }
    }
}