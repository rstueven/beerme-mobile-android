package com.beerme.android_free.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author rstueven
 * 
 */
public class Prefs {
	public static final String KEY_LICENSED = "licensed";
	public static final String KEY_DB_UPDATING = "db_updating";
	public static final String KEY_DB_LAST_UPDATE = "db_last_update";
	public static final String KEY_DIST_UNIT = "dist_unit";
	public static final String KEY_DEFAULT_LOCATION = "default_location";
	public static final String KEY_DEFAULT_LATITUDE = "default_latitude";
	public static final String KEY_DEFAULT_LONGITUDE = "default_longitude";
	public static final String KEY_STATUS_FILTER = "status_filter";
	public static final String KEY_NEARBY_DISPLAY = "nearby_display";
	public static final String KEY_NEARBY_DISPLAY_MAP = "0";
	public static final String KEY_NEARBY_DISPLAY_LIST = "1";
	public static final String KEY_NEARBY_DISTANCE = "nearby_distance";

	private Prefs() {
	}

	public static SharedPreferences getSettings(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static SharedPreferences.Editor getSettingsEditor(Context context) {
		return getSettings(context).edit();
	}

	public static SharedPreferenceSaver getSettingsSaver(Context context) {
		return PreferenceFactory.getSharedPreferenceSaver(context);
	}
}