package com.beerme.android.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsItem {
	private String url = "";
	private String title = "";
	private String source = "";
	private Date date = new Date();
	@SuppressLint("ConstantLocale")
	private final static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	public NewsItem(String[] fields) throws ParseException {
		if (fields != null) {
			int nFields = fields.length;
			switch (nFields) {
			case 4:
				this.date = df.parse(fields[3]);
			case 3:
				this.source = fields[2].replace("\\", "");
			case 2:
				this.title = fields[1].replace("\\", "");
			case 1:
				this.url = fields[0];
			}
		}
	}

	public Date getDate() {
		return date;
	}

	public String getSource() {
		return source;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setDate(String date) throws ParseException {
		this.date = df.parse(date);
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return this.getUrl() + ":" + this.getTitle() + ":" + this.getSource()
				+ ":" + this.getDate();
	}
}