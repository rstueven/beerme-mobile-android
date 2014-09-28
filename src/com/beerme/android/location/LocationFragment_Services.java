package com.beerme.android.location;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.beerme.android.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationFragment_Services extends LocationFragment implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	private LocationRequest mRequest;
	private LocationClient mClient;
	private boolean mFirstLocation = true;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);

		mRequest = LocationRequest.create();
		mRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mRequest.setInterval(0);
		mRequest.setFastestInterval(0);
		mRequest.setSmallestDisplacement(0);

		mClient = new LocationClient(getActivity(), this, this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!Utils.isLocationServiceEnabled(getActivity())) {
			publishLocation(getDefaultLocation());
		}

		mClient.connect();
	}

	@Override
	public void onPause() {
		if (mClient.isConnected()) {
			mClient.removeLocationUpdates(this);
		}

		mClient.disconnect();

		super.onPause();
	}

	@Override
	public Location getLocation() {
		return Utils.isLocationServiceEnabled(getActivity()) ? mClient
				.getLastLocation() : getDefaultLocation();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e("beerme", "LocationFragment_Services.onConnectionFailed("
				+ connectionResult.getErrorCode() + ")");
	}

	@Override
	public void onConnected(Bundle dataBundle) {
		mClient.requestLocationUpdates(mRequest, this);
	}

	@Override
	public void onDisconnected() {
		mClient.removeLocationUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		long timestamp = location.getTime();
		if (timestamp - mLastUpdateTime >= mTimeout) {
			mLastUpdateTime = timestamp;

			if (mFirstLocation) {
				mClient.removeLocationUpdates(this);
				mRequest.setInterval(mTimeout);
				mRequest.setFastestInterval(FAST_INTERVAL);
				mRequest.setSmallestDisplacement(MIN_DIST);
				mClient.requestLocationUpdates(mRequest, this);
				mFirstLocation = false;
			}

			if (Utils.isLocationServiceEnabled(getActivity())) {
				publishLocation(location);
			} else {
				publishLocation(getDefaultLocation());
			}
		}
	}

	@Override
	public void setTimeout(int timeout) {
		if (timeout >= 0) {
			mTimeout = timeout;
			if (mClient.isConnected()) {
				mRequest.setInterval(mTimeout);
				mClient.requestLocationUpdates(mRequest, this);
			}
		}
	}
}