package com.beerme.android_free.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.beerme.android_free.R;
import com.beerme.android_free.ui.actionbar.BeerMeActionBarActivity;
import com.beerme.android_free.utils.Utils;

public class BeerMeMapActivity extends BeerMeActionBarActivity {
	public static final String LAT_KEY = "latitude";
	public static final String LNG_KEY = "longitude";
	private static final String TAG_MAP_FRAG = "mapFrag";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);

		Intent intent = getIntent();
		double lat = intent.getDoubleExtra(LAT_KEY, Double.MAX_VALUE);
		double lng = intent.getDoubleExtra(LNG_KEY, Double.MAX_VALUE);
		BeerMeMapFragment mapFrag = BeerMeMapFragment.getInstance(lat, lng);

		if (null != findViewById(R.id.mapActivityFrame)) {
			FragmentManager mFragMgr = getSupportFragmentManager();
			FragmentTransaction trans = mFragMgr.beginTransaction();

			if (null == mFragMgr.findFragmentByTag(TAG_MAP_FRAG)) {
				trans.add(R.id.mapActivityFrame, mapFrag, TAG_MAP_FRAG);
			}

			trans.commit();
		}
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
}