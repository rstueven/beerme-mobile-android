/**
 * 
 */
package com.beerme.android_free.ui;

import java.lang.ref.WeakReference;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.beerme.android_free.R;
import com.beerme.android_free.database.DbOpenHelper;
import com.beerme.android_free.prefs.Prefs;
import com.beerme.android_free.prefs.SharedPreferenceSaver;
import com.beerme.android_free.utils.CheckLicenseTask;
import com.beerme.android_free.utils.DatabaseUpdateAlert;
import com.beerme.android_free.utils.DatabaseUpdateAlert.DatabaseUpdateAlerter;
import com.beerme.android_free.utils.ErrLog;
import com.beerme.android_free.utils.Utils;

/**
 * @author rstueven
 * 
 */
public class Splash extends FragmentActivity implements
		CheckLicenseTask.CheckLicenseListener, DbOpenHelper.OnDbOpenListener,
		DatabaseUpdateAlerter {
	private static final int DATABASE_CHECK = 2;
	private static final int DATABASE_DONE = 3;
	private static final int LICENSE_CHECK = 4;
	private static final int LICENSE_OK = 5;
	private static final int LICENSE_DONE = 6;
	private static final int NOT_LICENSED = 7;
	private static final int NETWORK_CHECK = 8;
	private static final int NETWORK_DONE = 9;
	private static SplashHandler mHandler = null;
	private boolean mLicensed = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		// Set the global application version string
		Utils.setAppVersion(this);

		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		mHandler = new SplashHandler(this);

		mHandler.sendEmptyMessage(NETWORK_CHECK);
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.trackActivityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Utils.trackActivityStop(this);
	}

	@Override
	public void onLicenseChecked(boolean result) {
		mLicensed = result;
		Context context = getApplicationContext();
		Editor editor = Prefs.getSettingsEditor(context);
		SharedPreferenceSaver saver = Prefs.getSettingsSaver(context);
		editor.putBoolean(Prefs.KEY_LICENSED, result);
		saver.savePreferences(editor, false);
		mHandler.sendEmptyMessage(result ? LICENSE_OK : NOT_LICENSED);
	}

	private void checkDatabase() {
		mHandler.sendEmptyMessage(DATABASE_CHECK);
	}

	@Override
	public void onDbOpen() {
		mHandler.sendEmptyMessage(DATABASE_DONE);
	}

	private final static class SplashHandler extends Handler {
		WeakReference<Splash> mRef;
		private ProgressDialog mLicenseDialog = null;
		SQLiteDatabase mDb = null;

		public SplashHandler(Splash instance) {
			mRef = new WeakReference<Splash>(instance);
		}

		@Override
		public void handleMessage(Message msg) {
			Splash instance = mRef.get();

			switch (msg.what) {
			case NETWORK_CHECK:
				// Online?
				if (!Utils.isOnline(instance)) {
					ErrLog.log(instance, "Splash.onCreate()", null,
							R.string.No_network_connection);
					DialogFrag.newInstance(DialogFrag.Mode.OFFLINE).show(
							instance.getSupportFragmentManager(),
							"offline");
				} else {
					this.sendEmptyMessage(NETWORK_DONE);
				}
				break;
			case NETWORK_DONE:
				// Check the license status, if it hasn't previously been
				// approved
				SharedPreferences settings = Prefs.getSettings(instance);
				if (Utils.FREE_VERSION) {
					instance.onLicenseChecked(true);
				} else if (settings.getBoolean(Prefs.KEY_LICENSED, false)) {
					instance.onLicenseChecked(true);
				} else {
					mHandler.sendEmptyMessage(LICENSE_CHECK);
				}
				break;
			case LICENSE_CHECK:
				mLicenseDialog = ProgressDialog
						.show(instance,
								instance.getString(R.string.Checking_license),
								"", true);
				new Thread(new CheckLicenseTask(instance), "CheckLicenseTask")
						.start();
				break;
			case LICENSE_OK:
				mHandler.sendEmptyMessage(LICENSE_DONE);
				break;
			case NOT_LICENSED:
				if (mLicenseDialog != null) {
					mLicenseDialog.dismiss();
				}
				DialogFrag.newInstance(DialogFrag.Mode.UNLICENSED).show(
						instance.getSupportFragmentManager(), "unlicensed");
				mHandler.sendEmptyMessage(LICENSE_DONE);
				break;
			case LICENSE_DONE:
				if (mLicenseDialog != null) {
					mLicenseDialog.dismiss();
				}
				if (instance.mLicensed) {
					instance.checkDatabase();
				}
				break;
			case DATABASE_CHECK:
				if (DbOpenHelper.isUpdating(instance)) {
					if (Prefs
							.getSettings(instance)
							.getBoolean(
									DatabaseUpdateAlert.SHOW_DB_UPDATE_ALERT_PREF,
									true)) {
						new DatabaseUpdateAlert().show(
								instance.getSupportFragmentManager(),
								"databaseUpdateAlert");
					} else {
						mHandler.sendEmptyMessage(DATABASE_DONE);
					}
				} else {
					DbOpenHelper helper = DbOpenHelper.getInstance(instance);
					mDb = helper.getWritableDatabase();
				}
				break;
			case DATABASE_DONE:
				if (mDb != null) {
					mDb.close();
				}

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				} finally {
					instance.startActivity(new Intent(instance,
							MainActivity.class));
					instance.finish();
				}
				break;
			}
		}
	}

	@Override
	public void onDatabaseUpdateAlert() {
		mHandler.sendEmptyMessage(DATABASE_DONE);
	}
}