package com.beerme.android_free.utils;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

import com.beerme.android_free.R;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

public class CheckLicenseTask implements Runnable {
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo0aACNgLXjwRhu5jPPVqwrE6woZcauIye54PtBDHcqoKf6x7TxMYqZwmFHln9n/ysM8gipjiYIJnc8CU+CGDvj92yQ70mVBFsRQIldhETBpDJi9fniazipRg6WD7iby1nRZ2F1JR3q6CYzqxeRFhf9cXsXLzkbapHCxllYgxCx4A5kqiOy2H7ybo+jkwCJ7eS7Txfg+i4LUXxh0/exk9k24GxTyEatW5TrX2cPyorM1aIFLYGx5Vn0l4FEI1thf6EX5fYizFGnOtQ4RmV4YajMwFNR/YnzIYSSg20xacY/2rQvEdhRfZ+5mm1ouQSOqXFZtffmZvMT8R90kZ1OPe3QIDAQAB";
	private static final byte[] SALT = new byte[] { -40, 95, -14, 113, -120,
			67, 15, -82, -118, -32, -54, -124, 82, -29, -15, 12, -2, 118, 99,
			102 };
	private Context mContext;

	public interface CheckLicenseListener {
		public void onLicenseChecked(boolean result);
	}

	public CheckLicenseTask(Context context) {
		mContext = context;
	}

	@Override
	public void run() {
		String deviceId = Secure.getString(mContext.getContentResolver(),
				Secure.ANDROID_ID);
		BeerMeLicenseCheckerCallback callback = new BeerMeLicenseCheckerCallback(
				mContext);
		LicenseChecker checker = new LicenseChecker(mContext,
				new ServerManagedPolicy(mContext, new AESObfuscator(SALT,
						mContext.getPackageName(), deviceId)),
				BASE64_PUBLIC_KEY);
		checker.checkAccess(callback);
	}

	private static class BeerMeLicenseCheckerCallback implements
			LicenseCheckerCallback {
		private Context mContext;
		private CheckLicenseListener mListener;

		public BeerMeLicenseCheckerCallback(Context context) {
			this.mContext = context;
			mListener = (CheckLicenseListener) mContext;
		}

		@Override
		public void allow(int reason) {
			onLicenseChecked(true);
		}

		@Override
		public void applicationError(int errorCode) {
			if (((Activity) mContext).isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}

			// This is a polite way of saying the developer made a mistake
			// while setting up or calling the license checker library.
			// Please examine the error code and fix the error.
			String msg;
			switch (errorCode) {
			case LicenseCheckerCallback.ERROR_INVALID_PACKAGE_NAME:
				msg = "Package is not installed.";
				break;
			case LicenseCheckerCallback.ERROR_NON_MATCHING_UID:
				msg = "Requested for a package that is not the current app.";
				break;
			case LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED:
				msg = "Market does not know about the package.";
				break;
			case LicenseCheckerCallback.ERROR_CHECK_IN_PROGRESS:
				msg = "A previous check request is already in progress. Only one check is allowed at a time.";
				break;
			case LicenseCheckerCallback.ERROR_INVALID_PUBLIC_KEY:
				msg = "Supplied public key is invalid.";
				break;
			case LicenseCheckerCallback.ERROR_MISSING_PERMISSION:
				msg = "App must request com.android.vending.CHECK_LICENSE permission.";
				break;
			default:
				msg = "Unknown error.";
				break;
			}
			String errMsg = String.format(
					mContext.getString(R.string.Application_error), errorCode)
					+ ": " + msg;
			ErrLog.log(mContext,
					"BeerMeLicenseCheckerCallback.applicationError("
							+ errorCode + ")", null, errMsg);
			onLicenseChecked(false);
		}

		@Override
		public void dontAllow(int reason) {
			Log.i(Utils.APPTAG,
					"Splash.BeerMeLicenseCheckerCallback.dontAllow(" + reason
							+ ")");

			ErrLog.log(mContext, "BeerMeLicenseCheckerCallback.dontAllow("
					+ reason + ")", null, R.string.App_not_licensed);

			onLicenseChecked(false);
		}

		private void onLicenseChecked(boolean result) {
			if (Utils.DEBUG) {
				Toast.makeText(mContext, "SKIPPING LICENSE CHECK",
						Toast.LENGTH_LONG).show();
				mListener.onLicenseChecked(true);
			} else {
				mListener.onLicenseChecked(result);
			}
		}
	}
}