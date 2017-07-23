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

import java.util.Arrays;
import java.util.HashSet;

// http://www.vogella.com/tutorials/AndroidSQLite/article.html#content-provider-and-sharing-data
public class DBContentProvider extends ContentProvider {
    private DBHelper dbHelper;
    private static final String AUTHORITY = DBContract.AUTHORITY;
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String BREWERY_TABLE = DBContract.Brewery.TABLE_NAME;
    private static final String BEER_TABLE = DBContract.Beer.TABLE_NAME;
    private static final String STYLE_TABLE = DBContract.Style.TABLE_NAME;

    private static final int CODE_BREWERY_TABLE = 1;
    private static final int CODE_BREWERY_ROW = 2;
    private static final int CODE_BEER_TABLE = 3;
    private static final int CODE_BEER_ROW = 4;
    private static final int CODE_STYLE_TABLE = 5;
    private static final int CODE_STYLE_ROW = 6;

    static {
        matcher.addURI(AUTHORITY, BREWERY_TABLE, CODE_BREWERY_TABLE);
        matcher.addURI(AUTHORITY, BREWERY_TABLE + "/#", CODE_BREWERY_ROW);
        matcher.addURI(AUTHORITY, BEER_TABLE, CODE_BEER_TABLE);
        matcher.addURI(AUTHORITY, BEER_TABLE + "/#", CODE_BEER_ROW);
        matcher.addURI(AUTHORITY, STYLE_TABLE, CODE_STYLE_TABLE);
        matcher.addURI(AUTHORITY, STYLE_TABLE + "/#", CODE_STYLE_ROW);
    }

    @Override
    public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
        final int uriType = matcher.match(uri);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final int rowsDeleted;
        final String id = uri.getLastPathSegment();

        switch (uriType) {
            case CODE_BREWERY_TABLE:
                rowsDeleted = db.delete(BREWERY_TABLE, selection, selectionArgs);
                break;
            case CODE_BREWERY_ROW:
                rowsDeleted = db.delete(BREWERY_TABLE, "_id=" + id, null);
                break;
            case CODE_BEER_TABLE:
                rowsDeleted = db.delete(BEER_TABLE, selection, selectionArgs);
                break;
            case CODE_BEER_ROW:
                rowsDeleted = db.delete(BEER_TABLE, "_id=" + id, null);
                break;
            case CODE_STYLE_TABLE:
                rowsDeleted = db.delete(STYLE_TABLE, selection, selectionArgs);
                break;
            case CODE_STYLE_ROW:
                rowsDeleted = db.delete(STYLE_TABLE, "_id=" + id, null);
                break;
            default:
                final String msg = "DBContentProvider.delete(" + uri + "): No match (" + uriType + ")";
                throw new IllegalArgumentException(msg);
        }

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull final Uri uri) {
        final int uriType = matcher.match(uri);

        switch (uriType) {
            case CODE_BREWERY_TABLE:
                return DBContract.Brewery.CONTENT_TYPE;
            case CODE_BREWERY_ROW:
                return DBContract.Brewery.CONTENT_ITEM_TYPE;
            case CODE_BEER_TABLE:
                return DBContract.Beer.CONTENT_TYPE;
            case CODE_BEER_ROW:
                return DBContract.Beer.CONTENT_ITEM_TYPE;
            case CODE_STYLE_TABLE:
                return DBContract.Style.CONTENT_TYPE;
            case CODE_STYLE_ROW:
                return DBContract.Style.CONTENT_ITEM_TYPE;
            default:
                final String msg = "DBContentProvider.getType(" + uri + "): No match (" + uriType + ")";
                throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {
        final int uriType = matcher.match(uri);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final String table;

        switch (uriType) {
            case CODE_BREWERY_TABLE:
                table = BREWERY_TABLE;
                break;
            case CODE_BEER_TABLE:
                table = BEER_TABLE;
                break;
            case CODE_STYLE_TABLE:
                table = STYLE_TABLE;
                break;
            default:
                final String msg = "DBContentProvider.insert(" + uri + "): No match (" + uriType + ")";
                throw new IllegalArgumentException(msg);
        }

        final long id = db.insert(table, null, values);

        //noinspection ConstantConditions
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

        final int uriType = matcher.match(uri);

        switch (uriType) {
            case CODE_BREWERY_TABLE:
                checkColumns(DBContract.Brewery.COLUMNS, projection);
                builder.setTables(BREWERY_TABLE);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "_id ASC";
                }
                break;
            case CODE_BREWERY_ROW:
                checkColumns(DBContract.Brewery.COLUMNS, projection);
                builder.setTables(BREWERY_TABLE);
                builder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case CODE_BEER_TABLE:
                checkColumns(DBContract.Beer.COLUMNS, projection);
                builder.setTables(BEER_TABLE);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "_id ASC";
                }
                break;
            case CODE_BEER_ROW:
                checkColumns(DBContract.Beer.COLUMNS, projection);
                builder.setTables(BEER_TABLE);
                builder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case CODE_STYLE_TABLE:
                checkColumns(DBContract.Style.COLUMNS, projection);
                builder.setTables(STYLE_TABLE);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "_id ASC";
                }
                break;
            case CODE_STYLE_ROW:
                checkColumns(DBContract.Style.COLUMNS, projection);
                builder.setTables(STYLE_TABLE);
                builder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            default:
                final String msg = "DBContentProvider.query(" + uri + "): No match (" + uriType + ")";
                throw new IllegalArgumentException(msg);
        }

        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        //noinspection ConstantConditions
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
            case CODE_BREWERY_TABLE:
                rowsUpdated = db.update(BREWERY_TABLE, values, selection, selectionArgs);
                break;
            case CODE_BREWERY_ROW:
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(BREWERY_TABLE, values, "_id=" + id, null);
                } else {
                    rowsUpdated = db.update(BREWERY_TABLE, values, "_id=" + id + " AND " + selection, null);
                }
                break;
            case CODE_BEER_TABLE:
                rowsUpdated = db.update(BEER_TABLE, values, selection, selectionArgs);
                break;
            case CODE_BEER_ROW:
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(BEER_TABLE, values, "_id=" + id, null);
                } else {
                    rowsUpdated = db.update(BEER_TABLE, values, "_id=" + id + " AND " + selection, null);
                }
                break;
            case CODE_STYLE_TABLE:
                rowsUpdated = db.update(STYLE_TABLE, values, selection, selectionArgs);
                break;
            case CODE_STYLE_ROW:
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(STYLE_TABLE, values, "_id=" + id, null);
                } else {
                    rowsUpdated = db.update(STYLE_TABLE, values, "_id=" + id + " AND " + selection, null);
                }
                break;
            default:
                final String msg = "DBContentProvider.update(" + uri + "): No match (" + uriType + ")";
                throw new IllegalArgumentException(msg);
        }

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(final String[] available, final String[] projection) {
        if (projection != null) {
            final HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            final HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                requestedColumns.removeAll(availableColumns);
                throw new IllegalArgumentException("Unknown columns in projection: " + requestedColumns);
            }
        }
    }

    public static String getAuthority() {
        return AUTHORITY;
    }

    public static String getBreweryTable() {
        return BREWERY_TABLE;
    }

    public static String getBeerTable() {
        return BEER_TABLE;
    }

    public static String getStyleTable() {
        return STYLE_TABLE;
    }
}