package com.beerme.android.location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.beerme.android.utils.Utils;

public class LocationList {
	private Context mContext;
	private String mTarget = null;
	private List<Address> mList = null;
	private String[] mAddresses = null;

	public LocationList(Context context, String target) throws IOException {
		this.mContext = context;
		this.mTarget = target;

		if (!"".equals(target)) {
			Geocoder gc = new Geocoder(mContext, Locale.getDefault());
			mList = gc.getFromLocationName(target, 10);
			if (mList != null) {
				int listSize = mList.size();
				if (listSize > 0) {
					mAddresses = new String[listSize];
					for (int i = 0; i < listSize; i++) {
						mAddresses[i] = Utils.formatAddress(mList.get(i));
					}
				}
			}
		}
	}

	public String getTarget() {
		return this.mTarget;
	}

	public List<Address> getList() {
		return this.mList;
	}

	public String[] getAddresses() {
		return this.mAddresses;
	}
}