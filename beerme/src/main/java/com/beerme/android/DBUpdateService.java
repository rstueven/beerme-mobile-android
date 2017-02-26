package com.beerme.android;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by rstueven on 2/25/17.
 * <p/>
 * Downloads updated data from beerme.com
 */

public class DBUpdateService extends IntentService {
    private static final String API_URL = "http://beerme.com/mobile/v2/";
    private static final String BREWERYLIST_URL = API_URL + "breweryList.php";
    private static final String BEERLIST_URL = API_URL + "beerList.php";
    private static final String STYLELIST_URL = API_URL + "styleList.php";

    public DBUpdateService() {
        super("DBUpdateService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d("beerme", "onHandleIntent(" + intent.toString() + ")");
        final DBHelper dbHelper = DBHelper.getInstance(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // TODO: Separate threads? (Probably won't matter.)
        loadBreweryUpdates(db);
        loadBeerUpdates(db);
        loadStyleUpdates(db);
    }

    private void loadBreweryUpdates(final SQLiteDatabase db) {
        String latestDate = null;
        final String latestSql = "SELECT MAX(updated) FROM brewery";
        final Cursor c = db.rawQuery(latestSql, null);
        if (c.moveToFirst()) {
            latestDate = c.getString(0);
        }
        c.close();

        final String urlString = BREWERYLIST_URL + ((latestDate != null) ? ("?t=" + latestDate) : "");
        Log.d("beerme", urlString);
        try {
            final URL url = new URL(urlString);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                Log.d("beerme", line);
            }

            reader.close();
        } catch (IOException e) {
            Log.e("fanapp", "loadBreweryUpdates(): " + e.getLocalizedMessage());
        }
    }

    private void loadBeerUpdates(final SQLiteDatabase db) {
        String latestDate = null;
        final String latestSql = "SELECT MAX(updated) FROM beer";
        final Cursor c = db.rawQuery(latestSql, null);
        if (c.moveToFirst()) {
            latestDate = c.getString(0);
        }
        c.close();

        final String urlString = BEERLIST_URL + ((latestDate != null) ? ("?t=" + latestDate) : "");
        Log.d("beerme", urlString);
        try {
            final URL url = new URL(urlString);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                Log.d("beerme", line);
            }

            reader.close();
        } catch (IOException e) {
            Log.e("fanapp", "loadBeerUpdates(): " + e.getLocalizedMessage());
        }
    }

    private void loadStyleUpdates(final SQLiteDatabase db) {
        String latestDate = null;
        final String latestSql = "SELECT MAX(updated) FROM style";
        final Cursor c = db.rawQuery(latestSql, null);
        if (c.moveToFirst()) {
            latestDate = c.getString(0);
        }
        c.close();

        final String urlString = STYLELIST_URL + ((latestDate != null) ? ("?t=" + latestDate) : "");
        Log.d("beerme", urlString);
        try {
            final URL url = new URL(urlString);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                Log.d("beerme", line);
            }

            reader.close();
        } catch (IOException e) {
            Log.e("fanapp", "loadStyleUpdates(): " + e.getLocalizedMessage());
        }
    }
}