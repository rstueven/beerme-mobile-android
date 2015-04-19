package com.beerme.android.prefs;

import android.content.Context;

import com.beerme.android.utils.Utils;

public class PreferenceFactory {


	/**
	 * Create a new SharedPreferenceSaver
	 * 
	 * @param context
	 *            Context
	 * @return SharedPreferenceSaver
	 */
	public static SharedPreferenceSaver getSharedPreferenceSaver(Context context) {
		return Utils.SUPPORTS_GINGERBREAD ? new GingerbreadSharedPreferenceSaver(
				context)
				: Utils.SUPPORTS_FROYO ? new FroyoSharedPreferenceSaver(
						context) : new LegacySharedPreferenceSaver(context);
	}
}