package com.beerme.android_free.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.beerme.android_free.R;
import com.beerme.android_free.utils.ErrLog;

public class Beer {
	private static final String sql = "SELECT beer.breweryid, brewery.name AS brewery, beer.name, style.name AS style, beer.abv, beer.image, beer.beermerating FROM brewery LEFT JOIN beer ON brewery._id = beer.breweryid LEFT JOIN style ON beer.style = style._id WHERE beer._id = ? ORDER BY beer.name";
	private static final int col_breweryid = 0;
	private static final int col_brewery = 1;
	private static final int col_name = 2;
	private static final int col_style = 3;
	private static final int col_abv = 4;
	private static final int col_image = 5;
	private static final int col_beermerating = 6;
	private long mBeerId = -1;
	private long mBreweryId;
	private String mBrewery;
	private String mName;
	private String mStyle;
	private float mAbv;
	private String mImage;
	private String mUpdated;
	private float mBeerMeRating;

	public Beer(Context context, long beerId) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		if (beerId <= 0) {
			throw new IllegalArgumentException("Invalid id(" + beerId + ")");
		}

		SQLiteDatabase db = null;
		Cursor cursor = null;
		mBeerId = beerId;

		try {
			db = DbOpenHelper.getInstance(context).getReadableDatabase();

			cursor = db.rawQuery(sql, new String[] { Long.toString(mBeerId) });

			int nBeers = cursor.getCount();

			if (nBeers == 1) {
				cursor.moveToFirst();
				mBreweryId = cursor.getInt(col_breweryid);
				mBrewery = cursor.getString(col_brewery);
				mName = cursor.getString(col_name);
				mStyle = cursor.getString(col_style);
				mAbv = cursor.getFloat(col_abv);
				mImage = cursor.getString(col_image);
				mBeerMeRating = cursor.getFloat(col_beermerating);
			} else {
				ErrLog.log(context, "Beer(" + mBeerId + ")", null,
						"Unexpected nBeers: " + nBeers);
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "Beer(" + mBeerId + ")", e,
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

	public long getId() {
		return this.mBeerId;
	}

	public long getBreweryId() {
		return this.mBreweryId;
	}

	public String getBrewery() {
		return this.mBrewery;
	}

	public String getName() {
		return this.mName;
	}

	public String getStyle() {
		return this.mStyle;
	}

	public float getAbv() {
		return this.mAbv;
	}

	public String getImage() {
		if (mImage == null || "null".equals(mImage)) {
			return "";
		}
		return mImage;
	}

	public String getUpdated() {
		return this.mUpdated;
	}

	private static final String MYRATING_SQL = "SELECT AVG(appscore+aroscore+mouscore+ovrscore) FROM beernotes WHERE beerid=?";

	public float getMyRating(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		float rating = -1;

		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = DbOpenHelper.getInstance(context).getReadableDatabase();
			cursor = db.rawQuery(MYRATING_SQL,
					new String[] { Long.toString(mBeerId) });

			if (cursor.moveToFirst()) {
				rating = cursor.getFloat(0);
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "Beer(" + mBeerId + ").getMyRating()", e,
					R.string.Database_is_busy);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return rating;
	}

	public float getBeerMeRating() {
		return this.mBeerMeRating;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("mBeerId: " + getId() + "\n");
		s.append("mBreweryId: " + getBreweryId() + "\n");
		s.append("mBrewery: " + getBrewery() + "\n");
		s.append("mName: " + getName() + "\n");
		s.append("mStyle: " + getStyle() + "\n");
		s.append("mAbv: " + getAbv() + "\n");
		s.append("mImage: " + getImage() + "\n");
		s.append("mUpdated: " + getUpdated() + "\n");
		s.append("mBeerMeRating: " + getBeerMeRating() + "\n");

		return s.toString();
	}
}