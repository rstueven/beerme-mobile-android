package com.beerme.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.beerme.android.utils.Utils;

public class TableDefs_2 extends TableDefs {
	public TableDefs_2() {
		super();
	}

	@Override
	protected void initTableDefs() {
		createStatements.put(TABLE_BREWERYNOTES, "CREATE TABLE IF NOT EXISTS "
				+ TABLE_BREWERYNOTES + " (" + "breweryid INTEGER NOT NULL, "
				+ "date TEXT DEFAULT CURRENT_DATE NOT NULL, " + "rating REAL, "
				+ "notes TEXT NOT NULL, " + "PRIMARY KEY (breweryid, date)"
				+ ")");
		createStatements.put(TABLE_BEERNOTES, "CREATE TABLE IF NOT EXISTS "
				+ TABLE_BEERNOTES + " (" + "pagenumber INTEGER PRIMARY KEY, "
				+ "beerid INTEGER NOT NULL," + " package TEXT, "
				+ "score REAL, " + "sampled TEXT DEFAULT CURRENT_DATE, "
				+ "place TEXT, " + "appearance TEXT, " + "aroma TEXT, "
				+ "mouthfeel TEXT, " + "notes TEXT, "
				+ "breweryid INTEGER NOT NULL" + ")");
	}

	@Override
	protected void upgrade(SQLiteDatabase db) {
		// Upgrade to v2 — LOSS OF DATA
		// Add beernotes.breweryid
		// Ideally, this would query beerme.com for breweryids
		// to match the beerids
		Log.i(Utils.APPTAG, "Upgrading database from 1 to 2");
		long startTime = System.currentTimeMillis();
		db.beginTransaction();
		DbOpenHelper.setUpdating(mContext, true);
		try {
			db.execSQL("DROP TABLE " + TABLE_BEERNOTES);
			db.execSQL(createStatements.get(TABLE_BEERNOTES));
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			DbOpenHelper.setUpdating(mContext, false);
			Log.i(Utils.APPTAG, "Elapsed: "
					+ (System.currentTimeMillis() - startTime) + " ms");
		}
	}
}