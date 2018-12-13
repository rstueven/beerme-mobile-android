package com.beerme.android.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.beerme.android.R;
import com.beerme.android.database.DbOpenHelper;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.prefs.SettingsActivity;
import com.beerme.android.utils.DatabaseUpdateAlert;
import com.beerme.android.utils.FileUtils;
import com.beerme.android.utils.Help;
import com.beerme.android.utils.SendUpdateDialog;
import com.beerme.android.utils.Utils;
import com.beerme.android.utils.Version;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ar.com.daidalos.afiledialog.FileChooserDialog;

public abstract class BeerMeActivity extends AppCompatActivity {
    private static final String PERMISSIONS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int REQUEST_FINE_LOCATION = 1;
    private static final String IS_REQUESTING_LOCATION_UPDATES = "isRequestingLocationUpdates";

    public interface LocationListener {
        void onLocationUpdated(Location location);
    }

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;
    private boolean isRequestingLocationUpdates = false;
    private LocationCallback mLocationCallback;
    private final List<LocationListener> locationListeners = new ArrayList<>();
    private LocationRequest mLocationRequest;

    // https://developers.google.com/mobile-ads-sdk/docs/admob/play-migration
    private AdView adView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateValuesFromBundle(savedInstanceState);
        checkLocationPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Version.FREE) {
            adView = findViewById(R.id.adView);
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
                builder.addTestDevice("C8E6BE9575D0D42208B498EF58A9B3A8");
                adView.loadAd(builder.build());
            }
        }
    }

    protected void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                final Activity activity = this;
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, new String[]{PERMISSIONS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{PERMISSIONS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            }
        } else {
            setupLocationClient();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationClient();
            }
        }
    }

    private void setupLocationClient() {
        if (mFusedLocationClient == null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(5000);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        for (Location location : locationResult.getLocations()) {
//                            Log.d("nfs", "LOCATION_RESULT: " + location.toString());
                            mCurrentLocation = location;

                            for (LocationListener listener : locationListeners) {
                                listener.onLocationUpdated(location);
                            }
                        }
                    }
                }
            };

            checkCurrentLocation();
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }

        // Is this necessarily true? Or should it be controlled by the Fragments?
        stopLocationUpdates();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isRequestingLocationUpdates) {
            startLocationUpdates();
        }

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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_REQUESTING_LOCATION_UPDATES, isRequestingLocationUpdates);

        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(IS_REQUESTING_LOCATION_UPDATES)) {
                isRequestingLocationUpdates = savedInstanceState.getBoolean(IS_REQUESTING_LOCATION_UPDATES);
            }
        }
    }

    public void registerLocationListener(LocationListener listener) {
        if (listener != null && !locationListeners.contains(listener)) {
            locationListeners.add(listener);
            isRequestingLocationUpdates = true;
            startLocationUpdates();
        }
    }

    protected void unRegisterLocationListener(LocationListener listener) {
        if (listener != null) {
            locationListeners.remove(listener);
            if (locationListeners.size() == 0) {
                isRequestingLocationUpdates = false;
                stopLocationUpdates();
            }
        }
    }

    private void startLocationUpdates() {
        if (mFusedLocationClient != null) {
            if (ContextCompat.checkSelfPermission(this, PERMISSIONS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        }
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void checkCurrentLocation() {
        if (mCurrentLocation == null) {
            if (ContextCompat.checkSelfPermission(this, PERMISSIONS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                loadCurrentLocation();
            }
        }
    }

    private void loadCurrentLocation() {
        if (mFusedLocationClient != null) {
            if (ContextCompat.checkSelfPermission(this, PERMISSIONS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        mCurrentLocation = location;
                    }
                });
            }
        }
    }

    public Location getCurrentLocation() {
//        Log.d(Utils.APPTAG, "getCurrentLocation(" + (mCurrentLocation != null ? mCurrentLocation.toString() : "NULL") + ")");
        return mCurrentLocation;
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
                if (!settings.getBoolean(DatabaseUpdateAlert.SHOW_DB_UPDATE_ALERT_PREF, true)) {
                    newFragment = new DatabaseUpdateAlert();
                    newFragment.show(fm, DatabaseUpdateAlert.SHOW_DB_UPDATE_ALERT_PREF);
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
                                    FileUtils.copyFile(DbOpenHelper.DB_FILEPATH, file.getAbsolutePath());
                                } catch (IOException e) {
                                    Log.e("beerme", e.getLocalizedMessage());
                                } finally {
                                    Toast.makeText(context, "Done copying", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFileSelected(Dialog source, File folder, String name) {
                                try {
                                    FileUtils.copyFile(DbOpenHelper.DB_FILEPATH, folder.getAbsolutePath() + "/" + name);
                                } catch (IOException e) {
                                    Log.e("beerme", e.getLocalizedMessage());
                                } finally {
                                    Toast.makeText(context, "Done copying", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                exportDialog.show();
                return true;
            case R.id.action_database_import:
                FileChooserDialog importDialog = new FileChooserDialog(this);
                importDialog.setCanCreateFiles(false);
                importDialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
                    @Override
                    public void onFileSelected(Dialog source, File file) {
                        try {
                            FileUtils.copyFile(file.getAbsolutePath(), DbOpenHelper.DB_FILEPATH);
                        } catch (IOException e) {
                            Log.e("beerme", e.getLocalizedMessage());
                        } finally {
                            Toast.makeText(context, "Done copying", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFileSelected(Dialog source, File folder, String name) {
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