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
    private static final String API_URL = "http://beerme.com/mobile/v3/";
    private static final String UPDATE_URL = API_URL + "dbUpdate.php";

    public DBUpdateService() {
        super("DBUpdateService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d("beerme", "onHandleIntent(" + intent.toString() + ")");
        final DBHelper dbHelper = DBHelper.getInstance(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        String latestDate = null;
        final String latestSql = "SELECT MAX(updated) FROM brewery";
        final Cursor c = db.rawQuery(latestSql, null);
        if (c.moveToFirst()) {
            latestDate = c.getString(0);
        }
        c.close();

        final String urlString = UPDATE_URL + ((latestDate != null) ? ("?t=" + latestDate) : "");
        Log.d("beerme", urlString);
        BufferedReader reader = null;

        try {
            final URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            loadBreweryUpdates(db, reader);
            loadBeerUpdates(db, reader);
            loadStyleUpdates(db, reader);
        } catch (final IOException e) {
            Log.e("fanapp", "DBUpdateService.onHandleIntent(): " + e.getLocalizedMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // Ignore.
                }
            }
        }
    }

    private void loadBreweryUpdates(final SQLiteDatabase db, final BufferedReader reader) {
        String line = "LINE ZERO";
        try {
            String[] values;
            final SQLiteStatement stmt = db.compileStatement("INSERT OR REPLACE INTO brewery (_id, name, address, latitude, longitude, status, hours, phone, web, services, image, updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            while ((line = reader.readLine()) != null) {
//                Log.d("beerme", line);
                if ("#####".equals(line)) {
                    break;
                }
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
                // hours
                stmt.bindString(7, values[6]);
                // phone
                stmt.bindString(8, values[7]);
                // web
                stmt.bindString(9, values[8]);
                // svc
                try {
                    stmt.bindLong(10, Integer.parseInt(values[9]));
                } catch (final NumberFormatException e) {
                    numberFormatWarn(e, line);
                }
                // image
                stmt.bindString(11, values[10]);
                // updated
                stmt.bindString(12, values[11]);

                stmt.execute();
            }
        } catch (final IOException e) {
            Log.e("fanapp", "loadBreweryUpdates(): " + line);
            Log.e("fanapp", "loadBreweryUpdates(): " + e.getLocalizedMessage());
        }
    }

    private void loadBeerUpdates(final SQLiteDatabase db, final BufferedReader reader) {
        String line = "LINE ZERO";

        try {
            String[] values;
            final SQLiteStatement stmt = db.compileStatement("INSERT OR REPLACE INTO beer (_id, breweryid, name, style, abv, image, updated, beermerating) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            while ((line = reader.readLine()) != null) {
//                Log.d("beerme", line);
                if ("#####".equals(line)) {
                    break;
                }
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
                // style
                if (!values[3].isEmpty()) {
                    try {
                        stmt.bindLong(4, Integer.parseInt(values[3]));
                    } catch (final NumberFormatException e) {
                        numberFormatWarn(e, line);
                    }
                }
                // abv
                if (!values[4].isEmpty()) {
                    try {
                        stmt.bindDouble(5, Double.parseDouble(values[4]));
                    } catch (final NumberFormatException e) {
                        numberFormatWarn(e, line);
                    }
                }
                // image
                stmt.bindString(6, values[5]);
                // updated
                stmt.bindString(7, values[6]);
                // beermerating
                if (!values[7].isEmpty()) {
                    try {
                        stmt.bindDouble(8, Double.parseDouble(values[7]));
                    } catch (final NumberFormatException e) {
                        numberFormatWarn(e, line);
                    }
                }

                stmt.execute();
            }
        } catch (final IOException e) {
            Log.e("fanapp", "loadBeerUpdates(): " + line);
            Log.e("fanapp", "loadBeerUpdates(): " + e.getLocalizedMessage());
        }
    }

    private void loadStyleUpdates(final SQLiteDatabase db, final BufferedReader reader) {
        String line = "LINE ZERO";

        try {
            // id, name, updated
            String[] values;
            final SQLiteStatement stmt = db.compileStatement("INSERT OR REPLACE INTO style (_id, name, updated) VALUES (?, ?, ?)");

            while ((line = reader.readLine()) != null) {
//                Log.d("beerme", line);
                if ("#####".equals(line)) {
                    break;
                }
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