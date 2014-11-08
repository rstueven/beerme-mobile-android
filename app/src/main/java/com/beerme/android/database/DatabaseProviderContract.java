package com.beerme.android.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rstueven on 7/6/14.
 */
public final class DatabaseProviderContract {
	public static final Uri CONTENT_URI = Uri.parse("content://com.beerme.android.provider");

	private DatabaseProviderContract() {
	}

	public static abstract class Brewery implements BaseColumns {
		public static final String TABLE_NAME = "brewery";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_ADDRESS = "address";
		public static final String COLUMN_NAME_LATITUDE = "latitude";
		public static final String COLUMN_NAME_LONGITUDE = "longitude";
		public static final String COLUMN_NAME_STATUS = "status";
		public static final String COLUMN_NAME_HOURS = "hours";
		public static final String COLUMN_NAME_PHONE = "phone";
		public static final String COLUMN_NAME_WEB = "web";
		public static final String COLUMN_NAME_SERVICES = "services";
		public static final String COLUMN_NAME_IMAGE = "image";
		public static final String COLUMN_NAME_UPDATED = "updated";
		public static final String[] ALL_COLUMNS = new String[]{COLUMN_NAME_ID, COLUMN_NAME_NAME,
				COLUMN_NAME_ADDRESS, COLUMN_NAME_LATITUDE, COLUMN_NAME_LONGITUDE, COLUMN_NAME_STATUS,
				COLUMN_NAME_HOURS, COLUMN_NAME_PHONE, COLUMN_NAME_WEB, COLUMN_NAME_SERVICES,
				COLUMN_NAME_IMAGE, COLUMN_NAME_UPDATED
		};
	}

	public static abstract class BreweryNotes implements BaseColumns {
		public static final String TABLE_NAME = "brewerynotes";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_BREWERYID = "breweryid";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_RATING = "rating";
		public static final String COLUMN_NAME_NOTES = "notes";
		public static final String[] ALL_COLUMNS = new String[]{COLUMN_NAME_ID, COLUMN_NAME_BREWERYID,
				COLUMN_NAME_DATE, COLUMN_NAME_RATING, COLUMN_NAME_NOTES
		};
	}

	public static abstract class Beer implements BaseColumns {
		public static final String TABLE_NAME = "beer";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_BREWERYID = "breweryid";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_STYLE = "style";
		public static final String COLUMN_NAME_ABV = "abv";
		public static final String COLUMN_NAME_IMAGE = "image";
		public static final String COLUMN_NAME_UPDATED = "updated";
		public static final String COLUMN_NAME_BEERMERATING = "beermerating";
		public static final String[] ALL_COLUMNS = new String[]{COLUMN_NAME_ID,
				COLUMN_NAME_BREWERYID, COLUMN_NAME_NAME, COLUMN_NAME_STYLE, COLUMN_NAME_ABV,
				COLUMN_NAME_IMAGE, COLUMN_NAME_UPDATED, COLUMN_NAME_BEERMERATING
		};
	}

	public static abstract class BeerNotes implements BaseColumns {
		public static final String TABLE_NAME = "beernotes";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_BEERID = "beerid";
		public static final String COLUMN_NAME_PACKAGE = "package";
		public static final String COLUMN_NAME_SAMPLED = "sampled";
		public static final String COLUMN_NAME_PLACE = "place";
		public static final String COLUMN_NAME_APPSCORE = "appscore";
		public static final String COLUMN_NAME_APPEARANCE = "appearance";
		public static final String COLUMN_NAME_AROSCORE = "aroscore";
		public static final String COLUMN_NAME_AROMA = "aroma";
		public static final String COLUMN_NAME_MOUSCORE = "mouscore";
		public static final String COLUMN_NAME_MOUTHFEEL = "mouthfeel";
		public static final String COLUMN_NAME_OVRSCORE = "ovrscore";
		public static final String COLUMN_NAME_NOTES = "notes";
		public static final String[] ALL_COLUMNS = new String[]{COLUMN_NAME_ID, COLUMN_NAME_BEERID,
				COLUMN_NAME_PACKAGE, COLUMN_NAME_SAMPLED, COLUMN_NAME_PLACE, COLUMN_NAME_APPSCORE,
				COLUMN_NAME_APPEARANCE, COLUMN_NAME_AROSCORE, COLUMN_NAME_AROMA,
				COLUMN_NAME_MOUSCORE, COLUMN_NAME_MOUTHFEEL, COLUMN_NAME_OVRSCORE,
				COLUMN_NAME_NOTES
		};
	}

	public static abstract class Style implements BaseColumns {
		public static final String TABLE_NAME = "style";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_UPDATED = "updated";
		public static final String[] ALL_COLUMNS = new String[]{COLUMN_NAME_ID, COLUMN_NAME_NAME,
				COLUMN_NAME_UPDATED};
	}
}