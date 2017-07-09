package com.beerme.android.db;

import android.content.ContentResolver;
import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by rstueven on 2/24/17.
 * <p/>
 * Database create, update, open, close.
 */

public class DBHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "beerme";
    private static final int DATABASE_VERSION = 6;

    // TODO: Is this actually a potential memory leak?
    private static DBHelper instance;
    private final ContentResolver contentResolver;

    public static synchronized DBHelper getInstance(final Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }

        return instance;
    }

    private DBHelper(final Context context) {
        super(context, DATABASE_NAME, context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);
        this.contentResolver = context.getContentResolver();
    }

    public ContentResolver getContentResolver() {
        return contentResolver;
    }
}