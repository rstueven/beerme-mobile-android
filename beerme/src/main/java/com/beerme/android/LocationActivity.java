package com.beerme.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

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

/**
 * Created by rstueven on 3/14/17.
 * <p/>
 * Superclass for location-aware activities.
 */

public class LocationActivity extends BeerMeActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    protected Location mCurrentLocation;
    protected boolean mRequestingLocationUpdates = true;
    private LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    private static final int PERM_ACCESS_FINE_LOCATION = 1;
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES";
    private static final String KEY_LOCATION = "KEY_LOCATION";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES, true);
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
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
                            status.startResolutionForResult(LocationActivity.this, 0x1);
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
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
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
    public void onConnected(@Nullable final Bundle connectionHint) {
        if (hasLocationPermission()) {
            //noinspection MissingPermission
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            final Activity activity = this;
            final View layout = activity.findViewById(android.R.id.content);
            Snackbar.make(layout,
                    R.string.location_permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            // Request the permission again.
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERM_ACCESS_FINE_LOCATION);
                        }
                    }).show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERM_ACCESS_FINE_LOCATION);
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        switch (requestCode) {
            case PERM_ACCESS_FINE_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if (mRequestingLocationUpdates) {
                        startLocationUpdates();
                    }
//                } else {
//                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
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
        Log.i("beerme", "LOCATION: " + location);
    }

    protected boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("MissingPermission")
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private LocationRequest createLocationRequest() {
        final LocationRequest lr = new LocationRequest();
        lr.setInterval(10000);
        lr.setFastestInterval(5000);
        lr.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        return lr;
    }
}