package com.beerme.android.utils;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class Utils {
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_LOCAL_NET = false;
	public static final String APPTAG = "beerme";
	public static final boolean FREE_VERSION = Utils.class.getPackage().getName().contains(".android_free.");
	public static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	public static final int REQUEST_CODE_WIFI_SETTINGS = 1002;
	public static boolean SUPPORTS_ECLAIR = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR;
	public static boolean SUPPORTS_FROYO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	public static boolean SUPPORTS_GINGERBREAD = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	public static boolean SUPPORTS_HONEYCOMB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	private static String platformVersion = Build.VERSION.RELEASE;
	public static final String BEERME_URL = "http://"
			+ (DEBUG_LOCAL_NET ? "beerme-local" : "beerme.com") + "/mobile/v3/";
	public static final String DISTANT_PAST = "1970-01-01";

	private static String appVersion = "-";

	private Utils() {
	}

	public static String getAppVersion() {
		return appVersion;
	}

	public static void setAppVersion(Context context) {
		PackageManager packageManager = context.getPackageManager();
		String packageName = context.getPackageName();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
			appVersion = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.i(Utils.APPTAG, e.getLocalizedMessage());
			appVersion = "";
		}
	}

	public static String getPlatformVersion() {
		return platformVersion;
	}

	public static void setPlatformVersion(String v) {
		platformVersion = v;
	}

	public static URL buildUrl(String target, String... parameters)
			throws MalformedURLException {
		StringBuffer buffer = new StringBuffer(Utils.BEERME_URL + target);
		buffer.append("?appVersion=" + getAppVersion());
		buffer.append("&platformVersion=" + getPlatformVersion());

		for (String parameter : parameters) {
			buffer.append('&' + parameter);
		}

		return new URL(buffer.toString());
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
}