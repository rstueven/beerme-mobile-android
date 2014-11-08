package com.beerme.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.beerme.android.R;
import com.beerme.android.database.DatabaseProvider;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.Utils;

public class Splash extends Activity implements DatabaseProvider.DatabaseProviderListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Utils.APPTAG, "Splash.onCreate()");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.splash);

		// Set the global application version string
		Utils.setAppVersion(this);

		// TODO: Preference defaults
		// PreferenceManager.setDefaultValues(this, R.xml.settings, false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(Utils.APPTAG, "Splash.onResume()");

	    setProgressBarVisibility(true);
	    
		DatabaseProvider.registerListener(this);
		if (!DatabaseProvider.isInstalling()) {
			onDatabaseInstalled();
		}
	}
	
	@Override
	public void onPause() {
		Log.d(Utils.APPTAG, "Splash.onPause()");
		DatabaseProvider.unRegisterListener(this);

	    setProgressBarVisibility(true);
		
		super.onPause();
	}

	@Override
	public void onDatabaseInstalled() {
		Log.d(Utils.APPTAG, "Splash.onDatabaseInstalled()");
		DatabaseProvider.unRegisterListener(this);
	    setProgressBarVisibility(false);
		this.startActivity(new Intent(this, MainActivity.class));
		this.finish();
	}

	@Override
	public void onError(String msg, Exception e) {
		Log.d(Utils.APPTAG, "Splash.onError(" + msg + ", " + e.toString() + ")");
		ErrLog.log(this, null, e, msg);
	}
}