package com.beerme.android_free.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.beerme.android_free.R;
import com.beerme.android_free.utils.ErrLog;

public class BeerNote_My extends BeerNote {
	private static final long serialVersionUID = 4442839551869908963L;
	private static final String TABLE = "beernotes";
	private static String[] mColumns = { "_id", "beerid", "package", "sampled",
			"place", "appscore", "appearance", "aroscore", "aroma", "mouscore",
			"mouthfeel", "ovrscore", "notes" };

	public BeerNote_My(Context context, long noteId) {
		super(context, noteId);
		this.mSource = Source.MY;
	}

	@Override
	public void load(Context context) {
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = DbOpenHelper.getInstance(context).getReadableDatabase();

			cursor = db.query(TABLE, mColumns, "_id=" + id, null, null, null,
					"_id");

			if (cursor.moveToFirst()) {
				beerid = cursor.getLong(cursor.getColumnIndexOrThrow("beerid"));
				pkg = cursor.getString(cursor.getColumnIndexOrThrow("package"));
				sampled = cursor.getString(cursor
						.getColumnIndexOrThrow("sampled"));
				place = cursor.getString(cursor.getColumnIndexOrThrow("place"));
				appscore = cursor.getFloat(cursor
						.getColumnIndexOrThrow("appscore"));
				appearance = cursor.getString(cursor
						.getColumnIndexOrThrow("appearance"));
				aroscore = cursor.getFloat(cursor
						.getColumnIndexOrThrow("aroscore"));
				aroma = cursor.getString(cursor.getColumnIndexOrThrow("aroma"));
				mouscore = cursor.getFloat(cursor
						.getColumnIndexOrThrow("mouscore"));
				mouthfeel = cursor.getString(cursor
						.getColumnIndexOrThrow("mouthfeel"));
				ovrscore = cursor.getFloat(cursor
						.getColumnIndexOrThrow("ovrscore"));
				notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "BeerNote_My.load(" + id + ")", e,
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
	public void save(Context context) {
		if (this.beerid <= 0) {
			throw new IllegalArgumentException("Bad beerid: " + this.beerid);
		}

		SQLiteDatabase db = null;

		try {
			db = DbOpenHelper.getInstance(context).getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put("beerid", this.beerid);
			values.put("package", this.pkg);
			values.put("sampled", this.sampled);
			values.put("place", this.place);
			values.put("appscore", this.appscore);
			values.put("appearance", this.appearance);
			values.put("aroscore", this.aroscore);
			values.put("aroma", this.aroma);
			values.put("mouscore", this.mouscore);
			values.put("mouthfeel", this.mouthfeel);
			values.put("ovrscore", this.ovrscore);
			values.put("notes", this.notes);

			if (this.id == -1) {
				this.id = (long) db.insert(TABLE, null, values);
			} else {
				db.update(TABLE, values, "_id=" + this.id, null);
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "BeerNote_My.save(" + id + ")", e,
					R.string.Database_is_busy);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	@Override
	public void delete(Context context) {
		SQLiteDatabase db = null;

		try {
			db = DbOpenHelper.getInstance(context).getReadableDatabase();
			db.delete(TABLE, "_id=" + this.id, null);
		} catch (SQLiteException e) {
			ErrLog.log(context, "BeerNote_My.delete(" + id + ")", e,
					R.string.Database_is_busy);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
}