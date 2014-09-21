package com.beerme.android_free.ui.tripplanner;

import java.util.HashMap;

import android.app.Activity;
import android.view.View;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.beerme.android_free.database.Brewery;
import com.beerme.android_free.prefs.BreweryStatusFilterPreference;
import com.beerme.android_free.ui.tripplanner.MessageHandler.MessageListener;
import com.beerme.android_free.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class BreweriesDisplayer implements Runnable {
	private Activity mActivity;
	private SupportMapFragment mMapFrag;
	private MessageListener mMessageCallbacks;
	private BreweriesDisplayerListener mBreweriesListener;
	private GoogleMap mMap;
	private HashMap<Long, Brewery> breweries;
	private HashMap<Long, Marker> markers = new HashMap<Long, Marker>();

	public interface BreweriesDisplayerListener {
		public void onBreweriesDisplayed(HashMap<Long, Marker> markers);
	}

	public BreweriesDisplayer(Activity activity, SupportMapFragment mapFrag,
			HashMap<Long, Brewery> breweries, BreweriesDisplayerListener breweriesListener, MessageListener messageListener) {
		this.mActivity = activity;
		this.mMapFrag = mapFrag;
		this.mMap = mMapFrag.getExtendedMap();

		mBreweriesListener = breweriesListener;
		mMessageCallbacks = messageListener;
		this.breweries = (breweries == null) ? new HashMap<Long, Brewery>()
				: breweries;
	}

	// Must run on UI thread.
	@Override
	public void run() {
		// http://stackoverflow.com/questions/14428766/at-what-time-in-the-application-lifecycle-can-should-you-use-layout-measurements
		if (mMapFrag != null) {
			View mapView = mMapFrag.getView();
			if (mapView != null) {
				mapView.post(new Runnable() {
					@Override
					public void run() {
						if (mMessageCallbacks != null) {
							mMessageCallbacks.postMessage(
									MessageHandler.BREWERIES_START,
									breweries.size(), 0);
						}

						LatLngBounds bounds = mMap.getProjection()
								.getVisibleRegion().latLngBounds;

						for (long id : breweries.keySet()) {
							if (mMessageCallbacks != null) {
								mMessageCallbacks
										.postEmptyMessage(MessageHandler.BREWERIES_INCREMENT);
							}
							Brewery b = breweries.get(id);
							LatLng latLng = new LatLng(b.getLatitude(), b
									.getLongitude());
							StringBuffer title = new StringBuffer(b.getName());
							if (b.getStatus() != BreweryStatusFilterPreference.OPEN)
								title.append(" ("
										+ Utils.breweryStatusString(mActivity,
												b.getStatus()) + ")");
							StringBuffer snippet = new StringBuffer();
							if (!b.getAddress().equals(""))
								snippet.append(b.getAddress());
							if (!b.getHours().equals(""))
								snippet.append("\n" + b.getHours());
							Marker marker = mMap.addMarker(new MarkerOptions()
									.position(latLng).title(title.toString())
									.snippet(snippet.toString()));
							markers.put(b.getId(), marker);
							bounds.including(latLng);
						}

						mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
								bounds, Utils.DEFAULT_ZOOM_PADDING));

						if (mMessageCallbacks != null) {
							mMessageCallbacks
									.postEmptyMessage(MessageHandler.BREWERIES_END);
						}

						if (mBreweriesListener != null) {
							mBreweriesListener.onBreweriesDisplayed(markers);
						}
					}
				});
			}
		}
	}
}