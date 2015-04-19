package com.beerme.android.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import com.beerme.android.utils.Utils;

public class LocationFragment_Legacy extends LocationFragment {
	private String mProvider;
	private LocationManager mManager;
	private Criteria mCriteria;
	private boolean mFirstLocation = true;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);

		mManager = (LocationManager) mActivity
				.getSystemService(Context.LOCATION_SERVICE);

		mCriteria = new Criteria();
		mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		mCriteria.setAltitudeRequired(false);
		mCriteria.setBearingRequired(false);
		mCriteria.setCostAllowed(false);
		mCriteria.setPowerRequirement(Criteria.POWER_HIGH);
		mCriteria.setSpeedRequired(false);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!Utils.isLocationServiceEnabled(getActivity())) {
			publishLocation(getDefaultLocation());
		}

		mProvider = mManager.getBestProvider(mCriteria, true);
		publishLocation(mManager.getLastKnownLocation(mProvider));
		mManager.requestLocationUpdates(mProvider, 0, 0, mListener);
	}

	@Override
	public void onPause() {
		mManager.removeUpdates(mListener);
		mFirstLocation = true;

		super.onPause();
	}

	@Override
	public Location getLocation() {
		return Utils.isLocationServiceEnabled(getActivity()) ? mManager
				.getLastKnownLocation(mProvider) : getDefaultLocation();
	}

	private LocationListener mListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if (mFirstLocation) {
				mManager.removeUpdates(mListener);
				mManager.requestLocationUpdates(mProvider, mTimeout, MIN_DIST,
						mListener);
				mFirstLocation = false;
			}

			if (Utils.isLocationServiceEnabled(getActivity())) {
				publishLocation(location);
			} else {
				publishLocation(getDefaultLocation());
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.OUT_OF_SERVICE) {
				onProviderDisabled(provider);
			}
		}

		public void onProviderEnabled(String provider) {
			if (provider.equals(mManager.getBestProvider(mCriteria, true))) {
				mManager.removeUpdates(mListener);
				mProvider = provider;
				mFirstLocation = true;
				mManager.requestLocationUpdates(mProvider, 0, 0, mListener);
			}
		}

		public void onProviderDisabled(String provider) {
			mManager.removeUpdates(mListener);
			mProvider = mManager.getBestProvider(mCriteria, true);
			mFirstLocation = true;
			mManager.requestLocationUpdates(mProvider, 0, 0, mListener);
		}
	};

//	@Override
//	public void setTimeout(int timeout) {
//		if (timeout >= 0) {
//			mTimeout = timeout;
//			mListener.onProviderDisabled(mProvider);
//		}
//	}
}