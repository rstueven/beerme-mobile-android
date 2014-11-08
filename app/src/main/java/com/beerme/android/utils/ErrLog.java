package com.beerme.android.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Provides consistent error logging via {@link android.util.Log} and error message display
 * via {@link android.widget.Toast}.
 * 
 */
public class ErrLog {

	private ErrLog() {
	}

	/**
	 * 
	 * @param context
	 *            Application context
	 * @param methodName
	 *            Name of the method that suffered the error (may be null)
	 * @param exception
	 *            Exception (may be null)
	 * @param userMessage
	 *            Integer pointer to a text resource; error message for the user
	 * 
	 */
	public static void log(final Context context, final String methodName,
			final Exception exception, final int userMessage) {
		final String umsg = (userMessage == 0) ? "" : context.getText(userMessage).toString();
		log(context, methodName, exception, umsg);
	}

	/**
	 * 
	 * @param context
	 *            Application context
	 * @param methodName
	 *            Name of the method that suffered the error (may be null)
	 * @param exception
	 *            Exception (may be null)
	 * @param userMessage
	 *            Error message for the user (may be null)
	 * 
	 */
	public static void log(final Context context, final String methodName,
			final Exception exception, final String userMessage) {
		Log.e(Utils.APPTAG, context.getClass().getName() + "." + methodName + ": "
				+ userMessage);

		if (exception != null) {
			String exMsg = exception.getLocalizedMessage();
			Log.e(Utils.APPTAG, (exMsg == null) ? exception.toString() : exMsg);
		}

		if (context instanceof Activity) {
			((Activity) context).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (userMessage != null) {
						Toast.makeText(((Activity) context), userMessage, Toast.LENGTH_LONG)
								.show();
					}
				}
			});
		}
	}
}