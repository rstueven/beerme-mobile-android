package com.beerme.android_free.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.beerme.android_free.R;
import com.beerme.android_free.utils.ErrLog;
import com.beerme.android_free.utils.UrlToFileDownloader;
import com.beerme.android_free.utils.Utils;
import com.beerme.android_free.utils.UrlToFileDownloader.UrlToFileDownloadListener;

public class TableDefs_6 extends TableDefs {
	private SQLiteDatabase mDb = null;
	private UpdateListener mListener = null;

	public static enum Table {
		BREWERY("brewery"), BEER("beer"), STYLE("style");

		private final String name;

		Table(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	};

	public TableDefs_6() {
		super();
	}

	// MEDIUM: AND0103: RFE: Localize style text
	@Override
	protected void initTableDefs() {
		createStatements.put(TABLE_BREWERYNOTES, "CREATE TABLE IF NOT EXISTS "
				+ TABLE_BREWERYNOTES + " (" + "_id INTEGER PRIMARY KEY, "
				+ "breweryid INTEGER NOT NULL, "
				+ "date TEXT DEFAULT CURRENT_DATE NOT NULL, " + "rating REAL, "
				+ "notes TEXT NOT NULL " + ")");
		indexStatements.put(TABLE_BREWERYNOTES,
				new String[] { createIndex(TABLE_BREWERYNOTES, "breweryid"),
						createIndex(TABLE_BREWERYNOTES, "date") });
		createStatements.put(TABLE_BEERNOTES, "CREATE TABLE IF NOT EXISTS "
				+ TABLE_BEERNOTES + " (" + "_id INTEGER PRIMARY KEY, "
				+ "beerid INTEGER NOT NULL, " + "package TEXT DEFAULT '', "
				+ "sampled TEXT DEFAULT CURRENT_DATE, "
				+ "place TEXT DEFAULT '', " + "appscore REAL DEFAULT 0, "
				+ "appearance TEXT DEFAULT '', " + "aroscore REAL DEFAULT 0, "
				+ "aroma TEXT DEFAULT '', " + "mouscore REAL DEFAULT 0, "
				+ "mouthfeel TEXT DEFAULT '', " + "ovrscore REAL DEFAULT 0, "
				+ "notes TEXT DEFAULT ''" + ")");
		indexStatements.put(TABLE_BEERNOTES,
				new String[] { createIndex(TABLE_BEERNOTES, "beerid"),
						createIndex(TABLE_BEERNOTES, "sampled") });
		createStatements.put(TABLE_BREWERY, "CREATE TABLE " + TABLE_BREWERY
				+ " (" + "_id INTEGER PRIMARY KEY, " + "name TEXT NOT NULL, "
				+ "address TEXT NOT NULL, " + "latitude REAL NOT NULL, "
				+ "longitude REAL NOT NULL, " + "status INTEGER NOT NULL, "
				+ "hours TEXT, " + "phone TEXT, " + "web TEXT, "
				+ "services INTEGER NOT NULL DEFAULT 0, " + "image TEXT, "
				+ "updated TEXT NOT NULL DEFAULT CURRENT_DATE" + ")");
		indexStatements.put(
				TABLE_BREWERY,
				new String[] { createIndex(TABLE_BREWERY, "name"),
						createIndex(TABLE_BREWERY, "status"),
						createIndex(TABLE_BREWERY, "latitude"),
						createIndex(TABLE_BREWERY, "longitude"),
						createIndex(TABLE_BREWERY, "updated") });
		createStatements.put(TABLE_BEER, "CREATE TABLE " + TABLE_BEER + " ("
				+ "_id INTEGER PRIMARY KEY, " + "breweryid INTEGER NOT NULL, "
				+ "name TEXT NOT NULL, " + "style INTEGER, " + "abv REAL, "
				+ "image TEXT, "
				+ "updated TEXT NOT NULL DEFAULT CURRENT_DATE, "
				+ "beermerating REAL" + ")");
		indexStatements.put(
				TABLE_BEER,
				new String[] { createIndex(TABLE_BEER, "breweryid"),
						createIndex(TABLE_BEER, "name"),
						createIndex(TABLE_BEER, "updated") });
		createStatements.put(TABLE_STYLE, "CREATE TABLE " + TABLE_STYLE + " ("
				+ "_id INTEGER PRIMARY KEY, " + "name TEXT NOT NULL, "
				+ "updated TEXT NOT NULL DEFAULT CURRENT_DATE" + ")");
		indexStatements.put(TABLE_STYLE,
				new String[] { createIndex(TABLE_STYLE, "updated") });
	}

	@Override
	public void upgrade(SQLiteDatabase db) {
		// Upgrade to v6
		// add beer.beermerating
		Log.i(Utils.APPTAG, "Upgrading database from 5 to 6");

		mDb = db;

		upgradeOldTables(db);
	}

	@Override
	protected void upgradeOldTables(SQLiteDatabase db) {
		mDb = db;

		db.beginTransaction();
		DbOpenHelper.setUpdating(mContext, true);
		try {
			// Deleting all the beer data and reloading it is more efficient
			// than saving the data and downloading just the ratings.
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_BEER);
			db.execSQL(createStatements.get(TABLE_BEER));
			db.setTransactionSuccessful();

			resetLastUpdate(mContext);
		} catch (SQLException e) {
			ErrLog.log(mContext, "onUpgrade(5 to 6)", e,
					R.string.Database_upgrade_failed);
		} finally {
			db.endTransaction();
			DbOpenHelper.setUpdating(mContext, false);
		}
	}

	@Override
	public void updateData(SQLiteDatabase db, UpdateListener listener) {
		mDb = db;
		mListener = listener;

		String lastUpdate = getLastUpdate(mContext);
		String now = DbOpenHelper.sqlDateFormat.format(Calendar.getInstance()
				.getTime());

		if (now.compareTo(lastUpdate) > 0) {
			HandlerThread uiThread = new HandlerThread("UIHandler");
			uiThread.start();
			UpdateHandler handler = new UpdateHandler(uiThread.getLooper(),
					this);
			handler.sendEmptyMessage(UpdateHandler.BREWERY_START);
		} else {
			if (listener != null) {
				listener.onDataUpdated();
			}
		}
	}

	protected final static class UpdateHandler extends Handler {
		public static final int BREWERY_START = 1;
		public static final int BREWERY_END = 2;
		public static final int BEER_START = 3;
		public static final int BEER_END = 4;
		public static final int STYLE_START = 5;
		public static final int STYLE_END = 6;
		private TableDefs_6 mRef;
		private Context mContext;
		private SQLiteDatabase mDb;
		private UpdateListener mListener;
		private boolean doneBrewery = false;
		private boolean doneBeer = false;
		private boolean doneStyle = false;

		public UpdateHandler(Looper looper, TableDefs_6 obj) {
			super(looper);
			WeakReference<TableDefs_6> ref = new WeakReference<TableDefs_6>(obj);
			mRef = ref.get();
			mContext = mRef.mContext;
			mDb = mRef.mDb;
			mListener = mRef.mListener;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BREWERY_START:
				doneBrewery = false;
				DbOpenHelper.setUpdating(mContext, true);
				updateDataByTable(mContext, mDb, Table.BREWERY, this);
				break;
			case BREWERY_END:
				doneBrewery = true;
				DbOpenHelper.setUpdating(mContext, false);
				sendEmptyMessage(BEER_START);
				break;
			case BEER_START:
				doneBeer = false;
				DbOpenHelper.setUpdating(mContext, true);
				updateDataByTable(mContext, mDb, Table.BEER, this);
				break;
			case BEER_END:
				doneBeer = true;
				DbOpenHelper.setUpdating(mContext, false);
				sendEmptyMessage(STYLE_START);
				break;
			case STYLE_START:
				DbOpenHelper.setUpdating(mContext, true);
				doneStyle = false;
				updateDataByTable(mContext, mDb, Table.STYLE, this);
				break;
			case STYLE_END:
				doneStyle = true;
				DbOpenHelper.setUpdating(mContext, false);
				break;
			default:
				super.handleMessage(msg);
			}

			if (doneBrewery && doneBeer && doneStyle) {
				setLastUpdate(mContext);
				if (mListener != null) {
					mListener.onDataUpdated();
				}
			}
		}
	};

	protected static final String URL_INIT_BREWERY_LIST = "breweryList.php";
	protected static final String URL_INIT_BEER_LIST = "beerList.php";
	protected static final String URL_INIT_STYLE_LIST = "styleList.php";

	private static void updateDataByTable(Context context, SQLiteDatabase db,
			Table table, UpdateHandler handler) {
		int notification = -1;
		String urlInit = null;

		switch (table) {
		case BREWERY:
			notification = TableDefs.NOTIFICATION_BREWERY_DOWNLOAD;
			urlInit = URL_INIT_BREWERY_LIST;
			break;
		case BEER:
			notification = TableDefs.NOTIFICATION_BEER_DOWNLOAD;
			urlInit = URL_INIT_BEER_LIST;
			break;
		case STYLE:
			notification = TableDefs.NOTIFICATION_STYLE_DOWNLOAD;
			urlInit = URL_INIT_STYLE_LIST;
			break;
		default:
			throw new IllegalArgumentException("invalid table: "
					+ table.getName());
		}

		String tableName = table.getName();
		String lastUpdate = getLastUpdateByTable(db, tableName);

		startDownloadProgress(context, String.format(
				context.getString(R.string.TABLE_data), tableName),
				notification);

		URL url = null;

		try {
			url = Utils.buildUrl(urlInit, new String[] { "t=" + lastUpdate });
			UrlToFileDownloader.download(context, url,
					TableDataListener.getInstance(context, db, table, handler));
		} catch (MalformedURLException e) {
			ErrLog.log(context, "updateDataByTable(" + tableName + ")", e,
					"Malformed URL");
		}
	}

	private static abstract class TableDataListener implements
			UrlToFileDownloadListener {
		private static final String INSERT_BREWERY = "INSERT OR REPLACE INTO brewery (_id, name, address, latitude, longitude, status, hours, phone, web, services, image, updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		private static final String INSERT_BEER = "INSERT OR REPLACE INTO beer (_id, breweryid, name, style, abv, image, updated, beermerating) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		private static final String INSERT_STYLE = "INSERT OR REPLACE INTO style (_id, name, updated) VALUES (?, ?, ?)";

		private Context mContext = null;
		private SQLiteDatabase mDb = null;
		private Table mTable = null;
		private String mName = null;
		private UpdateHandler mHandler = null;
		private NotificationManager mNotifyManager = null;
		private String mInsertSql = null;
		private int mNotificationDownload = -1;
		private int mNotificationLoad = -1;
		private int mNextStep = -1;

		public TableDataListener(Context context, SQLiteDatabase db,
				Table table, UpdateHandler handler) {
			mContext = context;
			mDb = db;
			mTable = table;
			mHandler = handler;

			mNotifyManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);

			switch (table) {
			case BREWERY:
				mInsertSql = INSERT_BREWERY;
				mNotificationDownload = TableDefs.NOTIFICATION_BREWERY_DOWNLOAD;
				mNotificationLoad = TableDefs.NOTIFICATION_BREWERY_LOAD;
				mNextStep = UpdateHandler.BREWERY_END;
				break;
			case BEER:
				mInsertSql = INSERT_BEER;
				mNotificationDownload = TableDefs.NOTIFICATION_BEER_DOWNLOAD;
				mNotificationLoad = TableDefs.NOTIFICATION_BEER_LOAD;
				mNextStep = UpdateHandler.BEER_END;
				break;
			case STYLE:
				mInsertSql = INSERT_STYLE;
				mNotificationDownload = TableDefs.NOTIFICATION_STYLE_DOWNLOAD;
				mNotificationLoad = TableDefs.NOTIFICATION_STYLE_LOAD;
				mNextStep = UpdateHandler.STYLE_END;
				break;
			default:
				throw new IllegalArgumentException("invalid table: "
						+ table.getName());
			}

			mName = mTable.getName();
		}

		public static TableDataListener getInstance(Context context,
				SQLiteDatabase db, Table table, UpdateHandler handler) {
			switch (table) {
			case BREWERY:
				return new TableDataListener_Brewery(context, db, table,
						handler);
			case BEER:
				return new TableDataListener_Beer(context, db, table, handler);
			case STYLE:
				return new TableDataListener_Style(context, db, table, handler);
			default:
				throw new IllegalArgumentException("invalid table: "
						+ table.getName());
			}
		}

		@Override
		public void onUrlToFileDownloaded(String fileName) {
			mNotifyManager.cancel(mNotificationDownload);

			if (fileName != null) {
				if ("".equals(fileName)) {
					throw new IllegalArgumentException(
							"TableDataListener: empty fileName");
				}

				Builder builder = new NotificationCompat.Builder(mContext)
						.setContentTitle(
								String.format(
										mContext.getString(R.string.TABLE_data),
										mName))
						.setContentText(
								String.format(
										mContext.getString(R.string.Loading_TABLE_data),
										mName))
						.setSmallIcon(R.drawable.ic_home)
						.setProgress(0, 0, true)
						.setContentIntent(
								PendingIntent.getActivity(mContext, 0,
										new Intent(), 0));

				File file = new File(fileName);
				long start = 0;
				SQLiteStatement insertStatement = null;

				start = System.currentTimeMillis();

				insertStatement = mDb.compileStatement(mInsertSql);

				try {
					mDb.beginTransaction();
					parse(file, insertStatement, builder);
					mDb.setTransactionSuccessful();
				} catch (FileNotFoundException e) {
					ErrLog.log(
							mContext,
							"onUrlToFileDownloaded(" + fileName
									+ "): FileNotFoundException: ",
							e,
							String.format(
									mContext.getString(R.string.Failed_to_update_TABLE_data),
									mName));
				} catch (IOException e) {
					ErrLog.log(mContext, "onUrlToFileDownloaded(" + fileName
							+ "): IOException: ", e, String.format(mContext
							.getString(R.string.Failed_to_update_TABLE_data),
							mName));
				} finally {
					mDb.endTransaction();
					if (file != null) {
						file.delete();
					}
					Log.i(Utils.APPTAG, "onUrlToFileDownloaded(" + mName
							+ ") elapsed: "
							+ (System.currentTimeMillis() - start) + " ms");
					mNotifyManager.cancel(mNotificationLoad);
					mHandler.sendEmptyMessage(mNextStep);
				}
			}
		}

		public void parse(File file, SQLiteStatement insertStatement,
				Builder builder) throws IOException {
			String[] fields;
			BufferedReader reader = new BufferedReader(new FileReader(file));

			String line = reader.readLine();
			if (line != null) {
				fields = line.split("\\|", -1);
				int max = Integer.parseInt(fields[0]);
				reader.close();
				int onePercent = Math.max(max / 100, 1);

				reader = new BufferedReader(new FileReader(file));

				int id = 0;

				while ((line = reader.readLine()) != null) {
					fields = line.split("\\|", -1);

					id = Integer.parseInt(fields[0]);
					if (id % onePercent == 0) {
						builder.setProgress(max, max - id, false);
						mNotifyManager.notify(mNotificationLoad,
								builder.build());
					}

					parseFields(id, fields, insertStatement);

					insertStatement.executeInsert();
				}

				reader.close();
			}
		}

		public abstract void parseFields(int id, String[] fields,
				SQLiteStatement insertStatement);
	}

	private static class TableDataListener_Brewery extends TableDataListener {
		public TableDataListener_Brewery(Context context, SQLiteDatabase db,
				Table table, UpdateHandler handler) {
			super(context, db, table, handler);
		}

		@Override
		public void parseFields(int id, String[] fields,
				SQLiteStatement insertStatement) {
			String name = fields[1];
			String address = fields[2];
			double latitude = Double.parseDouble(fields[3]);
			double longitude = Double.parseDouble(fields[4]);
			int status = Integer.parseInt(fields[5]);
			int services = Integer.parseInt(fields[6]);
			String updated = fields[7];
			String phone = fields[8];
			String hours = fields[9];
			String web = fields[10];
			String image = fields[11];

			insertStatement.bindLong(1, id);
			insertStatement.bindString(2, name);
			insertStatement.bindString(3, address);
			insertStatement.bindDouble(4, latitude);
			insertStatement.bindDouble(5, longitude);
			insertStatement.bindLong(6, status);
			insertStatement.bindString(7, hours);
			insertStatement.bindString(8, phone);
			insertStatement.bindString(9, web);
			insertStatement.bindLong(10, services);
			insertStatement.bindString(11, image);
			insertStatement.bindString(12, updated);
		}
	}

	private static class TableDataListener_Beer extends TableDataListener {
		public TableDataListener_Beer(Context context, SQLiteDatabase db,
				Table table, UpdateHandler handler) {
			super(context, db, table, handler);
		}

		@Override
		public void parseFields(int id, String[] fields,
				SQLiteStatement insertStatement) {
			int breweryid = Integer.parseInt(fields[1]);
			String name = fields[2];

			int style = -1;
			try {
				style = Integer.parseInt(fields[4]);
			} catch (NumberFormatException e) {
				style = -1;
			}

			double abv = -1;
			try {
				abv = Double.parseDouble(fields[5]);
			} catch (NumberFormatException e) {
				abv = -1;
			}

			String image = fields[6];
			String updated = fields[3];

			double beermerating = -1;
			try {
				beermerating = Double.parseDouble(fields[7]);
			} catch (NumberFormatException e) {
				beermerating = -1;
			}

			insertStatement.bindLong(1, id);
			insertStatement.bindLong(2, breweryid);
			insertStatement.bindString(3, name);
			if (style >= 0) {
				insertStatement.bindLong(4, style);
			} else {
				insertStatement.bindNull(4);
			}
			if (abv >= 0) {
				insertStatement.bindDouble(5, abv);
			} else {
				insertStatement.bindNull(5);
			}
			insertStatement.bindString(6, image);
			insertStatement.bindString(7, updated);
			if (beermerating >= 0) {
				insertStatement.bindDouble(8, beermerating);
			} else {
				insertStatement.bindNull(8);
			}
		}
	}

	private static class TableDataListener_Style extends TableDataListener {
		public TableDataListener_Style(Context context, SQLiteDatabase db,
				Table table, UpdateHandler handler) {
			super(context, db, table, handler);
		}

		@Override
		public void parseFields(int id, String[] fields,
				SQLiteStatement insertStatement) {
			String name = fields[1];
			String updated = fields[2];

			insertStatement.bindLong(1, id);
			insertStatement.bindString(2, name);
			insertStatement.bindString(3, updated);
		}
	}
}