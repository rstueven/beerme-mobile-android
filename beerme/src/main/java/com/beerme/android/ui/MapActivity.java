package com.beerme.android.ui;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.beerme.android.R;
import com.beerme.android.util.LocationActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;

public class MapActivity extends LocationActivity
        implements OnMapReadyCallback, MapOrListDialog.MapOrListListener {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    protected void onLocationUpdated(Location location) {
        Log.d("beerme", "MapActivity.onLocationUpdated()");
        Log.d("beerme", location.toString());
    }

    @Override
    public void onMapOrListChanged(@NonNull String mapOrList) {
        Log.d("beerme", "MapActivity.onMapOrListChanged(" + mapOrList + ")");
        if (mapOrList.equals(MapOrListDialog.LIST)) {
            startActivity(new Intent(this, BreweryListActivity.class));
            this.finish();
        }
    }
}