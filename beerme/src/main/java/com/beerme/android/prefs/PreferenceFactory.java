package com.beerme.android.prefs;

import android.content.Context;

public class PreferenceFactory {
	// TODO: No longer necessary to have a factory here.
	/**
	 * Create a new SharedPreferenceSaver
	 * 
	 * @param context
	 *            Context
	 * @return SharedPreferenceSaver
	 */
	public static SharedPreferenceSaver getSharedPreferenceSaver(Context context) {
		return new GingerbreadSharedPreferenceSaver(context);
	}
}