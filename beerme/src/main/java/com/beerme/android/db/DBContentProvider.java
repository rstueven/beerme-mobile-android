package com.beerme.android.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

// http://www.vogella.com/tutorials/AndroidSQLite/article.html#content-provider-and-sharing-data
public class DBContentProvider extends ContentProvider {
    private DBHelper dbHelper;
    private static final String AUTHORITY = "com.beerme.android.provider";
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int BREWERY_TABLE = 1;
    private static final int BREWERY_ROW = 2;
    private static final int BEER_TABLE = 3;
    private static final int BEER_ROW = 4;
    private static final int STYLE_TABLE = 5;
    private static final int STYLE_ROW = 6;

    static {
        matcher.addURI(AUTHORITY, "brewery", BREWERY_TABLE);
        matcher.addURI(AUTHORITY, "brewery/#", BREWERY_ROW);
        matcher.addURI(AUTHORITY, "beer", BEER_TABLE);
        matcher.addURI(AUTHORITY, "beer/#", BEER_ROW);
        matcher.addURI(AUTHORITY, "style", STYLE_TABLE);
        matcher.addURI(AUTHORITY, "style/#", STYLE_ROW);
    }

    @Override
    public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
        final int uriType = matcher.match(uri);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final int rowsDeleted;
        final String id = uri.getLastPathSegment();

        switch (uriType) {
            case BREWERY_TABLE:
                rowsDeleted = db.delete("brewery", selection, selectionArgs);
                break;
            case BREWERY_ROW:
                rowsDeleted = db.delete("brewery", "_id=" + id, null);
                break;
            case BEER_TABLE:
                rowsDeleted = db.delete("beer", selection, selectionArgs);
                break;
            case BEER_ROW:
                rowsDeleted = db.delete("beer", "_id=" + id, null);
                break;
            case STYLE_TABLE:
                rowsDeleted = db.delete("style", selection, selectionArgs);
                break;
            case STYLE_ROW:
                rowsDeleted = db.delete("style", "_id=" + id, null);
                break;
            default:
                final String msg = "DBContentProvider.delete(" + uri + "): No match (" + uriType + ")";
                throw new IllegalArgumentException(msg);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull final Uri uri) {
        // Implement this to handle requests for the MIME type of the data at the given URI.
        return null;
    }

    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {
        final int uriType = matcher.match(uri);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final String table;

        switch (uriType) {
            case BREWERY_TABLE:
                table = "brewery";
                break;
            case BEER_TABLE:
                table = "beer";
                break;
            case STYLE_TABLE:
                table = "style";
                break;
            default:
                final String msg = "DBContentProvider.insert(" + uri + "): No match (" + uriType + ")";
                throw new IllegalArgumentException(msg);
        }

        final long id = db.insert(table, null, values);
        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(table + "/" + id);
    }

    @Override
    public boolean onCreate() {
        dbHelper = DBHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(@NonNull final Uri uri, final String[] projection, final String selection,
                        final String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        checkColumns(projection);

        final int uriType = matcher.match(uri);

        switch (uriType) {
            case BREWERY_TABLE:
                builder.setTables("brewery");
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "_id ASC";
                }
                break;
            case BREWERY_ROW:
                builder.setTables("brewery");
                builder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case BEER_TABLE:
                builder.setTables("beer");
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "_id ASC";
                }
                break;
            case BEER_ROW:
                builder.setTables("beer");
                builder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case STYLE_TABLE:
                builder.setTables("style");
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "_id ASC";
                }
                break;
            case STYLE_ROW:
                builder.setTables("style");
                builder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            default:
                final String msg = "DBContentProvider.query(" + uri + "): No match (" + uriType + ")";
                throw new IllegalArgumentException(msg);
        }

        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues values, final String selection,
                      final String[] selectionArgs) {
        final int uriType = matcher.match(uri);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final int rowsUpdated;
        final String id = uri.getLastPathSegment();

        switch (uriType) {
            case BREWERY_TABLE:
                rowsUpdated = db.update("brewery", values, selection, selectionArgs);
                break;
            case BREWERY_ROW:
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update("brewery", values, "_id=" + id, null);
                } else {
                    rowsUpdated = db.update("brewery", values, "_id=" + id + " AND " + selection, null);
                }
                break;
            case BEER_TABLE:
                rowsUpdated = db.update("beer", values, selection, selectionArgs);
                break;
            case BEER_ROW:
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update("beer", values, "_id=" + id, null);
                } else {
                    rowsUpdated = db.update("beer", values, "_id=" + id + " AND " + selection, null);
                }
                break;
            case STYLE_TABLE:
                rowsUpdated = db.update("style", values, selection, selectionArgs);
                break;
            case STYLE_ROW:
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update("style", values, "_id=" + id, null);
                } else {
                    rowsUpdated = db.update("style", values, "_id=" + id + " AND " + selection, null);
                }
                break;
            default:
                final String msg = "DBContentProvider.update(" + uri + "): No match (" + uriType + ")";
                throw new IllegalArgumentException(msg);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(final String[] projection) {
        // TODO: Not implemented.
    }
}