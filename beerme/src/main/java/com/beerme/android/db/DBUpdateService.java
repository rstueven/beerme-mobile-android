package com.beerme.android.db;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by rstueven on 2/25/17.
 * <p/>
 * Downloads updated data from beerme.com
 */

// TODO: Setting to allow downloads over WiFi only.
public class DBUpdateService extends IntentService {
    private static final String API_URL = "http://beerme.com/mobile/v2/";
    private static final String BREWERY_URL = API_URL + "breweryList.php";
    private static final String BEER_URL = API_URL + "beerList.php";
    private static final String STYLE_URL = API_URL + "styleList.php";

    public DBUpdateService() {
        super("DBUpdateService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d("beerme", "onHandleIntent(" + intent.toString() + ")");
        final DBHelper dbHelper = DBHelper.getInstance(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        loadBreweryUpdates(db);
        loadBeerUpdates(db);
        loadStyleUpdates(db);
    }

    // TODO: Refactor. Will require a callback mechanism.

    private void loadBreweryUpdates(final SQLiteDatabase db) {
        String latestDate = null;
        final String latestSql = "SELECT MAX(updated) FROM brewery";
        final Cursor c = db.rawQuery(latestSql, null);
        if (c.moveToFirst()) {
            latestDate = c.getString(0);
        }
        c.close();

        final String urlString = BREWERY_URL + ((latestDate != null) ? ("?t=" + latestDate) : "");
        Log.d("beerme", urlString);
        String line = "LINE ZERO";
        try {
            final URL url = new URL(urlString);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            // id, name, address, latitude, longitude, status, svc, updated, phone, hours, url, image
            String[] values;
            final SQLiteStatement stmt = db.compileStatement("INSERT OR REPLACE INTO brewery (_id, name, address, latitude, longitude, status, services, updated, phone, hours, web, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            while ((line = reader.readLine()) != null) {
                stmt.clearBindings();
                values = line.split("\\|", -1);

                // _id
                try {
                    stmt.bindLong(1, Integer.parseInt(values[0]));
                } catch (final NumberFormatException e) {
                    numberFormatWarn(e, line);
                }
                // name
                stmt.bindString(2, values[1]);
                // address
                stmt.bindString(3, values[2]);
                // latitude
                try {
                    stmt.bindDouble(4, Float.parseFloat(values[3]));
                } catch (final NumberFormatException e) {
                    numberFormatWarn(e, line);
                }
                // longitude
                try {
                    stmt.bindDouble(5, Float.parseFloat(values[4]));
                } catch (final NumberFormatException e) {
                    numberFormatWarn(e, line);
                }
                // status
                try {
                    stmt.bindLong(6, Integer.parseInt(values[5]));
                } catch (final NumberFormatException e) {
                    numberFormatWarn(e, line);
                }
                // svc
                try {
                    stmt.bindLong(7, Integer.parseInt(values[6]));
                } catch (final NumberFormatException e) {
                    numberFormatWarn(e, line);
                }
                // updated
                stmt.bindString(8, values[7]);
                // phone
                stmt.bindString(9, values[8]);
                // hours
                stmt.bindString(10, values[9]);
                // web
                stmt.bindString(11, values[10]);
                // image
                stmt.bindString(12, values[11]);

                stmt.execute();
            }

            reader.close();
        } catch (final IOException e) {
            Log.e("fanapp", "loadBreweryUpdates(): " + line);
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

        final String urlString = BEER_URL + ((latestDate != null) ? ("?t=" + latestDate) : "");
        Log.d("beerme", urlString);
        String line = "LINE ZERO";

        try {
            final URL url = new URL(urlString);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            // id, breweryid, name, updated, style, abv, image, score
            String[] values;
            final SQLiteStatement stmt = db.compileStatement("INSERT OR REPLACE INTO beer (_id, breweryid, name, updated, style, abv, image, beermerating) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            while ((line = reader.readLine()) != null) {
                stmt.clearBindings();
                values = line.split("\\|", -1);

                // _id
                try {
                    stmt.bindLong(1, Integer.parseInt(values[0]));
                } catch (final NumberFormatException e) {
                    numberFormatWarn(e, line);
                }
                // breweryid
                try {
                    stmt.bindLong(2, Integer.parseInt(values[1]));
                } catch (final NumberFormatException e) {
                    numberFormatWarn(e, line);
                }
                // name
                stmt.bindString(3, values[2]);
                // updated
                stmt.bindString(4, values[3]);
                // style
                if (!values[4].isEmpty()) {
                    try {
                        stmt.bindLong(5, Integer.parseInt(values[4]));
                    } catch (final NumberFormatException e) {
                        numberFormatWarn(e, line);
                    }
                }
                // abv
                if (!values[5].isEmpty()) {
                    try {
                        stmt.bindDouble(6, Double.parseDouble(values[5]));
                    } catch (final NumberFormatException e) {
                        numberFormatWarn(e, line);
                    }
                }
                // image
                stmt.bindString(7, values[6]);
                // score
                if (!values[7].isEmpty()) {
                    try {
                        stmt.bindDouble(8, Double.parseDouble(values[7]));
                    } catch (final NumberFormatException e) {
                        numberFormatWarn(e, line);
                    }
                }

                stmt.execute();
            }

            reader.close();
        } catch (final IOException e) {
            Log.e("fanapp", "loadBeerUpdates(): " + line);
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

        final String urlString = STYLE_URL + ((latestDate != null) ? ("?t=" + latestDate) : "");
        Log.d("beerme", urlString);
        String line = "LINE ZERO";

        try {
            final URL url = new URL(urlString);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            // id, name, updated
            String[] values;
            final SQLiteStatement stmt = db.compileStatement("INSERT OR REPLACE INTO style (_id, name, updated) VALUES (?, ?, ?)");

            while ((line = reader.readLine()) != null) {
                stmt.clearBindings();
                values = line.split("\\|", -1);

                // _id
                try {
                    stmt.bindLong(1, Integer.parseInt(values[0]));
                } catch (final NumberFormatException e) {
                    numberFormatWarn(e, line);
                }
                // name
                stmt.bindString(2, values[1]);
                // updated
                stmt.bindString(3, values[2]);

                stmt.execute();
            }

            reader.close();
        } catch (final IOException e) {
            Log.e("fanapp", "loadStyleUpdates(): " + line);
            Log.e("fanapp", "loadStyleUpdates(): " + e.getLocalizedMessage());
        }
    }

    private void numberFormatWarn(final Exception e, final String line) {
        Log.w("fanapp", "NUMBER FORMAT:" + line);
        Log.w("fanapp", "NUMBER FORMAT: " + e.getLocalizedMessage());
    }
}