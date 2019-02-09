package com.beerme.android.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.beerme.android.R;
import com.beerme.android.util.SharedPref;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class LocationActivity extends BeerMeActivity {
    private static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;
    private static final int INTERVAL = 5000;
    private static final String CURRENT_LOCATION = "currentLocation";

    private FusedLocationProviderClient mFusedLocationActivity;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    public interface LocationListener {
        void onLocationUpdated(Location location);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.d("beerme", "LocationActivity(" + this.getLocalClassName() + ")");
        super.onCreate(savedInstanceState);

        updateValuesFromBundle(savedInstanceState);

        checkLocationPermission();
    }

    @Override
    protected void onPause() {
//        Log.d("beerme", "LocationActivity.onPause()");
        stopLocationUpdates();

        super.onPause();
    }

    /**
     * In case finish() gets called from onCreate()
     */
    @Override
    protected void onDestroy() {
//        Log.d("beerme", "LocationActivity.onDestroy()");
        stopLocationUpdates();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        Log.d("beerme", "LocationActivity.onSaveInstanceState()");
        outState.putParcelable(CURRENT_LOCATION, mCurrentLocation);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
//        Log.d("beerme", "LocationActivity.onResume()");
        super.onResume();

        if (SharedPref.read(SharedPref.Pref.IS_REQUESTING_LOCATION_UPDATES, false)) {
            startLocationUpdates();
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
//        Log.d("beerme", "LocationActivity.updateValuesFromBundle()");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(CURRENT_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(CURRENT_LOCATION);
            }
        }
    }

    private void checkLocationPermission() {
//        Log.d("beerme", "LocationActivity.checkLocationPermission(" + this.getLocalClassName() + ")");
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            setupLocationActivity();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                final Activity activity = this;
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{ACCESS_FINE_LOCATION},
                                        REQUEST_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
//        Log.d("beerme", "LocationActivity.onRequestPermissionsResult(" + requestCode + ")");
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                setupLocationActivity();
            }
        }
    }

    private void setupLocationActivity() {
//        Log.d("beerme", "LocationActivity.setupLocationActivity()");
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            if (mFusedLocationActivity == null) {
                mFusedLocationActivity = LocationServices.getFusedLocationProviderClient(this);
            }

            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(INTERVAL);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        mCurrentLocation = locationResult.getLastLocation();
                        onLocationUpdated(mCurrentLocation);
                    }
                }
            };

            checkCurrentLocation();
            startLocationUpdates();
        }
    }

    protected abstract void onLocationUpdated(Location location);

    public Location getLocation() {
        return mCurrentLocation;
    }

    private void checkCurrentLocation() {
//        Log.d("beerme", "LocationActivity.checkCurrentLocation()");
        if (mCurrentLocation == null) {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                loadCurrentLocation();
            }
        }
    }

    private void loadCurrentLocation() {
//        Log.d("beerme", "LocationActivity.loadCurrentLocation()");
        if (mFusedLocationActivity != null && ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mFusedLocationActivity.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    mCurrentLocation = location;
                    onLocationUpdated(location);
                }
            });
        }
    }

    private void startLocationUpdates() {
//        Log.d("beerme", "LocationActivity.startLocationUpdates()");
        if (mFusedLocationActivity != null) {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                mFusedLocationActivity.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        }

        SharedPref.write(SharedPref.Pref.IS_REQUESTING_LOCATION_UPDATES, true);
    }

    private void stopLocationUpdates() {
//        Log.d("beerme", "LocationActivity.stopLocationUpdates()");
        if (mFusedLocationActivity != null) {
            mFusedLocationActivity.removeLocationUpdates(mLocationCallback);
        }

        SharedPref.write(SharedPref.Pref.IS_REQUESTING_LOCATION_UPDATES, false);
    }
}