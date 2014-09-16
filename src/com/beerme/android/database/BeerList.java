package com.beerme.android.database;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.beerme.android.utils.ErrLog;
import com.beerme.android.R;

public class BeerList extends ArrayList<Beer> {
	private static final long serialVersionUID = 5103646984996136490L;
	private static final String TABLE = "beer";
	private static String[] mColumns = { "_id" };

	public BeerList() {
	}

	public BeerList(Context context, long breweryId) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		if (breweryId <= 0) {
			throw new IllegalArgumentException("Invalid breweryId(" + breweryId
					+ ")");
		}

		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = DbOpenHelper.getInstance(context).getReadableDatabase();

			cursor = db.query(TABLE, mColumns, "breweryId=" + breweryId, null,
					null, null, "name");

			while (cursor.moveToNext()) {
				this.add(new Beer(context, cursor.getInt(0)));
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "BeerList(" + breweryId + ")", e,
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

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();

		for (Beer beer : this) {
			s.append(beer.getId() + ":" + beer.getBreweryId() + ":"
					+ beer.getName() + ":" + beer.getStyle() + ":"
					+ beer.getAbv() + ":" + beer.getImage() + ":"
					+ beer.getUpdated() + "\n");
		}

		return s.toString();
	}
}