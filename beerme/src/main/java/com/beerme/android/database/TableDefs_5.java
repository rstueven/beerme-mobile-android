package com.beerme.android.database;

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
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

import com.beerme.android.R;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.UrlToFileDownloader;
import com.beerme.android.utils.UrlToFileDownloader.UrlToFileDownloadListener;
import com.beerme.android.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class TableDefs_5 extends TableDefs {
    protected static final String URL_INIT_BREWERY_LIST = "breweryList.php";
    protected static final String URL_INIT_BEER_LIST = "beerList.php";
    protected static final String URL_INIT_STYLE_LIST = "styleList.php";
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
    }

    ;

    public TableDefs_5() {
        super();
    }

    @Override
    protected void initTableDefs() {
        createStatements.put(TABLE_BREWERYNOTES, "CREATE TABLE IF NOT EXISTS "
                + TABLE_BREWERYNOTES + " (" + "_id INTEGER PRIMARY KEY, "
                + "breweryid INTEGER NOT NULL, "
                + "date TEXT DEFAULT CURRENT_DATE NOT NULL, " + "rating REAL, "
                + "notes TEXT NOT NULL " + ")");
        indexStatements.put(TABLE_BREWERYNOTES,
                new String[]{createIndex(TABLE_BREWERYNOTES, "breweryid"),
                        createIndex(TABLE_BREWERYNOTES, "date")});
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
                new String[]{createIndex(TABLE_BEERNOTES, "beerid"),
                        createIndex(TABLE_BEERNOTES, "sampled")});
        createStatements.put(TABLE_BREWERY, "CREATE TABLE " + TABLE_BREWERY
                + " (" + "_id INTEGER PRIMARY KEY, " + "name TEXT NOT NULL, "
                + "address TEXT NOT NULL, " + "latitude REAL NOT NULL, "
                + "longitude REAL NOT NULL, " + "status INTEGER NOT NULL, "
                + "hours TEXT, " + "phone TEXT, " + "web TEXT, "
                + "services INTEGER NOT NULL DEFAULT 0, " + "image TEXT, "
                + "updated TEXT NOT NULL DEFAULT CURRENT_DATE" + ")");
        indexStatements.put(
                TABLE_BREWERY,
                new String[]{createIndex(TABLE_BREWERY, "name"),
                        createIndex(TABLE_BREWERY, "status"),
                        createIndex(TABLE_BREWERY, "latitude"),
                        createIndex(TABLE_BREWERY, "longitude"),
                        createIndex(TABLE_BREWERY, "updated")});
        createStatements.put(TABLE_BEER, "CREATE TABLE " + TABLE_BEER + " ("
                + "_id INTEGER PRIMARY KEY, " + "breweryid INTEGER NOT NULL, "
                + "name TEXT NOT NULL, " + "style INTEGER, " + "abv REAL, "
                + "image TEXT, " + "updated TEXT NOT NULL DEFAULT CURRENT_DATE"
                + ")");
        indexStatements.put(
                TABLE_BEER,
                new String[]{createIndex(TABLE_BEER, "breweryid"),
                        createIndex(TABLE_BEER, "name"),
                        createIndex(TABLE_BEER, "updated")});
        createStatements.put(TABLE_STYLE, "CREATE TABLE " + TABLE_STYLE + " ("
                + "_id INTEGER PRIMARY KEY, " + "name TEXT NOT NULL, "
                + "updated TEXT NOT NULL DEFAULT CURRENT_DATE" + ")");
        indexStatements.put(TABLE_STYLE,
                new String[]{createIndex(TABLE_STYLE, "updated")});
    }

    @Override
    public void upgrade(SQLiteDatabase db) {
        // Upgrade to v5
        // add brewery, beer, style
        // remove brewerynotes.name, beernotes.name, beernotes.breweryid
        Log.i(Utils.APPTAG, "Upgrading database from 4 to 5");

        mDb = db;

        upgradeOldTables(db);
        installNewTables(db);
    }

    @Override
    protected void upgradeOldTables(SQLiteDatabase db) {
        mDb = db;

        db.beginTransaction();
        DbOpenHelper.setUpdating(true);
        try {
            db.execSQL("DROP TABLE IF EXISTS 'temp'");
            db.execSQL("ALTER TABLE " + TABLE_BREWERYNOTES + " RENAME TO 'temp'");
            db.execSQL(createStatements.get(TABLE_BREWERYNOTES));
            String breweryNotesSql = "INSERT INTO " + TABLE_BREWERYNOTES
                    + " (_id, breweryid, date, rating, notes) "
                    + "SELECT id, breweryid, date, rating, notes "
                    + "FROM 'temp'";
            db.execSQL(breweryNotesSql);
            for (String indexStmt : indexStatements.get(TABLE_BREWERYNOTES)) {
                db.execSQL(indexStmt);
            }

            db.execSQL("DROP TABLE IF EXISTS 'temp'");
            db.execSQL("ALTER TABLE " + TABLE_BEERNOTES + " RENAME TO 'temp'");
            db.execSQL(createStatements.get(TABLE_BEERNOTES));
            String beerNotesSql = "INSERT INTO "
                    + TABLE_BEERNOTES
                    + " (_id, beerid, package, sampled, place, appscore, appearance, aroscore, aroma, mouscore, mouthfeel, ovrscore, notes) "
                    + " SELECT pagenumber, beerid, package, sampled, place, appscore, appearance, aroscore, aroma, mouscore, mouthfeel, ovrscore, notes "
                    + " FROM 'temp';";
            db.execSQL(beerNotesSql);
            for (String indexStmt : indexStatements.get(TABLE_BEERNOTES)) {
                db.execSQL(indexStmt);
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            ErrLog.log(mContext, "onUpgrade(4 to 5)", e, R.string.Database_upgrade_failed);
        } finally {
            db.endTransaction();
            DbOpenHelper.setUpdating(false);
        }
    }

    @Override
    protected void installNewTables(SQLiteDatabase db) {
        mDb = db;

        db.beginTransaction();
        DbOpenHelper.setUpdating(true);

        try {
            db.execSQL("DROP TABLE IF EXISTS brewery");
            db.execSQL(createStatements.get(TABLE_BREWERY));
            for (String indexStmt : indexStatements.get(TABLE_BREWERY)) {
                db.execSQL(indexStmt);
            }

            db.execSQL("DROP TABLE IF EXISTS beer");
            db.execSQL(createStatements.get(TABLE_BEER));
            for (String indexStmt : indexStatements.get(TABLE_BEER)) {
                db.execSQL(indexStmt);
            }

            db.execSQL("DROP TABLE IF EXISTS style");
            db.execSQL(createStatements.get(TABLE_STYLE));
            for (String indexStmt : indexStatements.get(TABLE_STYLE)) {
                db.execSQL(indexStmt);
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            ErrLog.log(mContext, "onUpgrade(4 to 5)", e, R.string.Database_upgrade_failed);
        } finally {
            db.endTransaction();
            DbOpenHelper.setUpdating(false);
        }
    }

    @Override
    public void updateData(SQLiteDatabase db, UpdateListener listener) {
        mDb = db;
        mListener = listener;

        String lastUpdate = getLastUpdate();
        String now = DbOpenHelper.sqlDateFormat.format(Calendar.getInstance().getTime());

        if (now.compareTo(lastUpdate) > 0) {
            HandlerThread uiThread = new HandlerThread("UIHandler");
            uiThread.start();
            UpdateHandler handler = new UpdateHandler(uiThread.getLooper(), this);
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
        private TableDefs_5 mRef;
        private Context mContext;
        private SQLiteDatabase mDb;
        private UpdateListener mListener;
        private boolean doneBrewery = false;
        private boolean doneBeer = false;
        private boolean doneStyle = false;

        public UpdateHandler(Looper looper, TableDefs_5 obj) {
            super(looper);
            WeakReference<TableDefs_5> ref = new WeakReference<TableDefs_5>(obj);
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
                    DbOpenHelper.setUpdating(true);
                    updateBreweryData(mContext, mDb, this);
                    break;
                case BREWERY_END:
                    doneBrewery = true;
                    DbOpenHelper.setUpdating(false);
                    sendEmptyMessage(BEER_START);
                    break;
                case BEER_START:
                    doneBeer = false;
                    DbOpenHelper.setUpdating(true);
                    updateBeerData(mContext, mDb, this);
                    break;
                case BEER_END:
                    doneBeer = true;
                    DbOpenHelper.setUpdating(false);
                    sendEmptyMessage(STYLE_START);
                    break;
                case STYLE_START:
                    DbOpenHelper.setUpdating(true);
                    doneStyle = false;
                    updateStyleData(mContext, mDb, this);
                    break;
                case STYLE_END:
                    doneStyle = true;
                    DbOpenHelper.setUpdating(false);
                    break;
                default:
                    super.handleMessage(msg);
            }

            if (doneBrewery && doneBeer && doneStyle) {
                setLastUpdate();
                if (mListener != null) {
                    mListener.onDataUpdated();
                }
            }
        }
    }

    ;

    private static void updateBreweryData(Context context, SQLiteDatabase db, UpdateHandler handler) {
        String lastUpdate = getLastUpdateByTable(db, "brewery");

        startDownloadProgress(context, String.format(context.getString(R.string.TABLE_data), "brewery"), TableDefs.NOTIFICATION_BREWERY_DOWNLOAD);

        URL url = null;

        try {
            url = Utils.buildUrl(URL_INIT_BREWERY_LIST, new String[]{"t=" + lastUpdate});
        } catch (MalformedURLException e) {
            ErrLog.log(context, "updateBreweryData()", e, "Malformed URL");
        }

        UrlToFileDownloader.download(context, url, new BreweryListener(context, db, handler));
    }

    private static class BreweryListener implements UrlToFileDownloadListener {
        private Context mContext;
        private SQLiteDatabase mDb;
        private static final String insertSql = "INSERT OR REPLACE INTO brewery (_id, name, address, latitude, longitude, status, hours, phone, web, services, image, updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        private static final int mNotificationDownload = TableDefs.NOTIFICATION_BREWERY_DOWNLOAD;
        private static final int mNotificationLoad = TableDefs.NOTIFICATION_BREWERY_LOAD;
        private NotificationManager mNotifyManager;
        private UpdateHandler mHandler;
        private Table mTable = Table.BREWERY;
        private String mName = mTable.getName();

        public BreweryListener(Context context, SQLiteDatabase db, UpdateHandler handler) {
            mContext = context;
            mDb = db;
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mHandler = handler;
        }

        @Override
        public void onUrlToFileDownloaded(String fileName) {
            mNotifyManager.cancel(mNotificationDownload);

            if (fileName != null) {
                if ("".equals(fileName)) {
                    throw new IllegalArgumentException("BreweryListener: empty fileName");
                }

                Builder builder = new NotificationCompat.Builder(mContext)
                        .setContentTitle(String.format(mContext.getString(R.string.TABLE_data), mName))
                        .setContentText(String.format(mContext.getString(R.string.Loading_TABLE_data), mName))
                        .setSmallIcon(R.drawable.ic_home)
                        .setProgress(0, 0, true)
                        .setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE));

                File file = new File(fileName);
                long start = 0;
                SQLiteStatement insertStatement = null;

                start = System.currentTimeMillis();

                insertStatement = mDb.compileStatement(insertSql);

                try {
                    mDb.beginTransaction();
                    parse(file, insertStatement, builder);
                    mDb.setTransactionSuccessful();
                } catch (FileNotFoundException e) {
                    ErrLog.log(mContext, "onUrlToFileDownloaded(" + fileName + "): FileNotFoundException: ", e, String.format(mContext.getString(R.string.Failed_to_update_TABLE_data), mName));
                } catch (IOException e) {
                    ErrLog.log(mContext, "onUrlToFileDownloaded(" + fileName + "): IOException: ", e, String.format(mContext
                            .getString(R.string.Failed_to_update_TABLE_data), mName));
                } finally {
                    mDb.endTransaction();
                    if (file != null) {
                        file.delete();
                    }
                    Log.i(Utils.APPTAG, "onUrlToFileDownloaded(" + mName + ") elapsed: " + (System.currentTimeMillis() - start) + " ms");
                    mNotifyManager.cancel(mNotificationLoad);
                    mHandler.sendEmptyMessage(UpdateHandler.BREWERY_END);
                }
            }
        }

        public void parse(File file, SQLiteStatement insertStatement, Builder builder) throws IOException {
            int id = -1;
            String name = null;
            String address = null;
            double latitude = -1;
            double longitude = -1;
            int status = -1;
            String hours = null;
            String phone = null;
            String web = null;
            int services = -1;
            String image = null;
            String updated = null;

            String line;
            String[] fields;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            line = reader.readLine();
            if (line != null) {
                fields = line.split("\\|", -1);
                int max = Integer.parseInt(fields[0]);
                reader.close();
                int onePercent = Math.max(max / 100, 1);

                reader = new BufferedReader(new FileReader(file));

                while ((line = reader.readLine()) != null) {
                    fields = line.split("\\|", -1);

                    id = Integer.parseInt(fields[0]);
                    if (id % onePercent == 0) {
                        builder.setProgress(max, max - id, false);
                        mNotifyManager.notify(TableDefs.NOTIFICATION_BREWERY_LOAD, builder.build());
                    }
                    name = fields[1];
                    address = fields[2];
                    latitude = Double.parseDouble(fields[3]);
                    longitude = Double.parseDouble(fields[4]);
                    status = Integer.parseInt(fields[5]);
                    services = Integer.parseInt(fields[6]);
                    updated = fields[7];
                    phone = fields[8];
                    hours = fields[9];
                    web = fields[10];
                    image = fields[11];

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

                    insertStatement.executeInsert();
                }

                reader.close();
            }
        }
    }

    private static void updateBeerData(Context context, SQLiteDatabase db, UpdateHandler handler) {
        String lastUpdate = getLastUpdateByTable(db, "beer");

        startDownloadProgress(context, String.format(context.getString(R.string.TABLE_data), "beer"), TableDefs.NOTIFICATION_BEER_DOWNLOAD);

        URL url = null;

        try {
            url = Utils.buildUrl(URL_INIT_BEER_LIST, new String[]{"t=" + lastUpdate});
        } catch (MalformedURLException e) {
            ErrLog.log(context, "updateBeerData()", e, "Malformed URL");
        }

        UrlToFileDownloader.download(context, url, new BeerListener(context, db, handler));
    }

    private static class BeerListener implements UrlToFileDownloadListener {
        private Context mContext;
        private SQLiteDatabase mDb;
        private static final String insertSql = "INSERT OR REPLACE INTO beer (_id, breweryid, name, style, abv, image, updated) VALUES (?, ?, ?, ?, ?, ?, ?)";
        private static final int mNotificationDownload = TableDefs.NOTIFICATION_BEER_DOWNLOAD;
        private static final int mNotificationLoad = TableDefs.NOTIFICATION_BEER_LOAD;
        private NotificationManager mNotifyManager;
        private UpdateHandler mHandler;
        private Table mTable = Table.BEER;
        private String mName = mTable.getName();

        public BeerListener(Context context, SQLiteDatabase db, UpdateHandler handler) {
            mContext = context;
            mDb = db;
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mHandler = handler;
        }

        @Override
        public void onUrlToFileDownloaded(String fileName) {
            mNotifyManager.cancel(mNotificationDownload);

            if (fileName != null) {
                if ("".equals(fileName)) {
                    throw new IllegalArgumentException("BeerListener: empty fileName");
                }

                Builder builder = new NotificationCompat.Builder(mContext)
                        .setContentTitle(
                                String.format(mContext.getString(R.string.TABLE_data), mName))
                        .setContentText(
                                String.format(mContext.getString(R.string.Loading_TABLE_data), mName))
                        .setSmallIcon(R.drawable.ic_home)
                        .setProgress(0, 0, true)
                        .setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE));

                File file = new File(fileName);
                long start = 0;
                SQLiteStatement insertStatement = null;

                start = System.currentTimeMillis();

                insertStatement = mDb.compileStatement(insertSql);

                try {
                    mDb.beginTransaction();
                    parse(file, insertStatement, builder);
                    mDb.setTransactionSuccessful();
                } catch (FileNotFoundException e) {
                    ErrLog.log(mContext, "onUrlToFileDownloaded(" + fileName + "): FileNotFoundException: ", e, String.format(mContext.getString(R.string.Failed_to_update_TABLE_data), mName));
                } catch (IOException e) {
                    ErrLog.log(mContext, "onUrlToFileDownloaded(" + fileName + "): IOException: ", e, String.format(mContext
                            .getString(R.string.Failed_to_update_TABLE_data), mName));
                } finally {
                    mDb.endTransaction();
                    if (file != null) {
                        file.delete();
                    }
                    Log.i(Utils.APPTAG, "onUrlToFileDownloaded(" + mName + ") elapsed: " + (System.currentTimeMillis() - start) + " ms");
                    mNotifyManager.cancel(mNotificationLoad);
                    mHandler.sendEmptyMessage(UpdateHandler.BEER_END);
                }
            }
        }

        public void parse(File file, SQLiteStatement insertStatement, Builder builder) throws IOException {
            int id = -1;
            int breweryid = -1;
            String name = null;
            int style = -1;
            double abv = -1;
            String image = null;
            String updated = null;

            String line;
            String[] fields;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            line = reader.readLine();
            if (line != null) {
                fields = line.split("\\|", -1);
                int max = Integer.parseInt(fields[0]);
                reader.close();
                int onePercent = Math.max(max / 100, 1);

                reader = new BufferedReader(new FileReader(file));

                while ((line = reader.readLine()) != null) {
                    fields = line.split("\\|", -1);

                    id = Integer.parseInt(fields[0]);
                    if (id % onePercent == 0) {
                        builder.setProgress(max, max - id, false);
                        mNotifyManager.notify(mNotificationLoad, builder.build());
                    }
                    breweryid = Integer.parseInt(fields[1]);
                    name = fields[2];
                    try {
                        style = Integer.parseInt(fields[4]);
                    } catch (NumberFormatException e) {
                        // Log.i(Utils.APPTAG, e.getLocalizedMessage());
                        style = -1;
                    }
                    try {
                        abv = Double.parseDouble(fields[5]);
                    } catch (NumberFormatException e) {
                        // Log.i(Utils.APPTAG, e.getLocalizedMessage());
                        abv = -1;
                    }
                    image = fields[6];
                    updated = fields[3];

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

                    insertStatement.executeInsert();
                }

                reader.close();
            }
        }
    }

    private static void updateStyleData(Context context, SQLiteDatabase db, UpdateHandler handler) {
        String lastUpdate = getLastUpdateByTable(db, "style");

        startDownloadProgress(context, String.format(context.getString(R.string.TABLE_data), "style"), TableDefs.NOTIFICATION_STYLE_DOWNLOAD);

        URL url = null;

        try {
            url = Utils.buildUrl(URL_INIT_STYLE_LIST, new String[]{"t=" + lastUpdate});
        } catch (MalformedURLException e) {
            ErrLog.log(context, "updateStyleData()", e, "Malformed URL");
        }

        UrlToFileDownloader.download(context, url, new StyleListener(context, db, handler));
    }

    private static class StyleListener implements UrlToFileDownloadListener {
        private Context mContext;
        private SQLiteDatabase mDb;
        private static final String insertSql = "INSERT OR REPLACE INTO style (_id, name, updated) VALUES (?, ?, ?)";
        private static final int mNotificationDownload = TableDefs.NOTIFICATION_STYLE_DOWNLOAD;
        private static final int mNotificationLoad = TableDefs.NOTIFICATION_STYLE_LOAD;
        private NotificationManager mNotifyManager;
        private UpdateHandler mHandler;
        private Table mTable = Table.STYLE;
        private String mName = mTable.getName();

        public StyleListener(Context context, SQLiteDatabase db, UpdateHandler handler) {
            mContext = context;
            mDb = db;
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mHandler = handler;
        }

        @Override
        public void onUrlToFileDownloaded(String fileName) {
            mNotifyManager.cancel(mNotificationDownload);

            if (fileName != null) {
                if ("".equals(fileName)) {
                    throw new IllegalArgumentException("BeerListener: empty fileName");
                }

                Builder builder = new NotificationCompat.Builder(mContext)
                        .setContentTitle(String.format(mContext.getString(R.string.TABLE_data), mName))
                        .setContentText(String.format(mContext.getString(R.string.Loading_TABLE_data), mName))
                        .setSmallIcon(R.drawable.ic_home)
                        .setProgress(0, 0, true)
                        .setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE));

                File file = new File(fileName);
                long start = 0;
                SQLiteStatement insertStatement = null;

                start = System.currentTimeMillis();

                insertStatement = mDb.compileStatement(insertSql);

                try {
                    mDb.beginTransaction();
                    parse(file, insertStatement, builder);
                    mDb.setTransactionSuccessful();
                } catch (FileNotFoundException e) {
                    ErrLog.log(mContext, "onUrlToFileDownloaded(" + fileName + "): FileNotFoundException: ", e, String.format(mContext.getString(R.string.Failed_to_update_TABLE_data), mName));
                } catch (IOException e) {
                    ErrLog.log(mContext, "onUrlToFileDownloaded(" + fileName + "): IOException: ", e, String.format(mContext
                            .getString(R.string.Failed_to_update_TABLE_data), mName));
                } finally {
                    mDb.endTransaction();
                    if (file != null) {
                        file.delete();
                    }
                    Log.i(Utils.APPTAG, "onUrlToFileDownloaded(" + mName + ") elapsed: " + (System.currentTimeMillis() - start) + " ms");
                    mNotifyManager.cancel(mNotificationLoad);
                    mHandler.sendEmptyMessage(UpdateHandler.STYLE_END);
                }
            }
        }

        public void parse(File file, SQLiteStatement insertStatement, Builder builder) throws IOException {
            int id = -1;
            String name = null;
            String updated = null;

            String line;
            String[] fields;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            line = reader.readLine();
            if (line != null) {
                fields = line.split("\\|", -1);
                int max = Integer.parseInt(fields[0]);
                reader.close();
                int onePercent = Math.max(max / 100, 1);

                reader = new BufferedReader(new FileReader(file));

                while ((line = reader.readLine()) != null) {
                    fields = line.split("\\|", -1);

                    id = Integer.parseInt(fields[0]);
                    if (id % onePercent == 0) {
                        builder.setProgress(max, max - id, false);
                        mNotifyManager.notify(mNotificationLoad, builder.build());
                    }
                    name = fields[1];
                    updated = fields[2];

                    insertStatement.bindLong(1, id);
                    insertStatement.bindString(2, name);
                    insertStatement.bindString(3, updated);

                    insertStatement.executeInsert();
                }

                reader.close();
            }
        }
    }
}