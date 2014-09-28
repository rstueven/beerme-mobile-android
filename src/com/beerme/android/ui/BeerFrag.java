package com.beerme.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.database.Beer;
import com.beerme.android.utils.Utils;

public class BeerFrag extends Fragment {
	private static final String TAG_ID = "id";
	private FragmentActivity mActivity = null;
	private long mBeerId = -1;
	private Beer mBeer = null;

	public BeerFrag() {
	}

	public static BeerFrag getInstance(long id) {
		BeerFrag frag = new BeerFrag();

		Bundle args = new Bundle();
		args.putLong(TAG_ID, id);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setRetainInstance(true);

		mActivity = (FragmentActivity) activity;

		Bundle args = getArguments();
		if (args != null) {
			mBeerId = args.getLong(TAG_ID, -1);
		}

		if (mBeerId > 0) {
			mBeer = new Beer(mActivity, mBeerId);
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
		View view = inflater.inflate(R.layout.beer_frag, container, false);

		if (mBeer != null) {
			TextView nameView = (TextView) view
					.findViewById(R.id.beerdetail_name);
			nameView.setText(mBeer.getName());

			TextView breweryView = (TextView) view
					.findViewById(R.id.beerdetail_brewery);
			breweryView.setText(mBeer.getBrewery());
			breweryView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(getActivity(),
							BreweryActivity.class);
					intent.putExtra("id", mBeer.getBreweryId());
					startActivity(intent);
				}
			});

			TextView styleView = (TextView) view
					.findViewById(R.id.beerdetail_style);
			Utils.setTextOrGone(styleView, mBeer.getStyle());

			TextView abvView = (TextView) view
					.findViewById(R.id.beerdetail_abv);
			if (mBeer.getAbv() > 0) {
				abvView.setText(mBeer.getAbv() + getString(R.string.pct_abv));
			} else {
				abvView.setVisibility(View.GONE);
			}
		}

		return view;
	}
}