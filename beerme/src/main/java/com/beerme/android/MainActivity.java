package com.beerme.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.beerme.android.db.DBHelper;
import com.beerme.android.map.Placemark;
import com.beerme.android.map.TouchableWrapper;
import com.beerme.android.model.Services;
import com.beerme.android.model.Statuses;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

public class MainActivity extends LocationActivity
        implements
        OnMapReadyCallback, TouchableWrapper.UpdateMapAfterUserInteraction,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter {
    private static final String KEY_CAMERA_POSITION = "KEY_CAMERA_POSITION";
    final DBHelper dbHelper = DBHelper.getInstance(MainActivity.this);
    final SQLiteDatabase db = dbHelper.getReadableDatabase();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    final private SparseArray<Marker> mPointsOnMap = new SparseArray<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getExtendedMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Toast.makeText(this, "SETTINGS", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(this);
        googleMap.setClustering(new ClusteringSettings().addMarkersDynamically(true).clusterSize(20));


        if (hasLocationPermission()) {
            //noinspection MissingPermission
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setMinZoomPreference(3.0f);
            mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
        }

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) {
            final LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        mCurrentLocation = location;

        if ((mCurrentLocation != null) && (mMap != null) && mRequestingLocationUpdates) {
            final LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onUpdateMapAfterUserInteraction() {
        // http://dimitar.me/how-to-detect-a-user-pantouchdrag-on-android-map-v2/
        mRequestingLocationUpdates = false;
        stopLocationUpdates();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        mRequestingLocationUpdates = true;
        startLocationUpdates();
        return false;
    }

    @Override
    public void onCameraIdle() {
        new MarkerTask(mMap.getProjection().getVisibleRegion().latLngBounds).execute();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        mRequestingLocationUpdates = false;
        stopLocationUpdates();
        return false;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        final String id = Integer.toString((int) marker.getData());
        Log.d("beerme", "ID: " + id);
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.infowindow, null);

        final String sql = "SELECT name, address, status, hours, services FROM brewery WHERE _id = ?";
        final Cursor c = db.rawQuery(sql, new String[]{id});

        if (c.getCount() == 1) {
            c.moveToFirst();
            final TextView name = (TextView) view.findViewById((R.id.name));
            name.setText(c.getString(0));
            final TextView address = (TextView) view.findViewById((R.id.address));
            address.setText(c.getString(1));
            final TextView status = (TextView) view.findViewById((R.id.status));
            final String statusString = Statuses.statusString(c.getInt(2));
            if (statusString == null) {
                status.setVisibility(View.GONE);
            } else {
                status.setVisibility(View.VISIBLE);
                status.setText(statusString);
            }
            final TextView hours = (TextView) view.findViewById((R.id.hours));
            hours.setText(c.getString(3));
            final TextView services = (TextView) view.findViewById((R.id.services));
            services.setText(Services.serviceString(c.getInt(4)));
        } else {
            view = null;
        }

        c.close();

        return view;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        return null;
    }

    private class MarkerTask extends AsyncTask<Void, Void, ArrayList<Placemark>> {
        final private LatLngBounds bounds;

        MarkerTask(final LatLngBounds bounds) {
            super();
            this.bounds = bounds;
        }

        @Override
        protected ArrayList<Placemark> doInBackground(final Void... params) {
            final ArrayList<Placemark> placemarks = new ArrayList<>();

            final String sql = "SELECT _id, name, latitude, longitude FROM brewery WHERE latitude BETWEEN " + bounds.southwest.latitude + " AND " + bounds.northeast.latitude + " AND longitude BETWEEN " + bounds.southwest.longitude + " AND " + bounds.northeast.longitude;
            final Cursor c = db.rawQuery(sql, null);

            int id;

            // Place markers that aren't already on the map.
            while (c.moveToNext()) {
                id = c.getInt(0);
                if (mPointsOnMap.get(id) == null) {
                    //noinspection ObjectAllocationInLoop
                    placemarks.add(new Placemark(c));
                }
            }
            c.close();

            return placemarks;
        }

        @Override
        protected void onPostExecute(final ArrayList<Placemark> placemarks) {
            final MarkerOptions options = new MarkerOptions();
            Marker marker;

            for (final Placemark p : placemarks) {
                if (mPointsOnMap.get(p.id) == null) {
                    marker = mMap.addMarker(options.title(p.name).position(p.position).data(p.id));
                    mPointsOnMap.append(p.id, marker);
                }
            }

            // Remove markers that aren't on the map anymore.
            final int n = mPointsOnMap.size();
            for (int i = 0; i < n; i++) {
                marker = mPointsOnMap.valueAt(i);
                if ((marker != null) && !bounds.contains(marker.getPosition())) {
                    mPointsOnMap.removeAt(i);
                    marker.remove();
                }
            }
        }
    }
}