package com.beerme.android.location;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.beerme.android.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationFragment_Services extends LocationFragment implements GoogleApiClient
.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
	private LocationRequest mLocationRequest;
	private boolean mFirstLocation = true;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);

		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(0);
		mLocationRequest.setFastestInterval(0);
		mLocationRequest.setSmallestDisplacement(0);

		buildGoogleApiClient();
	}

    @Override
	public void onStart() {
        super.onStart();
        
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            Toast.makeText(this.getActivity(), "No location", Toast.LENGTH_LONG).show();
        }
        
        onLocationChanged(mLastLocation);
        startLocationUpdates();
    }

	@Override
	public Location getLocation() {
		return Utils.isLocationServiceEnabled(getActivity()) ? mLastLocation : getDefaultLocation();
	}

	@Override
	public void onLocationChanged(Location location) {
		long timestamp = location.getTime();
		if (timestamp - mLastUpdateTime >= mTimeout) {
			mLastUpdateTime = timestamp;

//			if (mFirstLocation) {
//				mClient.removeLocationUpdates(this);
//				mLocationRequest.setInterval(mTimeout);
//				mLocationRequest.setFastestInterval(FAST_INTERVAL);
//				mLocationRequest.setSmallestDisplacement(MIN_DIST);
//				mClient.requestLocationUpdates(mLocationRequest, this);
//				mFirstLocation = false;
//			}

			mLastLocation = location;
			if (Utils.isLocationServiceEnabled(getActivity())) {
				publishLocation(mLastLocation);
			} else {
				publishLocation(getDefaultLocation());
			}
		}
	}

//	@Override
//	public void setTimeout(int timeout) {
//		if (timeout >= 0) {
//			mTimeout = timeout;
//			if (mClient.isConnected()) {
//				mLocationRequest.setInterval(mTimeout);
//				mClient.requestLocationUpdates(mLocationRequest, this);
//			}
//		}
//	}

    private synchronized void buildGoogleApiClient() {
    	mGoogleApiClient = new GoogleApiClient.Builder(getActivity(), this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    	
        createLocationRequest();
        startLocationUpdates();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(Utils.APPTAG, "LocationFragment_Services.onConnectionFailed(" + connectionResult.getErrorCode() + ")");
	}

	@Override
	public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
	}
}