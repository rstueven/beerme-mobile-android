package com.beerme.android_free.database;

import android.database.sqlite.SQLiteDatabase;

public class TableDefs_4 extends TableDefs {
	public TableDefs_4() {
		super();
	}

	@Override
	protected void initTableDefs() {
		createStatements.put(TABLE_BREWERYNOTES, "CREATE TABLE IF NOT EXISTS "
				+ TABLE_BREWERYNOTES + " (" + "id INTEGER PRIMARY KEY, "
				+ "breweryid INTEGER NOT NULL, " + "name TEXT NOT NULL, "
				+ "date TEXT DEFAULT CURRENT_DATE NOT NULL, " + "rating REAL, "
				+ "notes TEXT NOT NULL " + ")");
		createStatements.put(TABLE_BEERNOTES, "CREATE TABLE IF NOT EXISTS "
				+ TABLE_BEERNOTES + " (" + "pagenumber INTEGER PRIMARY KEY, "
				+ "beerid INTEGER NOT NULL, " + "name TEXT NOT NULL, "
				+ "package TEXT DEFAULT '', "
				+ "sampled TEXT DEFAULT CURRENT_DATE, "
				+ "place TEXT DEFAULT '', " + "appscore REAL DEFAULT 0, "
				+ "appearance TEXT DEFAULT '', " + "aroscore REAL DEFAULT 0, "
				+ "aroma TEXT DEFAULT '', " + "mouscore REAL DEFAULT 0, "
				+ "mouthfeel TEXT DEFAULT '', " + "ovrscore REAL DEFAULT 0, "
				+ "notes TEXT DEFAULT '', " + "breweryid INTEGER NOT NULL"
				+ ")");
	}

	@Override
	protected void upgrade(SQLiteDatabase db) {
		// Upgrade to v4
		// add brewerynotes.name, beernotes.name
		// Unnecessary now, because v5 removes these same columns
	}
}