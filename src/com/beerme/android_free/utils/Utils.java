/**
 * 
 */
package com.beerme.android_free.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.beerme.android_free.R;
import com.beerme.android_free.prefs.BreweryStatusFilterPreference;
import com.beerme.android_free.prefs.Prefs;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * @author rstueven
 * 
 */
public class Utils {
	public static final boolean FREE_VERSION = true;
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_LOCAL_NET = false;
	public static final String APPTAG = "beerme";
	private static String appVersion = "-";
	private static String platformVersion = Build.VERSION.RELEASE;
	public static final String DISTANT_PAST = "1970-01-01";
	public static final String BEERME_URL = "http://"
			+ (DEBUG_LOCAL_NET ? "beerme-local" : "beerme.com") + "/mobile/v2/";
	public static final String NEWS_URL = BEERME_URL + "news.php";
	public static final String UPDATES_URL = BEERME_URL + "updates.php";
	public static final String BEER_RATING_URL = BEERME_URL + "beerRating.php";

	public static boolean SUPPORTS_ECLAIR = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR;
	public static boolean SUPPORTS_FROYO = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;
	public static boolean SUPPORTS_GINGERBREAD = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
	public static boolean SUPPORTS_HONEYCOMB = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;

	public static final int MILES = 0;
	public static final int KM = 1;
	public static final int YARDS = 2;
	public static final int METERS = 3;

	/*
	 * Services: These must match the values in BEERME_URL/beerme.php
	 */
	public static final int OPEN = 0x0001;
	public static final int BAR = 0x0002;
	public static final int BEERGARDEN = 0x0004;
	public static final int FOOD = 0x0008;
	public static final int GIFTSHOP = 0x0010;
	public static final int HOTEL = 0x0020;
	public static final int INTERNET = 0x0040;
	public static final int RETAIL = 0x0080;
	public static final int TOURS = 0x0100;

	private static final double SQRT_2 = Math.sqrt(2);
	private static final double EARTH_RADIUS = 6378100.0; // meters

	public static final int DEFAULT_ZOOM_PADDING = 20;

	private Utils() {
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni != null) {
				return ni.isConnected();
			}
		}
		return false;
	}

	public static String getPlatformVersion() {
		return platformVersion;
	}

	public static void setPlatformVersion(String v) {
		platformVersion = v;
	}

	public static String getAppVersion() {
		return appVersion;
	}

	public static void setAppVersion(Context context) {
		PackageManager packageManager = context.getPackageManager();
		String packageName = context.getPackageName();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(
					packageName, PackageManager.GET_CONFIGURATIONS);
			appVersion = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.i(Utils.APPTAG, e.getLocalizedMessage());
			appVersion = "";
		}
	}

	public static String stringify(String[] strings, String delimiter) {
		StringBuilder buf = new StringBuilder();
		int num = strings.length;

		for (int i = 0; i < num; i++) {
			if (i != 0) {
				buf.append(delimiter);
			}
			buf.append(strings[i]);
		}

		return buf.toString();
	}

	public static URL buildUrl(String file, String[] parameters)
			throws MalformedURLException {
		StringBuffer buffer = new StringBuffer(Utils.BEERME_URL + file);
		buffer.append("?appVersion=" + getAppVersion());
		buffer.append("&platformVersion=" + getPlatformVersion());

		for (int i = 0; i < parameters.length; i++) {
			buffer.append('&' + parameters[i]);
		}

		return new URL(buffer.toString());
	}

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	// http://stackoverflow.com/questions/6450709/detect-if-opengl-es-2-0-is-available-or-not
	public static int checkOpenGLVersion(Context context) {
		PackageManager packageManager = context.getPackageManager();
		FeatureInfo[] featureInfos = packageManager
				.getSystemAvailableFeatures();
		if (featureInfos != null && featureInfos.length > 0) {
			for (FeatureInfo featureInfo : featureInfos) {
				// Null feature name means this feature is the open gl es
				// version feature.
				if (featureInfo.name == null) {
					if (featureInfo.reqGlEsVersion != FeatureInfo.GL_ES_VERSION_UNDEFINED) {
						return (featureInfo.reqGlEsVersion & 0xffff0000) >> 16;
					} else {
						return 1; // Lack of property means OpenGL ES version 1
					}
				}
			}
		}
		return 1;
	}

	public static boolean checkPlayServices(Activity activity) {
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(activity);
		return (status == ConnectionResult.SUCCESS);
	}

	/*
	 * http://stackoverflow.com/questions/5263068/how-to-get-android-device-features
	 * -using-package-manager
	 */
	public final static boolean isFeatureAvailable(Context context,
			String feature) {
		final PackageManager packageManager = context.getPackageManager();
		final FeatureInfo[] featuresList = packageManager
				.getSystemAvailableFeatures();
		for (FeatureInfo f : featuresList) {
			if (f.name != null) {
				if (f.name.equals(feature)) {
					return true;
				}
			}
		}

		return false;
	}

	/*
	 * http://stackoverflow.com/questions/5968896/listing-all-extras-of-an-intent
	 */
	public static void dumpIntentExtras(Intent intent) {

		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			Set<String> keys = bundle.keySet();
			Iterator<String> iterator = keys.iterator();
			Log.e(Utils.APPTAG, "Dumping Intent start");
			while (iterator.hasNext()) {
				String key = iterator.next();
				Log.e(Utils.APPTAG, "[" + key + "=" + bundle.get(key) + "]");
			}
		}
	}

	public static Bitmap getBitmap(ImageView view) {
		try {
			return ((BitmapDrawable) view.getDrawable()).getBitmap();
		} catch (ClassCastException e) {
			return null;
		}
	}

	public static int bearingToCompass(float bearing) {
		int res = -1;

		if (bearing > 0 && bearing <= 22.5) {
			return R.string.north;
		}

		if (bearing > 22.5 && bearing <= 67.5) {
			return R.string.northeast;
		}

		if (bearing > 67.5 && bearing <= 112.5) {
			return R.string.east;
		}

		if (bearing > 112.5 && bearing <= 157.5) {
			return R.string.southeast;
		}

		if (bearing > 157.5 && bearing <= 180) {
			return R.string.south;
		}

		if (bearing > -180 && bearing <= -157.5) {
			return R.string.south;
		}

		if (bearing > -157.5 && bearing <= -112.5) {
			return R.string.southwest;
		}

		if (bearing > -112.5 && bearing <= -67.5) {
			return R.string.west;
		}

		if (bearing > -67.5 && bearing <= -22.5) {
			return R.string.northwest;
		}

		if (bearing > -22.5 && bearing <= 0) {
			return R.string.north;
		}

		return res;
	}

	public static String metersToUnits(Context context, float meters) {
		SharedPreferences prefs = Prefs.getSettings(context);
		int index = Integer.parseInt(prefs.getString(Prefs.KEY_DIST_UNIT, "0"));

		Resources res = context.getResources();
		String[] units = res.getStringArray(R.array.dist_unit_name);

		String[] conversion = res.getStringArray(R.array.dist_unit_conversion);

		return String.format(Locale.getDefault(), "%.1f %s",
				meters * Float.parseFloat(conversion[index]), units[index]);
	}

	public static float unitsToMeters(Context context, int d) {
		SharedPreferences prefs = Prefs.getSettings(context);
		int index = Integer.parseInt(prefs.getString(Prefs.KEY_DIST_UNIT, "0"));

		Resources res = context.getResources();
		String[] conversion = res.getStringArray(R.array.dist_unit_conversion);

		return d / Float.parseFloat(conversion[index]);
	}

	public static void setTextOrGone(TextView view, String text) {
		if (view != null) {
			if (text != null) {
				if (text.equals("")) {
					view.setVisibility(View.GONE);
				} else {
					view.setText(Html.fromHtml(text));
				}
			} else {
				view.setVisibility(View.GONE);
			}
		}
	}

	public static String getDistUnit(Context c, int unit) {
		switch (unit) {
		case Utils.METERS:
			return (c.getString(R.string.meters));
		case Utils.YARDS:
			return (c.getString(R.string.yards));
		case Utils.MILES:
			return (c.getString(R.string.miles));
		case Utils.KM:
			return (c.getString(R.string.kilometers));
		}
		return (c.getString(R.string.not_set));
	}

	public static String toFrac(float x) {
		float r = roundToHalf(x);
		return (int) r + (((int) r == r) ? "" : "½");
	}

	public static float roundToHalf(float x) {
		return (float) (((int) (x * 2.0)) / 2.0);
	}

	public static String formatAddress(Address address) {
		StringBuilder str = new StringBuilder();
		if (address.getThoroughfare() != null) {
			if (address.getFeatureName() != null) {
				str.append(address.getFeatureName());
				str.append(' ');
			}
			str.append(address.getThoroughfare());
		}
		if (address.getLocality() != null) {
			if (str.length() > 0) {
				str.append(", ");
			}
			str.append(address.getLocality());
		}
		if (address.getSubAdminArea() != null) {
			if (str.length() > 0) {
				str.append(", ");
			}
			str.append(address.getSubAdminArea());
		}
		if (address.getAdminArea() != null) {
			if (str.length() > 0) {
				str.append(", ");
			}
			str.append(address.getAdminArea());
		}
		if (address.getCountryName() != null) {
			if (str.length() > 0) {
				str.append(", ");
			}
			str.append(address.getCountryName());
		}

		return str.toString();
	}

	public static double distance(LatLng startPoint, LatLng endPoint) {
		Location startLocation = new Location("");
		startLocation.setLatitude(startPoint.latitude);
		startLocation.setLongitude(startPoint.longitude);

		Location endLocation = new Location("");
		endLocation.setLatitude(endPoint.latitude);
		endLocation.setLongitude(endPoint.longitude);

		return startLocation.distanceTo(endLocation);
	}

	public static LatLngBounds expandBounds(Context context, LatLngBounds orig,
			int d) {
		LatLngBounds newBounds;
		double d1 = d * SQRT_2;
		LatLng newSW = offsetPoint(orig.southwest, d1, 225);
		LatLng newNE = offsetPoint(orig.northeast, d1, 45);
		try {
			newBounds = new LatLngBounds(newSW, newNE);
		} catch (IllegalArgumentException e) {
			newBounds = orig;
			ErrLog.log(context, "Utils.expandBounds", e,
					R.string.Cant_create_map);
		}

		return newBounds;
	}

	// http://stackoverflow.com/questions/7222382/get-lat-long-given-current-point-distance-and-bearing
	public static LatLng offsetPoint(LatLng orig, double dist, int bearing) {
		double latRadians = Math.toRadians(orig.latitude);
		double lngRadians = Math.toRadians(orig.longitude);
		double bearingRadians = Math.toRadians(bearing);
		double distRadians = dist / EARTH_RADIUS;

		double lat = Math.asin(Math.sin(latRadians) * Math.cos(distRadians)
				+ Math.cos(latRadians) * Math.sin(distRadians)
				* Math.cos(bearingRadians));
		double lng = 0.0;
		if (Math.cos(lat) == 0) {
			lng = lngRadians;
		} else {
			lng = lngRadians
					+ Math.atan2(
							Math.sin(bearingRadians) * Math.sin(distRadians)
									* Math.cos(latRadians),
							Math.cos(distRadians) - Math.sin(latRadians)
									* Math.sin(lat));
		}

		return new LatLng(Math.toDegrees(lat), Math.toDegrees(lng));
	}

	public static String breweryStatusString(Context context, int status) {
		return context.getResources().getStringArray(R.array.status_value)[BreweryStatusFilterPreference
				.getIndex(status)];
	}

	public static boolean isNavigationAvailable(Activity activity) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("google.navigation:q=41.25,-96"));
		return (intent.resolveActivity(activity.getPackageManager()) != null);
	}

	public static void trackFragment(Fragment fragment) {
		if (!DEBUG) {
			Activity activity = fragment.getActivity();
			EasyTracker tracker = EasyTracker.getInstance(activity);
			tracker.set(Fields.SCREEN_NAME, activity.getClass().getName());
			tracker.send(MapBuilder.createAppView().build());
		}
	}

	public static void trackActivityStart(Activity activity) {
		if (!DEBUG) {
			EasyTracker.getInstance(activity).activityStart(activity);
		}
	}

	public static void trackActivityStop(Activity activity) {
		if (!DEBUG) {
			EasyTracker.getInstance(activity).activityStop(activity);
		}
	}

	public static float stars(float score) {
		return (score <= 0) ? 0 : (float) Math.max(
				(Math.floor(score - 10.5) / 2) + 0.5, 0.5);
	}

	@SuppressLint("InlinedApi")
	public static boolean isLocationServiceEnabled(Context context) {
		LocationManager lm = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		String provider = lm.getBestProvider(new Criteria(), true);
		if (SUPPORTS_FROYO) {
			return (!(provider == null || LocationManager.PASSIVE_PROVIDER
					.equals(provider)));
		} else {
			return (provider != null);
		}
	}
}