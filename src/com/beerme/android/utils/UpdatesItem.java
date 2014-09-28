package com.beerme.android.utils;

import java.text.ParseException;

public class UpdatesItem {
	private long breweryid;
	private String name;
	private String addr;

	public UpdatesItem(String[] fields) throws ParseException {
		this.breweryid = Long.parseLong(fields[0]);
		this.name = fields[1].replace("\\", "");
		this.addr = fields[2].replace("\\", "");
	}

	public UpdatesItem(long breweryid, String name, String addr) {
		this.breweryid = breweryid;
		this.name = name.replace("\\", "");
		this.addr = addr.replace("\\", "");
	}

	public long getBreweryid() {
		return breweryid;
	}

	public void setBreweryid(long breweryid) {
		this.breweryid = breweryid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	@Override
	public String toString() {
		return this.getBreweryid() + ":" + this.getName() + ":"
				+ this.getAddr();
	}
}