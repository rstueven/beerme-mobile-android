package com.beerme.android.database;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.beerme.android.utils.ErrLog;
import com.beerme.android.R;

public class BreweryNote implements Serializable {
	private static final long serialVersionUID = -8656135024844476668L;
	protected long id = -1;
	protected long breweryid = -1;
	protected String date;
	protected float rating;
	protected String notes;
	private static final String TABLE = "brewerynotes";
	private static String[] mColumns = { "_id", "breweryid", "date", "rating",
			"notes" };

	public BreweryNote(Context context, long noteId) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		if (noteId > 0) {
			this.id = noteId;

			load(context);
		}
	}

	public static BreweryNote newInstance(Context context, long noteId) {
		return new BreweryNote(context, noteId);
	}

	public void load(Context context) {
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = DbOpenHelper.getInstance(context).getReadableDatabase();

			cursor = db.query(TABLE, mColumns, "_id=" + id, null, null, null,
					"_id");

			if (cursor.moveToFirst()) {
				breweryid = cursor.getInt(cursor
						.getColumnIndexOrThrow("breweryid"));
				date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
				rating = cursor
						.getFloat(cursor.getColumnIndexOrThrow("rating"));
				notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "BreweryNote(" + id + ").load()", e,
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

	public void save(Context context) {
		if (this.breweryid <= 0) {
			throw new IllegalArgumentException("Bad breweryid: "
					+ this.breweryid);
		}

		SQLiteDatabase db = null;

		try {
			db = DbOpenHelper.getInstance(context).getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put("breweryid", this.breweryid);
			values.put("date", this.date);
			values.put("rating", this.rating);
			values.put("notes", this.notes);

			if (this.id == -1) {
				this.id = (int) db.insert(TABLE, null, values);
			} else {
				db.update(TABLE, values, "_id=" + this.id, null);
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "BreweryNote(" + id + ").save()", e,
					R.string.Database_is_busy);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void delete(Context context) {

		SQLiteDatabase db = null;

		try {
			db = DbOpenHelper.getInstance(context).getWritableDatabase();

			db.delete(TABLE, "_id=" + this.id, null);
		} catch (SQLiteException e) {
			ErrLog.log(context, "BreweryNote(" + id + ").delete()", e,
					R.string.Database_is_busy);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public long getId() {
		return this.id;
	}

	public long getBreweryId() {
		return this.breweryid;
	}

	public String getDate() {
		String s = "";

		try {
			SimpleDateFormat rawFormat = new SimpleDateFormat("yyyy-MM-dd",
					Locale.getDefault());
			Date d = rawFormat.parse(this.date);
			DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
					Locale.getDefault());
			s = df.format(d);
		} catch (ParseException e) {
			s = "";
		}
		return s;
	}

	public float getRating() {
		return this.rating;
	}

	public String getNotes() {
		return this.notes;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("_id: " + getId() + "\n");
		s.append("breweryID: " + getBreweryId() + "\n");
		s.append("date: " + getDate() + "\n");
		s.append("rating: " + getRating() + "\n");
		s.append("notes: " + getNotes() + "\n");

		return s.toString();
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setBreweryId(long breweryId) {
		this.breweryid = breweryId;
	}

	public void setDate(String date) {
		// date is formatted according to the default Locale
		// We want to format it according to yyyy-MM-dd
		String dateString = date;

		try {
			DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
					Locale.getDefault());
			Date d = df.parse(dateString);
			SimpleDateFormat rawFormat = new SimpleDateFormat("yyyy-MM-dd",
					Locale.getDefault());
			dateString = rawFormat.format(d);
		} catch (ParseException e) {
			dateString = "";
		} finally {
			this.date = dateString;
		}
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}