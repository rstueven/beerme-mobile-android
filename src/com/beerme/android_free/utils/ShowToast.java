package com.beerme.android_free.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class ShowToast {
	public static void show(final Activity activity, final String text) {
		Log.i(Utils.APPTAG, activity.getLocalClassName() + ": " + text);
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
			}
		});
	}

	public static void show(final Activity activity, final int stringResource) {
		show(activity, activity.getString(stringResource));
	}
}