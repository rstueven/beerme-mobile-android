package com.beerme.android_free.search;

public class Row {
	public long id;
	public String col1;
	public String col2;
	public long dataId;
	public String location;

	protected Row(long id, String col1, String col2, long dataId,
			String location) {
		this.id = id;
		this.col1 = col1;
		this.col2 = col2;
		this.dataId = dataId;
		this.location = location;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("id:" + id + "\n");
		buf.append("col1:" + col1 + "\n");
		buf.append("col2:" + col2 + "\n");
		buf.append("dataId:" + dataId + "\n");
		buf.append("location:" + location + "\n");

		return buf.toString();
	}
}