package com.beerme.android.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;

import com.beerme.android.utils.ErrLog;
import com.beerme.android.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class BreweryList extends ArrayList<Brewery> {
	private static final long serialVersionUID = -5209730453352659633L;
	private static final String TABLE = "brewery";
	private static final float DEFAULT_DISTANCE = 100000; // 100km
	private static String[] mColumns = { "_id", "latitude", "longitude" };

	public BreweryList() {
	}

	public BreweryList(Context context, int filter, Location location) {
		this(context, filter, location, DEFAULT_DISTANCE);
	}

	public BreweryList(Context context, int filter, Location location,
			float maxDistance) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}
		if (location == null) {
			throw new IllegalArgumentException("null location");
		}

		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = DbOpenHelper.getInstance(context).getReadableDatabase();

			String selection = "(status & " + filter + ") != 0";

			cursor = db.query(TABLE, mColumns, selection, null, null, null,
					null);

			TreeMap<Float, Brewery> tm = new TreeMap<Float, Brewery>();
			Location breweryLocation;
			float distance;

			while (cursor.moveToNext()) {
				breweryLocation = new Location("");
				breweryLocation.setLatitude(cursor.getDouble(1));
				breweryLocation.setLongitude(cursor.getDouble(2));
				distance = breweryLocation.distanceTo(location);

				if (distance <= maxDistance) {
					tm.put(distance, new Brewery(context, cursor.getInt(0)));
				}
			}

			// http://www.tutorialspoint.com/java/java_treemap_class.htm
			Set<Entry<Float, Brewery>> set = tm.entrySet();
			Iterator<Entry<Float, Brewery>> iterator = set.iterator();
			while (iterator.hasNext()) {
				this.add(iterator.next().getValue());
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "BreweryList", e, R.string.Database_is_busy);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}

	public BreweryList(Context context, int filter, Location location,
			LatLngBounds bounds) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}
		if (location == null) {
			throw new IllegalArgumentException("null location");
		}
		if (bounds == null) {
			throw new IllegalArgumentException("null bounds");
		}

		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = DbOpenHelper.getInstance(context).getReadableDatabase();

			String selection = "(status & " + filter + ") != 0";

			cursor = db.query(TABLE, mColumns, selection, null, null, null,
					null);

			TreeMap<Float, Brewery> tm = new TreeMap<Float, Brewery>();
			LatLng breweryLatLng;
			Location breweryLocation;
			float distance = 0;

			while (cursor.moveToNext()) {
				breweryLatLng = new LatLng(cursor.getDouble(1),
						cursor.getDouble(2));
				breweryLocation = new Location("");
				breweryLocation.setLatitude(breweryLatLng.latitude);
				breweryLocation.setLongitude(breweryLatLng.longitude);
				distance = location.distanceTo(breweryLocation);

				if (bounds.contains(breweryLatLng)) {
					tm.put(distance, new Brewery(context, cursor.getInt(0)));
				}
			}

			// http://www.tutorialspoint.com/java/java_treemap_class.htm
			Set<Entry<Float, Brewery>> set = tm.entrySet();
			Iterator<Entry<Float, Brewery>> iterator = set.iterator();
			while (iterator.hasNext()) {
				this.add(iterator.next().getValue());
			}
		} catch (SQLiteException e) {
			ErrLog.log(context, "BreweryList", e, R.string.Database_is_busy);
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