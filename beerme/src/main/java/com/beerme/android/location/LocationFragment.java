package com.beerme.android.location;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.beerme.android.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class LocationFragment extends Fragment {
    private static final String PERMISSIONS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int REQUEST_FINE_LOCATION = 1;
    private static final String IS_REQUESTING_LOCATION_UPDATES = "isRequestingLocationUpdates";
    protected Activity mActivity;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;
    private boolean isRequestingLocationUpdates = false;
    private LocationCallback mLocationCallback;
    private final List<LocationListener> locationListeners = new ArrayList<>();
    private LocationRequest mLocationRequest;

    public interface LocationListener {
        void onLocationUpdated(Location location);
    }

	public static LocationFragment getInstance() {
		return new LocationFragment();
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    mActivity = getActivity();

        updateValuesFromBundle(savedInstanceState);

        checkLocationPermission();
    }

    @Override
    public void onPause() {
        Log.d("nfs", "LocationFragment.onPause()");
        super.onPause();

        // Is this necessarily true? Or should it be controlled by the Fragments?
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        Log.d("nfs", "LocationFragment.onResume()");
        super.onResume();
        if (isRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d("nfs", "LocationFragment.onSaveInstanceState()");
        outState.putBoolean(IS_REQUESTING_LOCATION_UPDATES, isRequestingLocationUpdates);

        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d("nfs", "LocationFragment.updateValuesFromBundle()");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(IS_REQUESTING_LOCATION_UPDATES)) {
                isRequestingLocationUpdates = savedInstanceState.getBoolean(IS_REQUESTING_LOCATION_UPDATES);
            }
        }
    }

//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		Utils.trackFragment(this);
//	}

    private void checkLocationPermission() {
        Log.d("nfs", "LocationFragment.checkLocationPermission()");
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(mActivity)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(mActivity, new String[] {PERMISSIONS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(mActivity, new String[] {PERMISSIONS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            }
        } else {
            setupLocationClient();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d("nfs", "LocationFragment.onRequestPermissionsResult()");
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationClient();
            }
        }
    }

    private void setupLocationClient() {
        Log.d("nfs", "LocationFragment.setupLocationClient()");
        if (mFusedLocationClient == null) {
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
            }
        }

        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(5000);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        for (Location location : locationResult.getLocations()) {
//                            Log.d("nfs", "LOCATION_RESULT: " + location.toString());
                            mCurrentLocation = location;

                            for (LocationListener listener : locationListeners) {
                                listener.onLocationUpdated(location);
                            }
                        }
                    }
                }
            };

            checkCurrentLocation();
            startLocationUpdates();
        }
    }

    public void registerLocationListener(LocationListener listener) {
        if (listener != null && !locationListeners.contains(listener)) {
            locationListeners.add(listener);
            isRequestingLocationUpdates = true;
            startLocationUpdates();
        }
    }

    protected void unRegisterLocationListener(LocationListener listener) {
        if (listener != null) {
            locationListeners.remove(listener);
            if (locationListeners.size() == 0) {
                isRequestingLocationUpdates = false;
                stopLocationUpdates();
            }
        }
    }

    private void startLocationUpdates() {
        if (mFusedLocationClient != null) {
            if (ContextCompat.checkSelfPermission(mActivity, PERMISSIONS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        }
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void checkCurrentLocation() {
        if (mCurrentLocation == null) {
            if (ContextCompat.checkSelfPermission(mActivity, PERMISSIONS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                loadCurrentLocation();
            }
        }
    }

    private void loadCurrentLocation() {
        if (mFusedLocationClient != null) {
            if (ContextCompat.checkSelfPermission(mActivity, PERMISSIONS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(mActivity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        mCurrentLocation = location;
                        //                onGotCurrentLocation();
                    }
                });
            }
        }
    }

	protected void publishLocation(Location location) {
		for (LocationListener callback : locationListeners) {
			callback.onLocationUpdated(location);
		}
	}

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }
}
