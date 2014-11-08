package com.beerme.android.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.beerme.android.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends AbstractActionBarActivity {
	private GoogleMap mMap;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setupMap();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupMap();
	}

	private void setupMap() {
		if (mMap == null) {
			FragmentManager mgr = getSupportFragmentManager();
			SupportMapFragment frag = (SupportMapFragment) mgr.findFragmentById(R.id.map);

			mMap = frag.getMap();

			if (mMap != null) {
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				mMap.setIndoorEnabled(false);
				mMap.setLocationSource(null);
				mMap.setMyLocationEnabled(true);
			} else {
				FragmentTransaction trans = mgr.beginTransaction();
				trans.replace(R.id.map, frag);
				trans.commit();
			}
		}
	}
}