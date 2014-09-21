package com.beerme.android_free.database;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.beerme.android_free.R;
import com.beerme.android_free.utils.ErrLog;

public class BreweryNotesList extends ArrayList<BreweryNote> {
	private static final long serialVersionUID = -2810537115303896L;
	private long mBreweryId;
	private static final String TABLE = "brewerynotes";
	private static String[] mColumns = { "_id" };

	public BreweryNotesList() {
	}

	public BreweryNotesList(Context context, long breweryId) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		if (breweryId <= 0) {
			throw new IllegalArgumentException("Invalid breweryId(" + breweryId
					+ ")");
		}

		this.mBreweryId = breweryId;

		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = DbOpenHelper.getInstance(context).getReadableDatabase();

			cursor = db.query(TABLE, mColumns, "breweryId=" + mBreweryId, null,
					null, null, "date DESC");

			long id = -1;

			while (cursor.moveToNext()) {
				id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
				this.add(BreweryNote.newInstance(context, id));
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "BreweryNotesList(" + breweryId + ")", e,
					R.string.Database_is_busy);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}
}