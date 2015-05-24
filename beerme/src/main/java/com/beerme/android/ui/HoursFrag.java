package com.beerme.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.database.Brewery;
import com.beerme.android.utils.Utils;

public class HoursFrag extends Fragment {
	private static final String TAG_ID = "id";
	private Brewery mBrewery = null;

	// LOW: AND0067: RFE: Parse the "hours" text and display "open" or "closed"

	public static HoursFrag getInstance(long breweryId) {
		HoursFrag frag = new HoursFrag();

		Bundle args = new Bundle();
		args.putLong(TAG_ID, breweryId);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setRetainInstance(true);

		Bundle args = getArguments();
		if (args != null) {
			mBrewery = new Brewery(activity, args.getLong(TAG_ID));
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hours_frag, container, false);

		TextView phoneView = (TextView) view.findViewById(R.id.phone_view);
		Utils.setTextOrGone(phoneView, mBrewery.getPhone());

		TextView hoursView = (TextView) view.findViewById(R.id.hours_view);
		String hours = mBrewery.getHours().replaceAll("([,\\.] )|\\.$", "<br/>");

		Utils.setTextOrGone(hoursView, hours);

		TableRow bar = (TableRow) (view.findViewById(R.id.hours_bar));
		bar.setVisibility(mBrewery.hasBar() ? View.VISIBLE : View.GONE);

		TableRow beergarden = (TableRow) (view
				.findViewById(R.id.hours_beergarden));
		beergarden.setVisibility(mBrewery.hasBeergarden() ? View.VISIBLE
				: View.GONE);

		TableRow food = (TableRow) (view.findViewById(R.id.hours_food));
		food.setVisibility(mBrewery.hasFood() ? View.VISIBLE : View.GONE);

		TableRow giftshop = (TableRow) (view.findViewById(R.id.hours_giftshop));
		giftshop.setVisibility(mBrewery.hasGiftshop() ? View.VISIBLE
				: View.GONE);

		TableRow hotel = (TableRow) (view.findViewById(R.id.hours_hotel));
		hotel.setVisibility(mBrewery.hasHotel() ? View.VISIBLE : View.GONE);

		TableRow internet = (TableRow) (view.findViewById(R.id.hours_internet));
		internet.setVisibility(mBrewery.hasInternet() ? View.VISIBLE
				: View.GONE);

		TableRow retail = (TableRow) (view.findViewById(R.id.hours_retail));
		retail.setVisibility(mBrewery.hasRetail() ? View.VISIBLE : View.GONE);

		TableRow tours = (TableRow) (view.findViewById(R.id.hours_tours));
		tours.setVisibility(mBrewery.hasTours() ? View.VISIBLE : View.GONE);

		TextView urlView = (TextView) view.findViewById(R.id.url_view);
		Utils.setTextOrGone(urlView, mBrewery.getWeb().replaceAll("/$", ""));

		return view;
	}
}