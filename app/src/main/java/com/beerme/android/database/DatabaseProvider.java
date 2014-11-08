package com.beerme.android.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.beerme.android.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DatabaseProvider extends ContentProvider implements DBHelper.DBHelperListener {
	public interface DatabaseProviderListener {
		public void onDatabaseInstalled();
		public void onError(String msg, Exception e);
	}

	private static List<DatabaseProviderListener> mListeners = new ArrayList<>();

	private static final String AUTHORITY = "com.beerme.android.provider";
	private DBHelper mDbHelper;
	private SQLiteDatabase mDb;
	private static boolean mInstalling = false;

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int BREWERY = 1;
	private static final int BREWERY_ID = 2;
	private static final int BREWERY_BEERS = 21;
	private static final int BEER = 3;
	private static final int BEER_ID = 4;
	private static final int STYLE = 5;
	private static final int STYLE_ID = 6;

	static {
		sUriMatcher.addURI(AUTHORITY, "brewery", BREWERY);
		sUriMatcher.addURI(AUTHORITY, "brewery/#", BREWERY_ID);
		sUriMatcher.addURI(AUTHORITY, "brewery/#/beer", BREWERY_BEERS);
		sUriMatcher.addURI(AUTHORITY, "beer", BEER);
		sUriMatcher.addURI(AUTHORITY, "beer/#", BEER_ID);
		sUriMatcher.addURI(AUTHORITY, "style", STYLE);
		sUriMatcher.addURI(AUTHORITY, "style/#", STYLE_ID);
	}

	public DatabaseProvider() {
		Log.d(Utils.APPTAG, "DatabaseProvider()");
	}

	@Override
	public boolean onCreate() {
		Log.d(Utils.APPTAG, "DatabaseProvider.onCreate()");
		Context context = getContext();
		mDbHelper = DBHelper.getInstance(context);
		DBHelper.registerListener(this);
		Thread dbStart = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(Utils.APPTAG, "Running dbStart");
				mInstalling = true;
				mDb = mDbHelper.getWritableDatabase();
				mInstalling = false;
				Log.d(Utils.APPTAG, "Ending dbStart");
				onDatabaseInstalled();
			}
		});

		Log.d(Utils.APPTAG, "Starting dbStart");
		dbStart.start();
		Log.d(Utils.APPTAG, "DatabaseProvider.onCreate() done");
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(Utils.APPTAG, "DatabaseProvider.delete(" + uri.toString() + ")");
		// TODO: Implement this to handle requests to delete one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String getType(Uri uri) {
		Log.d(Utils.APPTAG, "DatabaseProvider.getType(" + uri.toString() + ")");
		switch (sUriMatcher.match(uri)) {
		case BREWERY:
			return "vnd.android.cursor.dir/brewery";
		case BREWERY_ID:
			return "vnd.android.cursor.item/brewery";
		case BREWERY_BEERS:
			return "vnd.android.cursor.dir/beer";
		case BEER:
			return "vnd.android.cursor.dir/beer";
		case BEER_ID:
			return "vnd.android.cursor.item/beer";
		case STYLE:
			return "vnd.android.cursor.dir/style";
		case STYLE_ID:
			return "vnd.android.cursor.item/style";
		default:
			return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(Utils.APPTAG, "DatabaseProvider.insert(" + uri.toString() + ")");
		// TODO: Implement this to handle requests to insert a new row.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Log.d(Utils.APPTAG, "DatabaseProvider.query()");
		Log.d(Utils.APPTAG, "URI: " + uri.toString());
		Log.d(Utils.APPTAG, "AUTHORITY: " + uri.getAuthority());
		Log.d(Utils.APPTAG, "ENCODEDAUTHORITY: " + uri.getEncodedAuthority());
		Log.d(Utils.APPTAG, "ENCODEDFRAGMENT: " + uri.getEncodedFragment());
		Log.d(Utils.APPTAG, "ENCODEDPATH: " + uri.getEncodedPath());
		Log.d(Utils.APPTAG, "ENCODEDQUERY: " + uri.getEncodedQuery());
		Log.d(Utils.APPTAG, "ENCODEDSCHEMESPECIFICPART: " + uri.getEncodedSchemeSpecificPart());
		Log.d(Utils.APPTAG, "ENCODEDUSERINFO: " + uri.getEncodedUserInfo());
		Log.d(Utils.APPTAG, "FRAGMENT: " + uri.getFragment());
		Log.d(Utils.APPTAG, "HOST: " + uri.getHost());
		Log.d(Utils.APPTAG, "LASTPATHSEGMENT: " + uri.getLastPathSegment());
		Log.d(Utils.APPTAG, "PATH: " + uri.getPath());
		List<String> pathSegments = uri.getPathSegments();
		if (pathSegments != null) {
			for (String pathSegment : pathSegments) {
				Log.d(Utils.APPTAG, "PATHSEGMENT: " + pathSegment);
			}
		}
		Log.d(Utils.APPTAG, "PORT: " + uri.getPort());
		Log.d(Utils.APPTAG, "QUERY: " + uri.getQuery());
		Log.d(Utils.APPTAG, "SCHEME: " + uri.getScheme());
		Log.d(Utils.APPTAG, "SCHEMESPECIFICPART: " + uri.getSchemeSpecificPart());
		Log.d(Utils.APPTAG, "USERINFO: " + uri.getUserInfo());
		if (projection != null) {
			for (String proj : projection) {
				Log.d(Utils.APPTAG, "PROJECTION: " + proj);
			}
		}
		Log.d(Utils.APPTAG, "SELECTION: " + selection);
		if (selectionArgs != null) {
			for (String arg : selectionArgs) {
				Log.d(Utils.APPTAG, "SELECTIONARGS: " + arg);
			}
		}
		Log.d(Utils.APPTAG, "SORTORDER: " + sortOrder);

		switch (sUriMatcher.match(uri)) {
		case BREWERY:
			Log.d(Utils.APPTAG, "MATCHED BREWERY");
			break;
		case BREWERY_ID:
			Log.d(Utils.APPTAG, "MATCHED BREWERY_ID");
			String realSelection = "_id = " + uri.getLastPathSegment();
			if (selection != null) {
				realSelection += " AND " + selection;
			}
			return mDb.query(DatabaseProviderContract.Brewery.TABLE_NAME, projection,
					realSelection, selectionArgs, null, null, sortOrder);
		case BREWERY_BEERS:
			Log.d(Utils.APPTAG, "MATCHED BREWERY_BEERS");
			break;
		case BEER:
			Log.d(Utils.APPTAG, "MATCHED BEER");
			break;
		case BEER_ID:
			Log.d(Utils.APPTAG, "MATCHED BEER_ID");
			break;
		case STYLE:
			Log.d(Utils.APPTAG, "MATCHED STYLE");
			break;
		case STYLE_ID:
			Log.d(Utils.APPTAG, "MATCHED STYLE_ID");
			break;
		}

		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		Log.d(Utils.APPTAG, "DatabaseProvider.update(" + uri.toString() + ")");
		// TODO: Implement this to handle requests to update one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public static boolean isInstalling() {
		return mInstalling;
	}

	public static boolean registerListener(DatabaseProviderListener listener) {
		return mListeners.add(listener);
	}

	public static boolean unRegisterListener(DatabaseProviderListener listener) {
		return mListeners.remove(listener);
	}

	private void onDatabaseInstalled() {
		for (DatabaseProviderListener l : mListeners) {
			if (l != null) {
				l.onDatabaseInstalled();
			}
		}
	}

	@Override
	public void onError(String msg, Exception e) {
		for (DatabaseProviderListener l : mListeners) {
			if (l != null) {
				l.onError(msg, e);
			}
		}
	}
}