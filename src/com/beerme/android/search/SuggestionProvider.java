package com.beerme.android.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.beerme.android.database.DbOpenHelper;
import com.beerme.android.database.TableDefs;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.Utils;
import com.beerme.android.R;

// http://mobile.tutsplus.com/tutorials/android/android-sdk_content-providers/

public class SuggestionProvider extends ContentProvider {
	private static final String AUTHORITY = "com.beerme.android.search.SuggestionProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/suggestion");
	private static final int DEFAULT_LIMIT = 10;
	public static final int SUGGESTIONS = 1;
	public static final int GET = 3;

	private UriMatcher mUriMatcher = buildUriMatcher();

	private UriMatcher buildUriMatcher() {
		UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SUGGESTIONS);
		uriMatcher.addURI(AUTHORITY, "breweries/#", GET); // What's this for?
		return uriMatcher;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (mUriMatcher.match(uri)) {
		case SUGGESTIONS:
			return getSuggestions(selectionArgs);
		case GET:
			// What's this for?
			return get(uri.getLastPathSegment());
		default:
			return getSuggestions(selectionArgs, -1);
		}
	}

	public Cursor getSuggestions(String[] selectionArgs) {
		return getSuggestions(selectionArgs, DEFAULT_LIMIT);
	}

	private static final String[] COLUMNS = new String[] { "_ID",
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
			SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA };

	public Cursor getSuggestions(String[] selectionArgs, int limit) {
		ArrayList<Row> map = new ArrayList<Row>();
		String queryString = "";

		if (selectionArgs != null) {
			queryString = selectionArgs[0];
			selectionArgs[0] = "%" + queryString + "%";
		}

		if (limit <= 0) {
			limit = Integer.MAX_VALUE;
		}

		String breweryQuery = "SELECT _id AS _ID, name AS "
				+ SearchManager.SUGGEST_COLUMN_TEXT_1 + ", address AS "
				+ SearchManager.SUGGEST_COLUMN_TEXT_2 + ", _id AS "
				+ SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID + ", null AS "
				+ SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
				+ " FROM brewery WHERE name LIKE ? ORDER BY name LIMIT "
				+ limit;

		SQLiteDatabase db = null;
		Cursor breweryCursor = null;
		Cursor beerCursor = null;

		try {
			db = DbOpenHelper.getInstance(getContext()).getReadableDatabase();

			breweryCursor = db.rawQuery(breweryQuery, selectionArgs);

			while (breweryCursor.moveToNext()) {
				map.add(new Row(breweryCursor.getLong(0), breweryCursor
						.getString(1), breweryCursor.getString(2),
						breweryCursor.getLong(3), null));
			}

			String beerQuery = "SELECT -beer._id AS _ID, beer.name AS "
					+ SearchManager.SUGGEST_COLUMN_TEXT_1
					+ ", brewery.name AS "
					+ SearchManager.SUGGEST_COLUMN_TEXT_2
					+ ", -beer._id AS "
					+ SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
					+ ", null AS "
					+ SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
					+ " FROM beer LEFT JOIN brewery ON beer.breweryid = brewery._id WHERE beer.name LIKE ? ORDER BY beer.name LIMIT "
					+ limit;

			beerCursor = db.rawQuery(beerQuery, selectionArgs);

			while (beerCursor.moveToNext()) {
				map.add(new Row(beerCursor.getLong(0), beerCursor.getString(1),
						beerCursor.getString(2), beerCursor.getLong(3), null));
			}
		} catch (SQLiteException e) {
			ErrLog.log(getContext(), "SuggestionProvider.getSuggestions()", e,
					R.string.Database_is_busy);
		} finally {
			if (breweryCursor != null) {
				breweryCursor.close();
			}
			if (beerCursor != null) {
				beerCursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		map.addAll(new PlaceList(getContext(), queryString));

		Collections.sort(map, new Comparator<Row>() {
			@Override
			public int compare(Row lhs, Row rhs) {
				long lhsId = lhs.id;
				String lhsCol1 = lhs.col1;
				long rhsId = rhs.id;
				String rhsCol1 = rhs.col1;

				if (Math.abs(lhsId) > 0 && Math.abs(rhsId) > 0) {
					// Brewery/Beer vs Brewery/Beer
					return lhsCol1.compareToIgnoreCase(rhsCol1);
				} else if (lhsId == 0 && rhsId != 0) {
					// Location vs Brewery/Beer
					return 1;
				} else if (lhsId != 0 && rhsId == 0) {
					// Brewery/Beer vs Location
					return -1;
				} else if (lhsId == 0 && rhsId == 0) {
					// Location vs Location
					return lhsCol1.compareToIgnoreCase(rhsCol1);
				} else {
					Log.w(Utils.APPTAG, "SuggestionProvider.compare(): WTF");
					Log.w(Utils.APPTAG, "\n\n" + lhs + "\n" + rhs + "\n\n");
					return 0;
				}
			}
		});

		MatrixCursor matrix = new MatrixCursor(COLUMNS);

		for (Row row : map) {
			matrix.addRow(new Object[] { Long.valueOf(row.id), row.col1,
					row.col2, Long.valueOf(row.dataId), row.location });
		}

		return matrix;
	}

	public Cursor get(String id) {
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = DbOpenHelper.getInstance(getContext()).getReadableDatabase();
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			builder.setTables(TableDefs.TABLE_BREWERY);
			cursor = builder.query(db,
					new String[] { "_id", "name", "address" }, "_id=?",
					new String[] { id }, null, null, null, "1");
		} catch (SQLiteException e) {
			ErrLog.log(getContext(), "SuggestionProvider.get(" + id + ")", e,
					R.string.Database_is_busy);
		}

		// Don't close the Cursor!
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
}