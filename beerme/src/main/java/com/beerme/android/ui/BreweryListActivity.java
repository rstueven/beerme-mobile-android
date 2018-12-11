package com.beerme.android.ui;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.beerme.android.R;
import com.beerme.android.location.LocationFragment;
import com.beerme.android.ui.actionbar.BeerMeActionBarActivity;
import com.beerme.android.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

public class BreweryListActivity extends BeerMeActionBarActivity implements LocationFragment.LocationListener {
	private static final String TAG_BREWERYLIST_FRAG = "brewerylistFrag";
	public static final String LAT_KEY = "latitude";
	public static final String LNG_KEY = "longitude";
	private BreweryListFrag mBreweryListFrag = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.brewerylist_activity);

		Intent intent = getIntent();
		double lat = intent.getDoubleExtra(LAT_KEY, Double.MAX_VALUE);
		double lng = intent.getDoubleExtra(LNG_KEY, Double.MAX_VALUE);
		if (lat != Double.MAX_VALUE && lng != Double.MAX_VALUE) {
			mBreweryListFrag = BreweryListFrag.getInstance(new LatLng(lat, lng));
		} else {
			mBreweryListFrag = BreweryListFrag.getInstance();
		}

		FragmentManager fragMgr = getSupportFragmentManager();
		FragmentTransaction trans = fragMgr.beginTransaction();

		if (null != findViewById(R.id.brewerylist_frame)) {
			if (null == fragMgr.findFragmentByTag(TAG_BREWERYLIST_FRAG)) {
				trans.add(R.id.brewerylist_frame, mBreweryListFrag,
						TAG_BREWERYLIST_FRAG);
			}
		}

		trans.commit();
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.trackActivityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Utils.trackActivityStop(this);
	}

	@Override
	public void onLocationUpdated(Location location) {
		if (mBreweryListFrag != null) {
            mBreweryListFrag.onLocationUpdated(location);
        }
	}
}