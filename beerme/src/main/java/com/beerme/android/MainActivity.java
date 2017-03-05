package com.beerme.android;

import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

// TODO: LocationActivity extends FragmentActivity, MainActivity extends LocationActivity
public class MainActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback, LocationListener, TouchableWrapper.UpdateMapAfterUserInteraction,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter {
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES";
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_CAMERA_POSITION = "KEY_CAMERA_POSITION";
    final DBHelper dbHelper = DBHelper.getInstance(MainActivity.this);
    final SQLiteDatabase db = dbHelper.getReadableDatabase();
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates = true;
    private LocationRequest mLocationRequest;
    final private SparseArray<Marker> mPointsOnMap = new SparseArray<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES, true);
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mLocationRequest = createLocationRequest();

        final LocationSettingsRequest.Builder lsrBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        final PendingResult<LocationSettingsResult> lsrResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, lsrBuilder.build());

        lsrResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull final LocationSettingsResult result) {
                final Status status = result.getStatus();
//                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // Ignore.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MainActivity.this, 0x1);
                        } catch (final IntentSender.SendIntentException e) {
                            // Ignore.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Ignore.
                        break;
                    default:
                        // Ignore.
                }
            }
        });

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getExtendedMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
        super.onSaveInstanceState(savedInstanceState);
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

    private static final int PERM_ACCESS_FINE_LOCATION = 1;

    @Override
    public void onConnected(@Nullable final Bundle connectionHint) {
        if (hasLocationPermission()) {
            //noinspection MissingPermission
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Show Permission Rationale", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERM_ACCESS_FINE_LOCATION);
            }
        }
    }

    private LocationRequest createLocationRequest() {
        final LocationRequest lr = new LocationRequest();
        lr.setInterval(10000);
        lr.setFastestInterval(5000);
        lr.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        return lr;
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        switch (requestCode) {
            case PERM_ACCESS_FINE_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if (mRequestingLocationUpdates) {
                        startLocationUpdates();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Log.w("beerme", "onRequestPermissionsResult(" + requestCode + "): unknown requestCode");
        }
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.w("beerme", "onConnectionSuspended(" + i + ")");
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        Log.w("beerme", "onConnectionFailed(): " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(final Location location) {
        mCurrentLocation = location;

        if ((mCurrentLocation != null) && (mMap != null) && mRequestingLocationUpdates) {
            final LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
        final String id = Integer.toString((int)marker.getData());
        Log.d("beerme", "ID: " + id);
        final LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.infowindow, null);

        final String sql = "SELECT name, address, status, hours, services FROM brewery WHERE _id = ?";
        final Cursor c = db.rawQuery(sql, new String[] {id});

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