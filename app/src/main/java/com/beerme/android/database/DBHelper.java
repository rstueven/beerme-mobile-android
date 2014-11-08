package com.beerme.android.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.Utils;

/**
 * Created by rstueven on 7/6/14. Adapted from SQLiteAssetHelper.
 * https://github.com/jgilfelt/android-sqlite-asset-helper
 */
public class DBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "beerme";
	private static final int DB_VERSION = 6;
	private static final String ASSET_DB_PATH = "databases";
	private static DBHelper mInstance = null;
	private Context mContext;
	private static List<DBHelperListener> mListeners = new ArrayList<>();

	public interface DBHelperListener {
		public void onError(String msg, Exception e);
	}

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
	}

	public static DBHelper getInstance(Context context) {
		return (mInstance == null) ? new DBHelper(context) : mInstance;
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		Log.d(Utils.APPTAG, "DBHelper.onCreate() start");

		try {
			runSqlFromAssets(db, DB_NAME);
		} catch (FileNotFoundException e) {
			notifyListeners("DBHelper.onCreate(): Can't find database file", e);
		}

		Log.d(Utils.APPTAG, "DBHelper.onCreate() end");
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		Log.d(Utils.APPTAG, "DBHelper.onOpen() start");
		// TODO: Download updates
//		if (Utils.isOnline(mContext)) {
//			updateData(db, "brewery");
//			updateData(db, "beer");
//			updateData(db, "style");
//		} else {
//			Log.w(Utils.APPTAG, "Can't update data: not online");
//		}
		Log.d(Utils.APPTAG, "DBHelper.onOpen() end");
	}

	private void updateData(SQLiteDatabase db, String table) {
		Log.d(Utils.APPTAG, "Updating " + table);
		String lastUpdate = getLastUpdateByTable(db, table);
		try {
			URL url = Utils.buildUrl(table + "List.php", "t=" + lastUpdate);
			InputStream in = url.openStream();
			execSql(db, in);
			in.close();
		} catch (MalformedURLException e) {
			notifyListeners("DBHelper.onOpen() : Failed to download " + table + " updates", e);
		} catch (IOException e) {
			notifyListeners("DBHelper.onOpen() : Failed to download " + table + " updates", e);
		} catch (SQLException e) {
			notifyListeners("DBHelper.onOpen() : Failed to download " + table + " updates", e);
		}
		Log.d(Utils.APPTAG, "Done updating " + table);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		Log.d(Utils.APPTAG, String.format("onUpgrade(%d, %d)", oldVersion, newVersion));
		switch (oldVersion) {
		case 4:
			try {
				// Save old data
				runSqlFromAssets(db, DB_NAME + "-preupgrade-" + oldVersion + "-" + newVersion);
				// Copy in new database
				runSqlFromAssets(db, DB_NAME);
				// Load old data
				runSqlFromAssets(db, DB_NAME + "-postupgrade-" + oldVersion + "-" + newVersion);
			} catch (FileNotFoundException e) {
				notifyListeners("DBHelper.onUpgrade(" + oldVersion + ", " + newVersion
						+ ") failed", e);
			}
		case 5:
			try {
				// Drop old beer table
				runSqlFromAssets(db, DB_NAME + "-preupgrade-" + oldVersion + "-" + newVersion);
				// Copy in new database
				runSqlFromAssets(db, DB_NAME);
			} catch (FileNotFoundException e) {
				notifyListeners("DBHelper.onUpgrade(" + oldVersion + ", " + newVersion
						+ ") :Can't find database upgrade file", e);
			}
			break;
		default:
			throw new IllegalArgumentException("invalid oldVersion " + oldVersion);
		}

		Log.d(Utils.APPTAG, String.format("onUpgrade(%d, %d) done", oldVersion, newVersion));
	}

	public void runSqlFromAssets(SQLiteDatabase db, String filename)
			throws FileNotFoundException {
		String path = ASSET_DB_PATH + "/" + filename;
		InputStream is;
		boolean isZip = false;

		try {
			// try uncompressed
			is = mContext.getAssets().open(path);
		} catch (IOException e) {
			// try zip
			try {
				is = mContext.getAssets().open(path + ".zip");
				isZip = true;
			} catch (IOException e2) {
				// try gzip
				try {
					is = mContext.getAssets().open(path + ".gz");
				} catch (IOException e3) {
					throw new FileNotFoundException(
							"Missing "
									+ path
									+ " file (or .zip, .gz archive) in assets, or target folder not writable");
				}
			}
		}

		try {
			if (isZip) {
				ZipInputStream zis = getFileFromZip(is);
				if (zis == null) {
					throw new FileNotFoundException("Archive is missing a SQLite database file");
				}
				execSql(db, zis);
				zis.close();
			} else {
				execSql(db, is);
				is.close();
			}
		} catch (IOException e) {
			ErrLog.log(mContext, "loadDatabaseFromAssets", e,
					"Failed to load database from assets");
		}
	}

	private void execSql(SQLiteDatabase db, InputStream in) throws SQLException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;

		while ((line = reader.readLine()) != null) {
			Log.d(Utils.APPTAG, line);
			db.execSQL(line);
		}

		reader.close();
	}

	public static ZipInputStream getFileFromZip(InputStream zipFileStream) throws IOException {
		ZipInputStream zis = new ZipInputStream(zipFileStream);
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			Log.i(Utils.APPTAG, "extracting file: '" + ze.getName() + "'...");
			return zis;
		}
		return null;
	}

	protected static String getLastUpdateByTable(SQLiteDatabase db, String table) {
		String newest = null;

		Cursor cursor = db.rawQuery("SELECT MAX(updated) FROM " + table, null);
		if (cursor.moveToFirst()) {
			newest = cursor.getString(0);
		}
		cursor.close();

		return (newest == null) ? Utils.DISTANT_PAST : newest;
	}

	public static boolean registerListener(DBHelperListener listener) {
		return mListeners.add(listener);
	}

	public static boolean unRegisterListener(DBHelperListener listener) {
		return mListeners.remove(listener);
	}

	private void notifyListeners(String msg, Exception e) {
		for (DBHelperListener l : mListeners) {
			if (l != null) {
				l.onError(msg, e);
			}
		}
	}
}