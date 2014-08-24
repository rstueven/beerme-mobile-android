package com.beerme.android_free.database;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.Spanned;

import com.beerme.android_free.utils.Utils;

public abstract class BeerNote implements Serializable {
	private static final long serialVersionUID = -4582989534126961588L;
	protected Source mSource;
	protected long id = -1;
	protected long beerid = -1;
	protected String pkg;
	protected String sampled;
	protected String place;
	protected float appscore;
	protected String appearance;
	protected float aroscore;
	protected String aroma;
	protected float mouscore;
	protected String mouthfeel;
	protected float ovrscore;
	protected String notes;

	public enum Source {
		BEERME, MY
	};

	public BeerNote(Context context, long noteId) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		if (noteId > 0) {
			this.id = noteId;

			load(context);
		}
	}

	public static BeerNote newInstance(Context context, long noteId,
			Source source) {
		switch (source) {
		case BEERME:
			return new BeerNote_BeerMe(context, noteId);
		case MY:
			return new BeerNote_My(context, noteId);
		default:
			return null;
		}
	}

	public abstract void load(Context context);

	public abstract void save(Context context);

	public abstract void delete(Context context);

	public Source getSource() {
		return this.mSource;
	}

	public long getId() {
		return this.id;
	}

	public long getBeerId() {
		return this.beerid;
	}

	public String getPkg() {
		return this.pkg;
	}

	public String getSampled() {
		String s = "";

		try {
			SimpleDateFormat rawFormat = new SimpleDateFormat("yyyy-MM-dd",
					Locale.getDefault());
			Date d = rawFormat.parse(this.sampled);
			DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
					Locale.getDefault());
			s = df.format(d);
		} catch (ParseException e) {
			s = "";
		}
		return s;
	}

	public String getPlace() {
		return this.place;
	}

	public float getAppscore() {
		return this.appscore;
	}

	public String getAppscoreFrac() {
		return Utils.toFrac(this.appscore);
	}

	public String getAppearance() {
		return this.appearance;
	}

	public float getAroscore() {
		return this.aroscore;
	}

	public String getAroscoreFrac() {
		return Utils.toFrac(this.aroscore);
	}

	public String getAroma() {
		return this.aroma;
	}

	public float getMouscore() {
		return this.mouscore;
	}

	public String getMouscoreFrac() {
		return Utils.toFrac(this.mouscore);
	}

	public String getMouthfeel() {
		return this.mouthfeel;
	}

	public float getOvrscore() {
		return this.ovrscore;
	}

	public String getOvrscoreFrac() {
		return Utils.toFrac(this.ovrscore);
	}

	public String getNotes() {
		return this.notes;
	}

	public float getScore() {
		return appscore + aroscore + mouscore + ovrscore;
	}

	public String getScoreFrac() {
		return Utils.toFrac(getScore());
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("_id: " + getId() + "\n");
		s.append("beerID: " + getBeerId() + "\n");
		s.append("pkg: " + getPkg() + "\n");
		s.append("sampled: " + getSampled() + "\n");
		s.append("place: " + getPlace() + "\n");
		s.append("appscore: " + getAppscore() + "\n");
		s.append("appearance: " + getAppearance() + "\n");
		s.append("aroscore: " + getAroscore() + "\n");
		s.append("aroma: " + getAroma() + "\n");
		s.append("mouscore: " + getMouscore() + "\n");
		s.append("mouthfeel: " + getMouthfeel() + "\n");
		s.append("ovrscore: " + getOvrscore() + "\n");
		s.append("notes: " + getNotes() + "\n");

		return s.toString();
	}

	public Spanned toHtml(final Context context) {
		StringBuilder s = new StringBuilder();
		String breweryName = null;
		String beerName = null;

		SQLiteDatabase db = DbOpenHelper.getInstance(context)
				.getReadableDatabase();
		String sql = "SELECT brewery.name, beer.name FROM brewery LEFT JOIN beer ON brewery._id=beer.breweryid WHERE beer._id=?";
		Cursor cursor = db.rawQuery(sql,
				new String[] { Long.toString(getBeerId()) });

		if (cursor.moveToFirst()) {
			breweryName = cursor.getString(0);
			beerName = cursor.getString(1);
		}

		s.append("<h1>" + beerName + "</h1>");
		s.append("<h2>" + breweryName + "</h2>");
		s.append("<div><b>Sampled</b> " + getSampled() + "</div>");
		s.append("<div><b>Place</b> " + getPlace() + "</div>");
		s.append("<div><b>Appearance</b> " + getAppscoreFrac() + "/3</div>");
		s.append("<div>" + getAppearance() + "</div>");
		s.append("<div><b>Aroma</b> " + getAroscoreFrac() + "/4</div>");
		s.append("<div>" + getAroma() + "</div>");
		s.append("<div><b>Mouthfeel</b> " + getMouscoreFrac() + "/10</div>");
		s.append("<div>" + getMouthfeel() + "</div>");
		s.append("<div><b>Overall</b> " + getOvrscoreFrac() + "/3</div>");
		s.append("<div>" + getNotes() + "</div>");
		s.append("<h3>Score " + getScoreFrac() + "/20</h3>");

		if (cursor != null) {
			cursor.close();
		}
		if (db != null) {
			db.close();
		}

		return Html.fromHtml(s.toString());
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setBeerId(long mBeerId) {
		this.beerid = mBeerId;
	}

	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	public void setSampled(String sampled) {
		// date is formatted according to the default Locale
		// We want to format it according to yyyy-MM-dd
		String dateString = sampled;

		try {
			DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
					Locale.getDefault());
			Date d = df.parse(dateString);
			SimpleDateFormat rawFormat = new SimpleDateFormat("yyyy-MM-dd",
					Locale.getDefault());
			dateString = rawFormat.format(d);
		} catch (ParseException e) {
			dateString = "";
		} finally {
			this.sampled = dateString;
		}
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public void setAppscore(float appscore) {
		this.appscore = appscore;
	}

	public void setAppearance(String appearance) {
		this.appearance = appearance;
	}

	public void setAroscore(float aroscore) {
		this.aroscore = aroscore;
	}

	public void setAroma(String aroma) {
		this.aroma = aroma;
	}

	public void setMouscore(float mouscore) {
		this.mouscore = mouscore;
	}

	public void setMouthfeel(String mouthfeel) {
		this.mouthfeel = mouthfeel;
	}

	public void setOvrscore(float ovrscore) {
		this.ovrscore = ovrscore;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}