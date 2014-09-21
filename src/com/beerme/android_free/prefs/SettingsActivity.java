package com.beerme.android_free.prefs;

import java.io.IOException;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.ArrayAdapter;

import com.beerme.android_free.R;
import com.beerme.android_free.location.LocationList;
import com.beerme.android_free.utils.Utils;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private Preference distUnitPref;
	private Preference locationPref;
	private Preference filterPref;
	private Preference nearbyDisplayPref;
	private LocationList locationList;
	private String[] locationNames;
	protected List<Address> addressList;
	private static final int DIALOG_LOCATION = 1;
	private SharedPreferences mPrefs;
	private Editor mEditor;
	private SharedPreferenceSaver mSaver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
		mPrefs = Prefs.getSettings(this);
		mEditor = Prefs.getSettingsEditor(this);
		mSaver = Prefs.getSettingsSaver(this);

		distUnitPref = findPreference(Prefs.KEY_DIST_UNIT);
		distUnitPref.setSummary(Utils.getDistUnit(this,
				Integer.parseInt(mPrefs.getString(Prefs.KEY_DIST_UNIT, "0"))));
		distUnitPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						distUnitPref.setSummary(Utils.getDistUnit(
								SettingsActivity.this,
								Integer.parseInt((String) newValue)));
						return true;
					}
				});

		locationPref = findPreference(Prefs.KEY_DEFAULT_LOCATION);
		locationPref.setSummary(mPrefs.getString(Prefs.KEY_DEFAULT_LOCATION,
				getString(R.string.defaultLocationSummary)));
		locationPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						String newLoc = newValue.toString();

						return setupLocationList(newLoc);
					}
				});

		filterPref = findPreference(Prefs.KEY_STATUS_FILTER);
		int filter = mPrefs.getInt(Prefs.KEY_STATUS_FILTER,
				BreweryStatusFilterPreference.DEFAULT_VALUE);
		filterPref.setSummary(BreweryStatusFilterPreference.toString(
				getApplicationContext(), filter));
		filterPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						return true;
					}
				});

		nearbyDisplayPref = findPreference(Prefs.KEY_NEARBY_DISPLAY);
		if (Utils.checkPlayServices(this)
				&& (Utils.checkOpenGLVersion(this) >= 2)) {
			nearbyDisplayPref
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							String[] values = SettingsActivity.this
									.getResources().getStringArray(
											R.array.nearby_display);
							int index = Integer.parseInt((String) newValue);
							nearbyDisplayPref.setSummary(values[index]);
							return true;
						}
					});
		} else {
			// Device doesn't support maps.
			mEditor.putString(Prefs.KEY_NEARBY_DISPLAY,
					Prefs.KEY_NEARBY_DISPLAY_LIST);
			mSaver.savePreferences(mEditor, false);

			nearbyDisplayPref.setEnabled(false);
		}

		String[] values = SettingsActivity.this.getResources().getStringArray(
				R.array.nearby_display);
		int index = Integer.parseInt(mPrefs.getString(Prefs.KEY_NEARBY_DISPLAY,
				"error"));
		nearbyDisplayPref.setSummary(values[index]);
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

	private boolean setupLocationList(String newLoc) {
		boolean success = false;

		if (!"".equals(newLoc)) {
			try {
				locationList = new LocationList(SettingsActivity.this, newLoc);
			} catch (IOException e) {
				try {
					throw e;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (locationList != null) {
				locationNames = locationList.getAddresses();
				if (locationNames != null) {
					addressList = locationList.getList();
					if (addressList != null) {
						showDialog(DIALOG_LOCATION);
						success = true;
					}
				}
			}
		}

		return success;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;

		switch (id) {
		case DIALOG_LOCATION:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.locationDialogTitle);
			builder.setSingleChoiceItems(locationNames, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Address x = addressList.get(item);
							double lng = x.getLongitude();
							double lat = x.getLatitude();
							String formattedAddress = Utils.formatAddress(x);

							mEditor.putFloat(Prefs.KEY_DEFAULT_LATITUDE,
									(float) lat);
							mEditor.putFloat(Prefs.KEY_DEFAULT_LONGITUDE,
									(float) lng);
							mEditor.putString(Prefs.KEY_DEFAULT_LOCATION,
									formattedAddress);
							mSaver.savePreferences(mEditor, false);
							locationPref.setSummary(formattedAddress);
							dialog.dismiss();
						}
					});
			builder.setNegativeButton(R.string.Cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}

		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_LOCATION:
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
					this, android.R.layout.select_dialog_singlechoice,
					android.R.id.text1, locationNames);
			AlertDialog ad = (AlertDialog) dialog;
			ad.getListView().setAdapter(adapter);
			break;
		default:
			super.onPrepareDialog(id, dialog);
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onResume() {
		super.onResume();

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Prefs.KEY_DIST_UNIT)) {
			distUnitPref.setSummary(Utils.getDistUnit(this, Integer
					.parseInt(sharedPreferences.getString(Prefs.KEY_DIST_UNIT,
							"0"))));
		} else if (key.equals(Prefs.KEY_DEFAULT_LOCATION)) {
			locationPref.setSummary(sharedPreferences.getString(key, ""));
		} else if (key.equals(Prefs.KEY_STATUS_FILTER)) {
			filterPref.setSummary(BreweryStatusFilterPreference.toString(this,
					sharedPreferences.getInt(key,
							BreweryStatusFilterPreference.DEFAULT_VALUE)));
		} else if (key.equals(Prefs.KEY_NEARBY_DISPLAY_MAP)) {
			String[] values = SettingsActivity.this.getResources()
					.getStringArray(R.array.nearby_display);
			int index = Integer.parseInt(sharedPreferences.getString(key,
					Prefs.KEY_NEARBY_DISPLAY_LIST));
			nearbyDisplayPref.setSummary(values[index]);
		}
	}
}