package com.beerme.android.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.beerme.android.db.BreweryListViewModel;
import com.beerme.android.util.SharedPref;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class MapActivity extends LocationActivity
        implements OnMapReadyCallback, MapOrListDialog.MapOrListListener, StatusFilterDialog.StatusFilterListener {
    private GoogleMap mMap;
    private List<Long> mapped = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the MapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            throw new IllegalStateException("MapActivity.onCreate(): null mapFragment");
        }

        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the MapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    // If you've got this far, you already have the location permission.
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        final int statusFilter = SharedPref.read(SharedPref.Pref.STATUS_FILTER, 0);
        loadBreweryMarkers(statusFilter);
    }

    private void loadBreweryMarkers(final int statusFilter) {
        Log.d("beerme", "MapActivity.loadBreweryMarkers(" + statusFilter + ")");
        BreweryListViewModel breweryListViewModel = ViewModelProviders.of(this).get(BreweryListViewModel.class);

        breweryListViewModel.getBreweryList().observe(this, new Observer<List<Brewery>>() {
            @Override
            public void onChanged(List<Brewery> breweries) {
                Log.d("beerme", "MapActivity.observe()");
                LatLng latLng;

                for (Brewery brewery : breweries) {
                    if (!mapped.contains(brewery.id) && ((brewery.status & statusFilter) != 0)) {
                        mapped.add(brewery.id);
                        latLng = new LatLng(brewery.latitude, brewery.longitude);
                        mMap.addMarker(new MarkerOptions().position(latLng).title(brewery.name));
                    }
                }
            }
        });
    }

    @Override
    protected void onLocationUpdated(Location location) {
//        Log.d("beerme", "MapActivity.onLocationUpdated()");
//        if (location != null && mMap != null) {
//            Log.d("beerme", location.toString());
//            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
//        }
    }

    @Override
    public void onMapOrListChanged(@NonNull String mapOrList) {
        Log.d("beerme", "MapActivity.onMapOrListChanged(" + mapOrList + ")");
        if (mapOrList.equals(MapOrListDialog.LIST)) {
            startActivity(new Intent(this, BreweryListActivity.class));
            this.finish();
        }
    }

    @Override
    public void onStatusFilterChanged(int statusFilter) {
        Log.d("beerme", "MapActivity.onStatusFilterChanged(" + statusFilter + ")");
        mapped.clear();
        mMap.clear();
        loadBreweryMarkers(statusFilter);
    }
}