package com.beerme.android.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rstueven on 7/4/17.
 * Contract for DBContentProvider.
 */

public final class DBContract {
    public static final String AUTHORITY = "com.beerme.android.provider";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_BREWERY = "brewery";
    public static final String PATH_BEER = "beer";
    public static final String PATH_STYLE = "style";

    public static final class Brewery implements BaseColumns {
        public static final String TABLE_NAME = "brewery";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_HOURS = "hours";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_WEB = "web";
        public static final String COLUMN_SERVICES = "services";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_UPDATED = "updated";

        public static final String[] COLUMNS = {
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_ADDRESS,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_STATUS,
                COLUMN_HOURS,
                COLUMN_PHONE,
                COLUMN_WEB,
                COLUMN_SERVICES,
                COLUMN_IMAGE,
                COLUMN_UPDATED
        };

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_BREWERY).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_BREWERY;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_BREWERY;

        public static Uri buildBreweryUri(final long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class Beer implements BaseColumns {
        public static final String TABLE_NAME = "beer";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_BREWERYID = "breweryid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_STYLE = "style";
        public static final String COLUMN_ABV = "abv";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_UPDATED = "updated";
        public static final String COLUMN_BEERMERATING = "beermerating";

        public static final String[] COLUMNS = {
                COLUMN_ID,
                COLUMN_BREWERYID,
                COLUMN_NAME,
                COLUMN_STYLE,
                COLUMN_ABV,
                COLUMN_IMAGE,
                COLUMN_UPDATED,
                COLUMN_BEERMERATING
        };

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_BEER).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_BEER;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_BEER;

        public static Uri buildBeerUri(final long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class Style implements BaseColumns {
        public static final String TABLE_NAME = "style";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_UPDATED = "updated";

        public static final String[] COLUMNS = {
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_UPDATED
        };

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_STYLE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_STYLE;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_STYLE;

        public static Uri buildStyleUri(final long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}