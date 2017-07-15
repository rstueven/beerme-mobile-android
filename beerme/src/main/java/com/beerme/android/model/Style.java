package com.beerme.android.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.beerme.android.db.DBContract;
import com.beerme.android.db.DBHelper;

/**
 * Created by rstueven on 7/15/17.
 * <p/>
 * Style data
 */

public class Style {
    private int id;
    private String name;

    public Style(final Context context, final int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Style(" + id + "): Invalid id");
        }

        final DBHelper dbHelper = DBHelper.getInstance(context);
        final ContentResolver contentResolver = dbHelper.getContentResolver();
        final Uri uri = DBContract.Style.buildUri(id);

        final Cursor c = contentResolver.query(uri, DBContract.Style.COLUMNS, null, null, null);

        if ((c != null) && c.moveToFirst()) {
            c.moveToFirst();
            this.id = id;
            this.name = c.getString(c.getColumnIndex(DBContract.Style.COLUMN_NAME));

            c.close();
        }
    }

    @Override
    public String toString() {
        String s = "";
        s += "id: " + this.id + "\n";
        s += "name: " + this.name + "\n";

        return s;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
