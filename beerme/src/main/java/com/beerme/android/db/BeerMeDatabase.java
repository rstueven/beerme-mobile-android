package com.beerme.android.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.beerme.android.util.NetworkRequestQueue;
import com.beerme.android.util.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(version = 7, entities = {Brewery.class, Beer.class, BeerNotes.class, BreweryNotes.class, Style.class})
public abstract class BeerMeDatabase extends RoomDatabase {
    private static BeerMeDatabase mInstance;
    private static final String DB_NAME = "beerme";
    private static final int BUFSIZE = 0x7FFF;
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    BeerMeDatabase() {
    }

    public static void init(Context context) {
        Log.d("beerme", "BeerMeDatabase.init()");
        // Change this whenever a new database file is installed.
        // OR BETTER YET
        // Get the last update date from the database itself.
        long updated = SharedPref.read(SharedPref.Pref.DB_LAST_UPDATE, 0L);
        if (updated == 0L) {
            Calendar cal = Calendar.getInstance();
            cal.set(2019, 0, 21);
            SharedPref.write(SharedPref.Pref.DB_LAST_UPDATE, cal.getTimeInMillis());
        }

        final String DB_PATH = context.getDatabasePath(DB_NAME).getPath();

        if (!new File(DB_PATH).exists()) {
            copyInitialDatabase(context, DB_PATH);
        }

        if (mInstance == null) {
            mInstance = Room.databaseBuilder(context, BeerMeDatabase.class, "beerme")
                    .addMigrations(MIGRATION_6_7)
                    .addCallback(new DBCallback(context))
                    .build();
        }
    }

    public static BeerMeDatabase getInstance(Context context) {
        if (mInstance == null) {
            BeerMeDatabase.init(context);
        }

        return mInstance;
    }

    abstract public BreweryDao breweryDao();

    abstract public BeerDao beerDao();

    abstract public StyleDao styleDao();

    private static void copyInitialDatabase(@NonNull final Context context, @NonNull final String path) {
        Log.d("beerme", "copyInitialDatabase(" + path + ")");
        long now = System.currentTimeMillis();
        ZipInputStream zin = null;
        OutputStream out = null;


        try {
            zin = new ZipInputStream(context.getAssets().open(DB_NAME + ".zip"));
            out = new FileOutputStream(path);
            byte[] buf = new byte[BUFSIZE];
            int length;

            ZipEntry zipEntry = zin.getNextEntry();
            if (zipEntry == null) {
                throw new IOException("No zipEntry");
            }

            while ((length = zin.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        } catch (IOException e) {
            Log.e("beerme", "Failed to create database: " + e.getLocalizedMessage());
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                // IGNORE
            }

            try {
                if (zin != null) {
                    zin.close();
                }
            } catch (IOException e) {
                // IGNORE
            }
        }
        Log.d("beerme", "Database copied in " + (System.currentTimeMillis() - now) + " ms");
        // TODO: Get the oldest newest-record date and stash it in SharedPrefs. Otherwise the whole database will get downloaded in the first update.
//        SharedPref.write(SharedPref.Pref.DB_LAST_UPDATE, System.currentTimeMillis());
    }

    static class DBCallback extends RoomDatabase.Callback {
        final Context context;

        DBCallback(@NonNull Context context) {
            this.context = context;
        }

        public void onCreate(@NonNull final SupportSQLiteDatabase db) {
            Log.d("beerme", "onCreate()");
        }

        public void onOpen(@NonNull final SupportSQLiteDatabase db) {
            Log.d("beerme", "onOpen()");
            // TODO: This should actually come from the database.
            final long lastUpdate = SharedPref.read(SharedPref.Pref.DB_LAST_UPDATE, 0L);
            Log.d("beerme", "Last Update: " + lastUpdate);
            Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    Log.d("beerme", "onOpen.execute.run()");
                    final long now = System.currentTimeMillis();

                    // Download database updates
                    RequestQueue queue = NetworkRequestQueue.getRequestQueue();
                    String dt = dateFormat.format(new Date(lastUpdate));
                    String url = "https://beerme.com/mobile/v3/dbUpdate.php?t=" + dt;
                    Log.d("beerme", "Update URL: " + url);
                    // TODO: This can timeout if there's a lot of data.
                    StringRequest request = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("beerme", "Database updates downloaded in " + (System.currentTimeMillis() - now) + " ms");
                                    Log.d("beerme", "onResponse(" + response.length() + ")");
//                                    Log.d("beerme", response);

                                    // TODO: Gson is probably more appropriate. Might require model changes.
                                    // Although this is working well enough.
                                    try {
                                        JSONObject obj = new JSONObject(response);

                                        JSONArray breweryArr = obj.getJSONArray("brewery");
                                        final List<Brewery> breweries = new ArrayList<>();

                                        for (int i = 0; i < breweryArr.length(); i++) {
//                                            Log.d("beerme", "BREWERY " + i);
//                                            Log.d("beerme", breweryArr.getJSONObject(i).toString());
                                            breweries.add(new Brewery(breweryArr.getJSONObject(i)));
                                        }

                                        JSONArray beerArr = obj.getJSONArray("beer");
                                        final List<Beer> beers = new ArrayList<>();

                                        for (int i = 0; i < beerArr.length(); i++) {
//                                            Log.d("beerme", "BEER " + i);
//                                            Log.d("beerme", beerArr.getJSONObject(i).toString());
                                            beers.add(new Beer(beerArr.getJSONObject(i)));
                                        }

                                        JSONArray styleArr = obj.getJSONArray("style");
                                        final List<Style> styles = new ArrayList<>();

                                        for (int i = 0; i < styleArr.length(); i++) {
//                                            Log.d("beerme", "STYLE " + i);
//                                            Log.d("beerme", styleArr.getJSONObject(i).toString());
                                            styles.add(new Style(styleArr.getJSONObject(i)));
                                        }

                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {
//                                                mInstance.breweryDao().upsertAll(breweries.toArray(new Brewery[]{}));
//                                                mInstance.beerDao().upsertAll(beers.toArray(new Beer[]{}));
//                                                mInstance.styleDao().upsertAll(styles.toArray(new Style[]{}));
                                                mInstance.breweryDao().insertAll(breweries.toArray(new Brewery[]{}));
                                                mInstance.beerDao().insertAll(beers.toArray(new Beer[]{}));
                                                mInstance.styleDao().insertAll(styles.toArray(new Style[]{}));

                                                SharedPref.write(SharedPref.Pref.DB_LAST_UPDATE, System.currentTimeMillis());
                                                Log.d("beerme", "Database updates installed in " + (System.currentTimeMillis() - now) + " ms");
                                            }
                                        });
                                    } catch (JSONException e) {
                                        Log.d("beerme", "Database updates parse failed: " + e.getLocalizedMessage());
                                        Log.d("beerme", "Database updates failed after " + (System.currentTimeMillis() - now) + " ms");
                                    }

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("beerme", "onErrorResponse()");
//                                    Log.e("beerme", error.getLocalizedMessage());
                                    Log.e("beerme", error.toString());

                                    Log.d("beerme", "Database updates failed after " + (System.currentTimeMillis() - now) + " ms");
                                }
                            }
                    );

                    Log.d("beerme", "Sending request");
                    queue.add(request);
                }
            });
        }
    }

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            final long now = System.currentTimeMillis();

            database.execSQL("PRAGMA foreign_keys=off;");
            database.execSQL("BEGIN TRANSACTION;");

            database.execSQL("ALTER TABLE brewery RENAME TO temp_brewery;");
            database.execSQL("CREATE TABLE brewery (_id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, address TEXT NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, status INTEGER NOT NULL, hours TEXT, phone TEXT, web TEXT, services INTEGER NOT NULL DEFAULT 0, image TEXT, updated TEXT NOT NULL DEFAULT CURRENT_DATE);");
            database.execSQL("INSERT INTO brewery (_id, name, address, latitude, longitude, status, hours, phone, web, services, image, updated) SELECT _id, name, address, latitude, longitude, status, hours, phone, web, services, image, updated FROM temp_brewery;");
            database.execSQL("DROP INDEX IF EXISTS brewery_latitude;");
            database.execSQL("CREATE INDEX index_Brewery_latitude ON brewery(latitude);");
            database.execSQL("DROP INDEX IF EXISTS brewery_status;");
            database.execSQL("CREATE INDEX index_Brewery_status ON brewery(status);");
            database.execSQL("DROP INDEX IF EXISTS brewery_updated;");
            database.execSQL("CREATE INDEX index_Brewery_updated ON brewery(updated);");
            database.execSQL("DROP INDEX IF EXISTS brewery_name;");
            database.execSQL("CREATE INDEX index_Brewery_name ON brewery(name);");
            database.execSQL("DROP INDEX IF EXISTS brewery_longitude;");
            database.execSQL("CREATE INDEX index_Brewery_longitude ON brewery(longitude);");
            database.execSQL("DROP TABLE temp_brewery;");

            database.execSQL("ALTER TABLE beer RENAME TO temp_beer;");
            database.execSQL("CREATE TABLE beer (_id INTEGER PRIMARY KEY NOT NULL, breweryid INTEGER NOT NULL, name TEXT NOT NULL, style INTEGER, abv REAL, image TEXT, updated TEXT NOT NULL DEFAULT CURRENT_DATE, beermerating REAL);");
            database.execSQL("INSERT INTO beer (_id, breweryid, name, style, abv, image, updated, beermerating) SELECT _id, breweryid, name, style, abv, image, updated, beermerating FROM temp_beer;");
            database.execSQL("DROP INDEX IF EXISTS beer_breweryid;");
            database.execSQL("CREATE INDEX index_Beer_breweryid ON beer (breweryid);");
            database.execSQL("DROP INDEX IF EXISTS beer_name;");
            database.execSQL("CREATE INDEX index_Beer_name ON beer (name);");
            database.execSQL("DROP INDEX IF EXISTS beer_updated;");
            database.execSQL("CREATE INDEX index_Beer_updated ON beer (updated);");
            database.execSQL("DROP TABLE temp_beer;");

            database.execSQL("ALTER TABLE beernotes RENAME TO temp_beernotes;");
            database.execSQL("CREATE TABLE beernotes (_id INTEGER PRIMARY KEY NOT NULL, beerid INTEGER NOT NULL, package TEXT DEFAULT '', sampled TEXT DEFAULT CURRENT_DATE, place TEXT DEFAULT '', appscore REAL DEFAULT 0, appearance TEXT DEFAULT '', aroscore REAL DEFAULT 0, aroma TEXT DEFAULT '', mouscore REAL DEFAULT 0, mouthfeel TEXT DEFAULT '', ovrscore REAL DEFAULT 0, notes TEXT DEFAULT '');");
            database.execSQL("INSERT INTO beernotes (_id, beerid, package, sampled, place, appscore, appearance, aroscore, aroma, mouscore, mouthfeel, ovrscore, notes) SELECT _id, beerid, package, sampled, place, appscore, appearance, aroscore, aroma, mouscore, mouthfeel, ovrscore, notes FROM temp_beernotes;");
            database.execSQL("DROP INDEX IF EXISTS beernotes_beerid");
            database.execSQL("CREATE INDEX index_beernotes_beerid ON beernotes (beerid);");
            database.execSQL("DROP INDEX IF EXISTS beernotes_sampled");
            database.execSQL("CREATE INDEX index_beernotes_sampled ON beernotes (sampled);");
            database.execSQL("DROP TABLE temp_beernotes");

            database.execSQL("ALTER TABLE brewerynotes RENAME TO temp_brewerynotes;");
            database.execSQL("CREATE TABLE brewerynotes (_id INTEGER PRIMARY KEY NOT NULL, breweryid INTEGER NOT NULL, date TEXT DEFAULT CURRENT_DATE NOT NULL, rating REAL, notes TEXT);");
            database.execSQL("INSERT INTO brewerynotes (_id, breweryid, date, rating, notes) SELECT _id, breweryid, date, rating, notes FROM temp_brewerynotes;");
            database.execSQL("DROP INDEX IF EXISTS brewerynotes_breweryid");
            database.execSQL("CREATE INDEX index_brewerynotes_breweryid ON brewerynotes (breweryid);");
            database.execSQL("DROP INDEX IF EXISTS brewerynotes_date");
            database.execSQL("CREATE INDEX index_brewerynotes_date ON brewerynotes (date);");
            database.execSQL("DROP TABLE temp_brewerynotes");

            database.execSQL("ALTER TABLE style RENAME TO temp_style;");
            database.execSQL("CREATE TABLE style (_id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, updated TEXT NOT NULL DEFAULT CURRENT_DATE);");
            database.execSQL("INSERT INTO style (_id, name, updated) SELECT _id, name, updated FROM temp_style;");
            database.execSQL("DROP INDEX IF EXISTS style_updated");
            database.execSQL("CREATE INDEX index_style_updated ON style (updated);");
            database.execSQL("DROP TABLE temp_style");

            database.execSQL("COMMIT;");
            database.execSQL("PRAGMA foreign_keys=on;");

            Log.d("beerme", "MIGRATION_6_7 completed in " + (System.currentTimeMillis() - now) + " ms");
        }
    };
}