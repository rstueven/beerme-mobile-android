package com.beerme.android.database;

public class TableDefs_1 extends TableDefs {
	public TableDefs_1() {
		super();
	}

	@Override
	protected void initTableDefs() {
		createStatements.put(TABLE_BREWERYNOTES, "CREATE TABLE IF NOT EXISTS "
				+ TABLE_BREWERYNOTES + " (" + "breweryid INTEGER NOT NULL,"
				+ "date TEXT NOT NULL," + "rating INTEGER,"
				+ "notes TEXT NOT NULL," + "PRIMARY KEY (breweryid, date)"
				+ ")");
		createStatements.put(TABLE_BEERNOTES, "CREATE TABLE IF NOT EXISTS "
				+ TABLE_BEERNOTES + " (" + "beerid INTEGER NOT NULL,"
				+ "date TEXT NOT NULL," + "rating INTEGER,"
				+ "notes TEXT NOT NULL," + "PRIMARY KEY (beerid, date)" + ");");
	}
}