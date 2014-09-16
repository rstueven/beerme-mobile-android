package com.beerme.android.ui.actionbar;

import java.io.File;
import java.io.IOException;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;

import com.beerme.android.database.DbOpenHelper;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.prefs.SettingsActivity;
import com.beerme.android.ui.AboutFrag;
import com.beerme.android.utils.DatabaseUpdateAlert;
import com.beerme.android.utils.FileUtils;
import com.beerme.android.utils.Help;
import com.beerme.android.utils.SendUpdateDialog;
import com.beerme.android.utils.Utils;
import com.beerme.android.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public abstract class BeerMeActionBarActivity extends ActionBarActivity {
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
		FragmentManager fm = getSupportFragmentManager();
		DialogFragment newFragment = null;
		DbOpenHelper dbHelper;
		SQLiteDatabase db;
		final Context context = this;

		switch (item.getItemId()) {
		case R.id.action_search:
			onSearchRequested();
			return true;
		case R.id.action_update:
			SendUpdateDialog updateFrag = SendUpdateDialog.newInstance(-1);
			updateFrag.show(fm, "update");
			return true;
		case R.id.action_help:
			Help.show(this);
			return true;
		case R.id.action_database_update:
			dbHelper = DbOpenHelper.getInstance(this);
			db = dbHelper.getWritableDatabase();
			dbHelper.forceUpdate(this, db);
			// db.close();
			return true;
		case R.id.action_database_load:
			SharedPreferences settings = Prefs.getSettings(this);
			if (!settings.getBoolean(
					DatabaseUpdateAlert.SHOW_DB_UPDATE_ALERT_PREF, true)) {
				newFragment = new DatabaseUpdateAlert();
				newFragment.show(fm,
						DatabaseUpdateAlert.SHOW_DB_UPDATE_ALERT_PREF);
			}
			dbHelper = DbOpenHelper.getInstance(this);
			db = dbHelper.getWritableDatabase();
			dbHelper.forceReload(this, db);
			// db.close();
			return true;
		case R.id.action_database_export:
			FileChooserDialog exportDialog = new FileChooserDialog(this);
			exportDialog.setCanCreateFiles(true);
			exportDialog
					.addListener(new FileChooserDialog.OnFileSelectedListener() {
						@Override
						public void onFileSelected(Dialog source, File file) {
							try {
								FileUtils.copyFile(DbOpenHelper.DB_FILEPATH,
										file.getAbsolutePath());
							} catch (IOException e) {
								Log.e("beerme", e.getLocalizedMessage());
							} finally {
								Toast.makeText(context, "Done copying",
										Toast.LENGTH_LONG).show();
							}
						}

						@Override
						public void onFileSelected(Dialog source, File folder,
								String name) {
							try {
								FileUtils.copyFile(DbOpenHelper.DB_FILEPATH,
										folder.getAbsolutePath() + "/" + name);
							} catch (IOException e) {
								Log.e("beerme", e.getLocalizedMessage());
							} finally {
								Toast.makeText(context, "Done copying",
										Toast.LENGTH_LONG).show();
							}
						}
					});
			exportDialog.show();
			return true;
		case R.id.action_database_import:
			FileChooserDialog importDialog = new FileChooserDialog(this);
			importDialog.setCanCreateFiles(false);
			importDialog
					.addListener(new FileChooserDialog.OnFileSelectedListener() {
						@Override
						public void onFileSelected(Dialog source, File file) {
							try {
								FileUtils.copyFile(file.getAbsolutePath(),
										DbOpenHelper.DB_FILEPATH);
							} catch (IOException e) {
								Log.e("beerme", e.getLocalizedMessage());
							} finally {
								Toast.makeText(context, "Done copying",
										Toast.LENGTH_LONG).show();
							}
						}

						@Override
						public void onFileSelected(Dialog source, File folder,
								String name) {
						}
					});
			importDialog.show();
			return true;
		case R.id.action_about:
			newFragment = new AboutFrag();
			newFragment.show(fm, "about");
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}