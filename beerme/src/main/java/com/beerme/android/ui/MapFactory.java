package com.beerme.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.beerme.android.R;
import com.beerme.android.database.Brewery;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

public class MapFactory {
	private MapFactory() {
	}

	public static Intent newMapIntent(Activity activity) {
		return newIntent(activity, Double.MAX_VALUE, Double.MAX_VALUE);
	}

	public static Intent newIntent(Activity activity, Brewery brewery) {
		double lat = brewery.getLatitude();
		double lng = brewery.getLongitude();
		String appQuery = "geo:0,0?q=" + brewery.getAddress();
		String urlQuery = "http://maps.google.com/?q=" + brewery.getAddress();
		return newIntent(activity, lat, lng, appQuery, urlQuery);
	}

	public static Intent newIntent(Activity activity, double lat, double lng) {
		String appQuery = "geo:" + lat + "," + lng;
		String urlQuery = "http://maps.google.com/?q=" + lat + "," + lng;
		return newIntent(activity, lat, lng, appQuery, urlQuery);
	}

	public static Intent newIntent(Activity activity, LatLng latLng) {
		return newIntent(activity, latLng.latitude, latLng.longitude);
	}

	private static Intent newIntent(Activity activity, double lat, double lng, String appQuery, String urlQuery) {
		Intent intent = new Intent();
		boolean servicesAvailable = Utils.checkPlayServices(activity);
		int openGLVersion = Utils.checkOpenGLVersion(activity);

		if (servicesAvailable && (openGLVersion >= 2)) {
//			intent.setClass(activity, BeerMeMapActivity.class);
			intent.setClass(activity, MainActivity.class);
			intent.putExtra(MainActivity.LAT_KEY, lat);
			intent.putExtra(MainActivity.LNG_KEY, lng);
			return intent;
		} else {
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(appQuery));
			if (intent.resolveActivity(activity.getPackageManager()) != null) {
				return intent;
			} else {
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(urlQuery));
				if (intent.resolveActivity(activity.getPackageManager()) != null) {
					return intent;
				} else {
					ErrLog.log(activity, "MapFactory.returnIntent()", null, activity.getString(R.string.Cant_create_map));
					return null;
				}
			}
		}
	}
}