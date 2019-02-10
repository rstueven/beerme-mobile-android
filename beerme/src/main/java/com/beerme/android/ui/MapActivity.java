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
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class MapActivity extends LocationActivity
        implements OnMapReadyCallback, MapOrListDialog.MapOrListListener,
        StatusFilterDialog.StatusFilterListener {
    private GoogleMap mMap;
    private ClusterManager<MarkerItem> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            throw new IllegalStateException("MapActivity.onCreate(): null mapFragment");
        }

        mapFragment.getMapAsync(this);
    }

    // If you've got this far, you already have the location permission.
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

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
                MarkerItem item;

                for (Brewery brewery : breweries) {
                    if ((brewery.status & statusFilter) != 0) {
                        item = new MarkerItem(brewery.latitude, brewery.longitude, brewery.name, brewery.address);
                        mClusterManager.addItem(item);
                    }
                }

                mClusterManager.cluster();
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
        mMap.clear();
        mClusterManager.clearItems();
        loadBreweryMarkers(statusFilter);
    }

    public class MarkerItem implements ClusterItem {
        private final LatLng mPosition;
        private final String mTitle;
        private final String mSnippet;

        MarkerItem(double lat, double lng, String title, String snippet) {
            mPosition = new LatLng(lat, lng);
            mTitle = title;
            mSnippet = snippet;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public String getTitle() {
            return mTitle;
        }

        @Override
        public String getSnippet() {
            return mSnippet;
        }
    }
}