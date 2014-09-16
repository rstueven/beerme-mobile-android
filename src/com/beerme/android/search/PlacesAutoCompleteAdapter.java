package com.beerme.android.search;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements
		Filterable {
	private Context mContext;
	private List<Address> resultList;

	// https://developers.google.com/places/training/autocomplete-android
	public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return resultList.size();
	}

	@Override
	public String getItem(int index) {
		Address addr = resultList.get(index);
		StringBuffer buf = new StringBuffer(addr.getAddressLine(0));
		int n = addr.getMaxAddressLineIndex();
		if (n >= 1) {
			for (int i = 1; i <= n; i++) {
				buf.append(", " + addr.getAddressLine(i));
			}
		}
		return buf.toString();
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();
				if (constraint != null) {
					// Retrieve the autocomplete results.
					resultList = new PlaceList(mContext, constraint.toString())
							.getList();

					// Assign the data to the FilterResults
					filterResults.values = resultList;
					filterResults.count = resultList.size();
				}
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		};
		return filter;
	}
}