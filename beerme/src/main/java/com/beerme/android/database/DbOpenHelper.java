package com.beerme.android.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.beerme.android.R;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.prefs.SharedPreferenceSaver;
import com.beerme.android.utils.ErrLog;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public class DbOpenHelper extends SQLiteOpenHelper implements
		TableDefs.UpdateListener {
	public static final int DB_VERSION = 6;
	public static final String DB_NAME = "beerme";
	public static String DB_FILEPATH = null;
	@SuppressLint("ConstantLocale")
	public static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private static Context mContext;
	private static OnDbOpenListener mListener;
	private static DbOpenHelper mInstance = null;

	public interface OnDbOpenListener {
		void onDbOpen();
	}

	private DbOpenHelper(final Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		mContext = context;

		try {
			mListener = (OnDbOpenListener) mContext;
		} catch (ClassCastException e) {
			mListener = null;
		}

		DB_FILEPATH = mContext.getDatabasePath(DB_NAME).getAbsolutePath();
	}

	public static DbOpenHelper getInstance(Context context) {
		return (mInstance == null) ? new DbOpenHelper(context) : mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		TableDefs tableDefs;
		tableDefs = TableDefs.newInstance(DB_VERSION);

		if (!isUpdating(mContext)) {
			try {
				setUpdating(mContext, true);
				HashMap<String, String> createStatements = tableDefs
						.getCreateStatements();

				for (String key : createStatements.keySet()) {
					db.execSQL(createStatements.get(key));
				}

				HashMap<String, String[]> indexStatements = tableDefs
						.getIndexStatements();

				for (String key : indexStatements.keySet()) {
					String[] idxStmts = indexStatements.get(key);
					for (String idxStmt : idxStmts) {
						db.execSQL(idxStmt);
					}
				}

			} catch (SQLException e) {
				ErrLog.log(mContext, "DbOpenHelper.onCreate()", e,
						R.string.Database_problem);
			} finally {
				setUpdating(mContext, false);
			}
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		if (!isUpdating(mContext)) {
			TableDefs.newInstance(DB_VERSION).updateData(db, this);
		} else {
			if (mListener != null) {
				mListener.onDbOpen();
			}
		}
	}

	@Override
	public void onDataUpdated() {
		if (mListener != null) {
			mListener.onDbOpen();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (int i = ++oldVersion; i <= newVersion; i++) {
			TableDefs.newInstance(i).upgrade(db);
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String msg = String.format(mContext.getString(R.string.Database_downgrade_unsupported),
				oldVersion, newVersion);
		ErrLog.log(mContext, "DbOpenHelper.onDowngrade(" + oldVersion + ", " + newVersion + ")", null, msg);
	}

	public static void setUpdating(Context context, boolean updating) {
		SharedPreferences.Editor editor = Prefs.getSettingsEditor(context);
		SharedPreferenceSaver saver = Prefs.getSettingsSaver(context);
		editor.putBoolean(Prefs.KEY_DB_UPDATING, updating);
		saver.savePreferences(editor, false);
	}

	public static boolean isUpdating(Context context) {
		return Prefs.getSettings(context).getBoolean(Prefs.KEY_DB_UPDATING,
				false);
	}

	protected static Context getContext() {
		return mContext;
	}

	public void forceUpdate(Context context, SQLiteDatabase db) {
		TableDefs.resetLastUpdate(context);
		TableDefs.newInstance(DB_VERSION).updateData(db, this);
	}

	public void forceReload(Context context, SQLiteDatabase db) {
		TableDefs.resetLastUpdate(context);
		TableDefs tableDefs = TableDefs.newInstance(DB_VERSION);
		tableDefs.installNewTables(db);
		tableDefs.updateData(db, this);
	}
}