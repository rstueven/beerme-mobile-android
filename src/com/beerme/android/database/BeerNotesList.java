package com.beerme.android.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.Utils;
import com.beerme.android.R;

public class BeerNotesList extends ArrayList<BeerNote> {
	private static final long serialVersionUID = -7427457104756970882L;
	private long mBeerId;
	private BeerNote.Source mSource;
	private static final String TABLE = "beernotes";
	private static String[] mColumns = { "_id" };
	private static final String notesListUrl = Utils.BEERME_URL
			+ "beerNotesList.php";

	public BeerNotesList() {
	}

	public BeerNotesList(final Context context, long beerId,
			BeerNote.Source source) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		if (beerId <= 0) {
			throw new IllegalArgumentException("Invalid beerId(" + beerId + ")");
		}

		this.mBeerId = beerId;
		this.mSource = source;

		switch (mSource) {
		case BEERME:
			final BeerNotesList list = this;
			Thread beerme_beernotes_thread = new Thread(new Runnable() {

				@Override
				public void run() {
					BufferedReader in = null;
					try {
						URL url = new URL(notesListUrl + "?i=" + mBeerId);
						in = new BufferedReader(new InputStreamReader(
								url.openStream()));
						String idListString = in.readLine();

						if (idListString != null) {
							String[] idList = idListString.split("\\|");
							int n = idList.length;
							long id = -1;

							for (int i = 0; i < n; i++) {
								try {
									id = Long.parseLong(idList[i]);
									list.add(BeerNote.newInstance(context, id,
											mSource));
								} catch (NumberFormatException e) {
									Log.e(Utils.APPTAG, "BeerNotesList("
											+ mBeerId + ", " + mSource.name()
											+ "): " + e.getLocalizedMessage());
								}
							}
						}
					} catch (MalformedURLException e) {
						Log.e(Utils.APPTAG, "notesListUrl:" + notesListUrl
								+ ":" + e.getLocalizedMessage());
					} catch (IOException e) {
						Log.e(Utils.APPTAG, "notesListUrl:" + notesListUrl
								+ ":" + e.getLocalizedMessage());
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
								Log.w(Utils.APPTAG, e.getLocalizedMessage());
							}
						}
					}
				}
			});

			beerme_beernotes_thread.start();
			try {
				beerme_beernotes_thread.join();
			} catch (InterruptedException e) {
				Log.w(Utils.APPTAG, e.getLocalizedMessage());
			}
			break;
		case MY:
			SQLiteDatabase db = null;
			Cursor cursor = null;

			try {
				db = DbOpenHelper.getInstance(context).getReadableDatabase();

				cursor = db.query(TABLE, mColumns, "beerId=" + mBeerId, null,
						null, null, "_id");

				int id = -1;

				while (cursor.moveToNext()) {
					id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
					this.add(BeerNote.newInstance(context, id, mSource));
				}
			} catch (SQLiteException e) {
				ErrLog.log(context,
						"BeerNotesList(" + mBeerId + ", " + mSource.name()
								+ ")", e, R.string.Database_is_busy);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				if (db != null) {
					db.close();
				}
			}
			break;
		default:
			break;
		}
	}

	public float getAvgRating() {
		float total = 0;
		int n = 0;

		for (BeerNote note : this) {
			total += note.getScore();
			++n;
		}

		return (n > 0) ? (total / n) : -1;
	}
}