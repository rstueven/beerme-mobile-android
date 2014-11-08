package com.beerme.android.ui;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.beerme.android.R;
import com.beerme.android.utils.Utils;
import com.google.android.gms.ads.*;

public class AbstractActionBarActivity extends ActionBarActivity {
	// https://developers.google.com/mobile-ads-sdk/docs/admob/play-migration
	private AdView adView = null;

	@Override
	protected void onStart() {
		super.onStart();

		if (Utils.FREE_VERSION) {
			adView = (AdView) findViewById(R.id.adView);
			if (adView != null) {
				adView.setVisibility(View.VISIBLE);
				adView.setAdListener(new AdListener() {
					@Override
					public void onAdFailedToLoad(int errorCode) {
						Log.w(Utils.APPTAG, "Ad failed to load: " + errorCode);
					}
				});

				AdRequest.Builder builder = new AdRequest.Builder();
				builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
				builder.addTestDevice("E44C1FE48309C834798791087E2A29BE");
				adView.loadAd(builder.build());
			}
		}
	}

	@Override
	protected void onPause() {
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
	}

	@Override
	protected void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.actionbar_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
