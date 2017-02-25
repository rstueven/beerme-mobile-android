package com.beerme.android;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by rstueven on 2/25/17.
 * <p/>
 * Downloads updated data from beerme.com
 */

public class DBUpdateService extends IntentService {
    public DBUpdateService() {
        super("DBUpdateService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d("beerme", "onHandleIntent(" + intent.toString() + ")");
        final DBHelper dbHelper = DBHelper.getInstance(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
    }
}