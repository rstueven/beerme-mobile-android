package com.beerme.android.location;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.beerme.android.R;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;

public abstract class LocationFragment extends Fragment {
	protected static final int MIN_TIME = 10000;
	protected static final int MIN_DIST = 100;
	protected static final int UPDATE_INTERVAL = 10000;
    protected static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
	protected static final String KEY_LOCATION_UPDATES_ON = "KEY_LOCATION_UPDATES_ON";
	protected Activity mActivity;
	protected ArrayList<LocationCallbacks> mCallbacks = new ArrayList<LocationCallbacks>();
	protected int mTimeout = MIN_TIME;
	protected long mLastUpdateTime = 0;

	public interface LocationCallbacks {
		public void onLocationReceived(Location location);
	}

	public static LocationFragment getInstance(Activity activity) {
		LocationFragment frag;

		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS) {
			// Get a Location Services instance
			// http://developer.android.com/google/play-services/location.html
			frag = new LocationFragment_Services();
		} else {
			// Get an old-school instance
			// http://developer.android.com/guide/topics/location/strategies.html
			frag = new LocationFragment_Legacy();
		}

		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	public void registerListener(LocationCallbacks callback) {
		if (!mCallbacks.contains(callback)) {
			mCallbacks.add(callback);
		}
	}

	public void unregisterListener(LocationCallbacks callback) {
		if (mCallbacks.contains(callback)) {
			mCallbacks.remove(callback);
		}
	}

	protected void publishLocation(Location location) {
		for (LocationCallbacks callback : mCallbacks) {
			callback.onLocationReceived(location);
		}
	}

	protected Location getDefaultLocation() {
		Location location = new Location("default");
		SharedPreferences settings = Prefs.getSettings(mActivity);

		if (settings.getString(Prefs.KEY_DEFAULT_LOCATION, null) != null) {
			location = new Location("default");
			location.setLatitude(settings.getFloat(Prefs.KEY_DEFAULT_LATITUDE,
					0));
			location.setLongitude(settings.getFloat(
					Prefs.KEY_DEFAULT_LONGITUDE, 0));
		} else {
			ErrLog.log(mActivity, "LocationFragment.getDefaultLocation",
					null, R.string.Unknown_location);
		}

		return location;
	}

	public abstract Location getLocation();

	//public abstract void setTimeout(int timeout);
}
