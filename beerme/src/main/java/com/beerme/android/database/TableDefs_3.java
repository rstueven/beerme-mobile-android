package com.beerme.android.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.beerme.android.R;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.Utils;

public class TableDefs_3 extends TableDefs {
    public TableDefs_3() {
        super();
    }

    @Override
    protected void initTableDefs() {
        createStatements.put(TABLE_BREWERYNOTES, "CREATE TABLE IF NOT EXISTS "
                + TABLE_BREWERYNOTES + " (" + "id INTEGER PRIMARY KEY, "
                + "breweryid INTEGER NOT NULL, "
                + "date TEXT DEFAULT CURRENT_DATE NOT NULL, " + "rating REAL, "
                + "notes TEXT NOT NULL " + ")");
        createStatements.put(TABLE_BEERNOTES, "CREATE TABLE IF NOT EXISTS "
                + TABLE_BEERNOTES + " (" + "pagenumber INTEGER PRIMARY KEY, "
                + "beerid INTEGER NOT NULL, " + "package TEXT DEFAULT '', "
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
        // Upgrade to v3
        // add brewerynotes.id, beernotes.appscore,
        // beernotes.aroscore, beernotes.mouscore,
        // beernotes.ovrscore; delete beernotes.score
        Log.i(Utils.APPTAG, "Upgrading database from 2 to 3");
        long startTime = System.currentTimeMillis();
        db.beginTransaction();
        DbOpenHelper.setUpdating(true);
        try {
            String[] columns = getColumns(db, TABLE_BREWERYNOTES);
            db.execSQL("DROP TABLE IF EXISTS 'temp'");
            db.execSQL("ALTER TABLE " + TABLE_BREWERYNOTES + " RENAME TO 'temp'");
            db.execSQL(createStatements.get(TABLE_BREWERYNOTES));
            String cols = Utils.stringify(columns, ",");
            db.execSQL(String.format("INSERT INTO %s (%s) SELECT %s from temp", TABLE_BREWERYNOTES, cols, cols));

            db.execSQL("DROP TABLE IF EXISTS 'temp'");
            db.execSQL("ALTER TABLE " + TABLE_BEERNOTES + " RENAME TO 'temp'");
            db.execSQL(createStatements.get(TABLE_BEERNOTES));

            Cursor c = null;

            c = db.rawQuery("SELECT pagenumber, beerid, package, score, sampled, place, appearance, aroma, mouthfeel, notes, breweryid FROM 'temp'", null);

            if (c != null) {
                String insertSql = "INSERT INTO "
                        + TABLE_BEERNOTES
                        + "(pagenumber, beerid, package, sampled, place, appscore, appearance, aroscore, aroma, mouscore, mouthfeel, ovrscore, notes, breweryid) "
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                SQLiteStatement insertStatement = db.compileStatement(insertSql);

                while (c.moveToNext()) {
                    int pagenumber = c.getInt(0);
                    long beerid = c.getLong(1);
                    String pkg = c.getString(2);
                    double score = c.getDouble(3);
                    String sampled = c.getString(4);
                    String place = c.getString(5);
                    String appearance = c.getString(6);
                    String aroma = c.getString(7);
                    String mouthfeel = c.getString(8);
                    String notes = c.getString(9);
                    long breweryid = c.getLong(10);

                    double k = score / 20;
                    double appScore = Math.round((3 * k) * 2) / 2;
                    double aroScore = Math.round((4 * k) * 2) / 2;
                    double mouScore = Math.round((10 * k) * 2) / 2;
                    double ovrScore = score - (appScore + aroScore + mouScore);

                    insertStatement.bindLong(1, pagenumber);
                    insertStatement.bindLong(2, beerid);
                    insertStatement.bindString(3, pkg);
                    insertStatement.bindString(4, sampled);
                    insertStatement.bindString(5, place);
                    insertStatement.bindDouble(6, appScore);
                    insertStatement.bindString(7, appearance);
                    insertStatement.bindDouble(8, aroScore);
                    insertStatement.bindString(9, aroma);
                    insertStatement.bindDouble(10, mouScore);
                    insertStatement.bindString(11, mouthfeel);
                    insertStatement.bindDouble(12, ovrScore);
                    insertStatement.bindString(13, notes);
                    insertStatement.bindLong(14, breweryid);

                    insertStatement.executeInsert();
                }
            }

            c.close();
            db.execSQL("DROP TABLE 'temp'");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            ErrLog.log(mContext, "onUpgrade(case 2)", e, R.string.Database_problem);
        } finally {
            db.endTransaction();
            DbOpenHelper.setUpdating(false);
            Log.i(Utils.APPTAG, "Elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }
}