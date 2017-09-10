package com.beerme.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.beerme.android.db.DBContract;
import com.beerme.android.map.Placemark;
import com.beerme.android.map.TouchableWrapper;
import com.beerme.android.model.Brewery;
import com.beerme.android.model.Services;
import com.beerme.android.model.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class MainActivity extends LocationActivity
        implements
        OnMapReadyCallback, TouchableWrapper.UpdateMapAfterUserInteraction,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapClickListener {
    private static final String KEY_CAMERA_POSITION = "KEY_CAMERA_POSITION";
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    final private SparseArray<Marker> mPointsOnMap = new SparseArray<>();
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;
    private List<Marker> declusterifiedMarkers;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = getIntent();
        final Double lat = intent.getDoubleExtra("latitude", Double.NaN);
        final Double lng = intent.getDoubleExtra("longitude", Double.NaN);

        if (!lat.isNaN() && !lng.isNaN()) {
            mCameraPosition = new CameraPosition(new LatLng(lat, lng), 17f, 0f, 0f);
        } else if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getExtendedMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        prefsListener = new PrefsChangeListener(mMap, mPointsOnMap);
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(prefsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (prefsListener != null) {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .unregisterOnSharedPreferenceChangeListener(prefsListener);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setMinZoomPreference(3.0f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.299699, -95.899515), 10.0f));
        mMap.setClustering(new ClusteringSettings().addMarkersDynamically(true).clusterSize(20));

        if (hasLocationPermission()) {
            //noinspection MissingPermission
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            mRequestingLocationUpdates = false;
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

        if (marker.isCluster()) {
            final List<Marker> markers = marker.getMarkers();
            boolean samePosition = true;
            final LatLng firstPosition = markers.get(0).getPosition();
            LatLng position;
            double deltaLat, deltaLng;
            final LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (final Marker m : markers) {
                position = m.getPosition();
                builder.include(position);

                if (samePosition) {
                    deltaLat = abs(position.latitude - firstPosition.latitude);
                    deltaLng = abs(position.longitude - firstPosition.longitude);
                    if ((deltaLat > 0.0001) || (deltaLng > 0.0001)) {
                        samePosition = false;
                    }
                }
            }

            final LatLngBounds bounds = builder.build();

            final boolean allTogether = samePosition;

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30), 750,
                    new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            if (allTogether) {
                                declusterify(marker);
                            }
                        }

                        @Override
                        public void onCancel() {}
                    });

            return true;
        }

        return false;
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    private void declusterify(final Marker cluster) {
        clusterifyMarkers();
        declusterifiedMarkers = cluster.getMarkers();
        final LatLng clusterPosition = cluster.getPosition();
        final double distance = calculateDistanceBetweenMarkers();
        double currentDistance = (-declusterifiedMarkers.size() / 2) * distance;

        for (final Marker marker : declusterifiedMarkers) {
            final MarkerData data = marker.getData();
            marker.setData(new MarkerData(data.position, data.id));
            marker.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
            final LatLng newPosition = new LatLng(clusterPosition.latitude, clusterPosition.longitude + currentDistance);
            marker.animatePosition(newPosition);
            currentDistance += distance;
        }
    }

    private double calculateDistanceBetweenMarkers() {
        final Projection projection = mMap.getProjection();
        final Point point = projection.toScreenLocation(new LatLng(0.0, 0.0));
        point.x += getResources().getDimensionPixelSize(R.dimen.distance_between_markers);
        final LatLng nextPosition = projection.fromScreenLocation(point);
        return nextPosition.longitude;
    }

    private void clusterifyMarkers() {
        if (declusterifiedMarkers != null) {
            for (final Marker marker : declusterifiedMarkers) {
                marker.setClusterGroup(ClusterGroup.DEFAULT);
                marker.setPosition(((MarkerData)marker.getData()).position);
            }

            declusterifiedMarkers = null;
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoWindow(final Marker marker) {
        View view;
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (marker.isCluster()) {
//            final List<Marker> markers = marker.getMarkers();
            view = inflater.inflate(R.layout.clusterwindow, null);
            final TextView txt = (TextView) view.findViewById(R.id.clusterList);

            final ArrayList<String> ids = new ArrayList<>();
            for (final Marker m : marker.getMarkers()) {
                ids.add(Integer.toString(((MarkerData)m.getData()).id));
            }

            final Cursor c = contentResolver.query(DBContract.Brewery.CONTENT_URI,
                    DBContract.Brewery.COLUMNS,
                    "_id IN (" + TextUtils.join(",", ids) + ")",
                    null,
                    "name");


            final StringBuilder txtList = new StringBuilder();
            int n = 0;
            if (c != null) {
                while (c.moveToNext() && (n < 5)) {
                    txtList.append(c.getString(0)).append('\n');
                    ++n;
                }
                if (n < c.getCount()) {
                    txtList.append("(").append(c.getCount() - n).append(" more)");
                }
                txt.setText(txtList.toString());

                c.close();
            }
        } else {
            view = inflater.inflate(R.layout.infowindow, null);

            try {
                final Brewery brewery = new Brewery(this, ((MarkerData)marker.getData()).id);
                final int status = brewery.getStatus();

                final TextView nameView = (TextView) view.findViewById((R.id.name));
                nameView.setText(brewery.getName());
                final TextView addressView = (TextView) view.findViewById((R.id.address));
                addressView.setText(brewery.getAddress());
                final TextView statusView = (TextView) view.findViewById((R.id.status));
                final String statusString = Status.statusString(brewery.getStatus());
                if (statusString == null) {
                    statusView.setVisibility(View.GONE);
                } else {
                    statusView.setVisibility(View.VISIBLE);
                    statusView.setText(statusString);
                }

                final TextView hoursView = (TextView) view.findViewById((R.id.hours));
                hoursView.setText(brewery.getHours());
                if (status == Status.CLOSED) {
                    hoursView.setVisibility(View.GONE);
                }

                final LinearLayout svcLayout = Services.serviceIcons(this, brewery.getServices());
                svcLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                ((LinearLayout) view).addView(svcLayout);
                if (status == Status.CLOSED) {
                    svcLayout.setVisibility(View.GONE);
                }
            } catch (final IllegalArgumentException e) {
                Toast.makeText(this, "Database error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                view = null;
            }
        }

        return view;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
//        Log.d("beerme", "onInfoWindowClick(" + marker.getData() + " : " + marker.getTitle() + ")");
        final Integer id = ((MarkerData)marker.getData()).id;
//        if (id != null) {
            final Intent intent = new Intent(this, BreweryActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
//        }
    }

    @Override
    public void onMapClick(final LatLng position) {
        clusterifyMarkers();
    }

    private class PrefsChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        final private SparseArray<Marker> points;
        final private GoogleMap map;

        PrefsChangeListener(final GoogleMap map, final SparseArray<Marker> points) {
            this.points = points;
            this.map = map;
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
            if (key.startsWith("status_filter_")) {
                points.clear();
                if (map != null) {
                    map.clear();
                    new MarkerTask(map.getProjection().getVisibleRegion().latLngBounds).execute();
                }
            }
        }
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

            final String[] projection = {
                    DBContract.Brewery.COLUMN_ID,
                    DBContract.Brewery.COLUMN_NAME,
                    DBContract.Brewery.COLUMN_LATITUDE,
                    DBContract.Brewery.COLUMN_LONGITUDE
            };
            final String selection = "(latitude BETWEEN ? AND ?) AND (longitude BETWEEN ? AND ?) AND (" + com.beerme.android.model.Status.statusClause(MainActivity.this) + ")";
            final String[] selectionArgs = {
                    Double.toString(bounds.southwest.latitude),
                    Double.toString(bounds.northeast.latitude),
                    Double.toString(bounds.southwest.longitude),
                    Double.toString(bounds.northeast.longitude)
            };

            final Cursor c = contentResolver.query(
                    DBContract.Brewery.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );

            int id;

            // Place markers that aren't already on the map.
            if (c != null) {
                while (c.moveToNext()) {
                    id = c.getInt(c.getColumnIndex("_id"));
                    if (mPointsOnMap.get(id) == null) {
                        //noinspection ObjectAllocationInLoop
                        placemarks.add(new Placemark(c));
                    }
                }
                c.close();
            }

            return placemarks;
        }

        @Override
        protected void onPostExecute(final ArrayList<Placemark> placemarks) {
            final MarkerOptions options = new MarkerOptions();
            Marker marker;

            for (final Placemark p : placemarks) {
                if (mPointsOnMap.get(p.id) == null) {
                    //noinspection ObjectAllocationInLoop
                    marker = mMap.addMarker(options.title(p.name).position(p.position).data(new MarkerData(p.position, p.id)));
                    mPointsOnMap.append(p.id, marker);
                }
            }

            // Remove markers that aren't on the map anymore.
            // Unless we're declusterified.
            if (declusterifiedMarkers == null) {
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

    private static class MarkerData {
        LatLng position;
        int id;

        MarkerData(final LatLng p, final int i) {
            position = p;
            id = i;
        }

        @Override
        public String toString() {
            return "id: " + id + " — position: " + position.toString();
        }
    }

    public void addBrewery(final View v) {
        final Intent intent = new Intent(this, EditBreweryActivity.class);
        startActivity(intent);
    }
}