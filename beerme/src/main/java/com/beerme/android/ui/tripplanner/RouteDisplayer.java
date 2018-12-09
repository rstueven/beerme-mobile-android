package com.beerme.android.ui.tripplanner;

import android.util.Log;
import android.view.View;

import com.beerme.android.ui.tripplanner.directions.Route;
import com.beerme.android.ui.tripplanner.directions.Segment;
import com.beerme.android.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

public class RouteDisplayer implements Runnable {
	private SupportMapFragment mMapFrag;
	private Route route;
	private Segment overview;
	private int distance;
	private GoogleMap mMap;

	public RouteDisplayer(SupportMapFragment mMapFrag, Route route,
			Segment overview, int distance) {
		this.mMapFrag = mMapFrag;
		mMapFrag.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				mMap = googleMap;
			}
		});
		this.route = route;
		this.overview = overview;
		this.distance = distance;
	}

	// Must run on UI thread.
	@Override
	public void run() {
		// http://stackoverflow.com/questions/14428766/at-what-time-in-the-application-lifecycle-can-should-you-use-layout-measurements
		if (mMapFrag != null) {
			View v = mMapFrag.getView();
			if (v != null) {
				v.post(new Runnable() {
					@Override
					public void run() {
						LatLngBounds mapBounds = Utils.expandBounds(
								mMapFrag.getActivity(), route.getBounds(),
								distance);
						CameraUpdate cameraUpdate = CameraUpdateFactory
								.newLatLngBounds(mapBounds,
										Utils.DEFAULT_ZOOM_PADDING);
						if (mMap != null) {
							mMap.animateCamera(cameraUpdate);

							mMap.clear();

							mMap.addPolyline(new PolylineOptions()
									.addAll(overview));
						} else {
							Log.w(Utils.APPTAG,
									"RouteDisplayer: null mMapFrag.getMap()");
						}
					}
				});
			} else {
				Log.w(Utils.APPTAG, "RouteDisplayer: null mMapFrag.getView()");
			}
		} else {
			Log.w(Utils.APPTAG, "RouteDisplayer: null mMapFrag");
		}
	}
}