package com.beerme.android.search;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.beerme.android.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceList extends ArrayList<Row> {
	private static final long serialVersionUID = -1685894698135672290L;
	private List<Address> mAddressList;

	public PlaceList(Context context, String queryString) {

		Geocoder geocoder = new Geocoder(context);
		try {
			mAddressList = geocoder.getFromLocationName(queryString, 5);
			for (Address addr : mAddressList) {
				this.add(new Row(0, addr.getAddressLine(0), addr
						.getAddressLine(1), 0, String.format(Locale.getDefault(),
						"%f,%f", addr.getLatitude(), addr.getLongitude())));
			}
		} catch (IOException e) {
			Log.w(Utils.APPTAG, "PlaceList.geocoder.getFromLocationName("
					+ queryString + ", 5): " + e.getLocalizedMessage());
		}
	}

	public List<Address> getList() {
		return mAddressList;
	}
}