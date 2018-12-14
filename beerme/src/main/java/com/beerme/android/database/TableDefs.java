package com.beerme.android.database;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.beerme.android.R;
import com.beerme.android.utils.SharedPref;
import com.beerme.android.utils.Utils;

import java.util.Calendar;
import java.util.HashMap;

import static com.beerme.android.utils.SharedPref.Pref.KEY_DB_LAST_UPDATE;
import static com.beerme.android.utils.SharedPref.Pref.KEY_DB_UPDATING;

public abstract class TableDefs extends Fragment {
    public static final String TABLE_BREWERYNOTES = "brewerynotes";
    public static final String TABLE_BEERNOTES = "beernotes";
    public static final String TABLE_BREWERY = "brewery";
    public static final String TABLE_BEER = "beer";
    public static final String TABLE_STYLE = "style";
    public static final int NOTIFICATION_BREWERY_DOWNLOAD = 1;
    public static final int NOTIFICATION_BREWERY_LOAD = 2;
    public static final int NOTIFICATION_BEER_DOWNLOAD = 3;
    public static final int NOTIFICATION_BEER_LOAD = 4;
    public static final int NOTIFICATION_STYLE_DOWNLOAD = 5;
    public static final int NOTIFICATION_STYLE_LOAD = 6;
    protected Context mContext;
    protected NotificationManager mNotifyManager = null;
    protected final static HashMap<String, String> createStatements = new HashMap<String, String>();
    protected final static HashMap<String, String[]> indexStatements = new HashMap<String, String[]>();

    public interface UpdateListener {
        public void onDataUpdated();
    }

    public TableDefs() {
        mContext = DbOpenHelper.getContext();
        initTableDefs();
    }

    public final static TableDefs newInstance(int version) {
        TableDefs tableDefs = null;

        switch (version) {
            case 1:
                tableDefs = new TableDefs_1();
                break;
            case 2:
                tableDefs = new TableDefs_2();
                break;
            case 3:
                tableDefs = new TableDefs_3();
                break;
            case 4:
                tableDefs = new TableDefs_4();
                break;
            case 5:
                tableDefs = new TableDefs_5();
                break;
            case 6:
                tableDefs = new TableDefs_6();
                break;
            default:
                throw new IllegalArgumentException("Invalid version: " + version);
        }

        return tableDefs;
    }

    protected abstract void initTableDefs();

    protected void upgrade(SQLiteDatabase db) {
        Log.i(Utils.APPTAG, "upgrade(): default implementation");
    }

    public void updateData(SQLiteDatabase db, UpdateListener listener) {
        Log.i(Utils.APPTAG, "updateData(): default implementation");
        if (listener != null) {
            listener.onDataUpdated();
        }
    }

    protected void upgradeOldTables(SQLiteDatabase db) {
        Log.i(Utils.APPTAG, "upgradeOldTables(): default implementation");
    }

    protected void installNewTables(SQLiteDatabase db) {
        Log.i(Utils.APPTAG, "installNewTables(): default implementation");
    }

    protected final HashMap<String, String> getCreateStatements() {
        return createStatements;
    }

    protected final HashMap<String, String[]> getIndexStatements() {
        return indexStatements;
    }

    protected static String createIndex(String table, String column) {
        return createIndex(table, column, new String[]{column});
    }

    protected static String[] getColumns(SQLiteDatabase db, String tableName) {
        String[] ar = null;

        Cursor c = db.rawQuery("select * from " + tableName + " limit 1", null);

        if (c != null) {
            ar = c.getColumnNames();
            c.close();
        }

        return ar;
    }

    protected static String createIndex(String table, String name,
                                        String[] columns) {
        StringBuffer buffer = new StringBuffer("CREATE INDEX " + table + "_" + name + " ON " + table + " (");

        int n = columns.length;
        boolean first = true;

        for (int i = 0; i < n; i++) {
            if (!first) {
                buffer.append(", ");
            }

            first = false;

            buffer.append(columns[i]);
        }

        buffer.append(')');

        return buffer.toString();
    }

    protected static void setLastUpdate() {
        SharedPref.write(KEY_DB_LAST_UPDATE, DbOpenHelper.sqlDateFormat.format(Calendar.getInstance().getTime()));
    }

    protected static String getLastUpdate() {
        return SharedPref.read(KEY_DB_LAST_UPDATE, Utils.DISTANT_PAST);
    }

    protected static void resetLastUpdate() {
        SharedPref.write(KEY_DB_LAST_UPDATE, Utils.DISTANT_PAST);
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

    protected static void startDownloadProgress(Context context, int title, int id) {
        startDownloadProgress(context, context.getString(title), id);
    }

    protected static void startDownloadProgress(Context context, String title,
                                                int id) {
        Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(context.getText(R.string.Download_in_progress))
                .setSmallIcon(R.drawable.ic_home)
                .setProgress(0, 0, true)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, builder.build());
    }

    public static boolean isUpdating() {
        return SharedPref.read(KEY_DB_UPDATING, false);
    }
}