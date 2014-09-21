package com.beerme.android_free.ui;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.beerme.android_free.R;
import com.beerme.android_free.database.Beer;
import com.beerme.android_free.database.BeerList;
import com.beerme.android_free.utils.Utils;

public class BeerListFrag extends Fragment {
	private static final String TAG_ID = "id";
	private final static int LOAD_START = 1;
	private final static int LOAD_END = 2;
	private long mId = -1;
	private BeerList mList = null;
	private ListView mListView = null;
	private ProgressBar mProgress = null;
	private static ListHandler mHandler = null;

	// LOW: AND0036: RFE: Beer list sort options

	public static BeerListFrag getInstance(long id) {
		BeerListFrag frag = new BeerListFrag();

		Bundle args = new Bundle();
		args.putLong(TAG_ID, id);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setRetainInstance(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new ListHandler(this);

		Bundle args = getArguments();
		if (args != null) {
			mId = args.getLong(TAG_ID, -1);
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
		View view = inflater.inflate(R.layout.beerlist_frag, container, false);

		if (mId > 0) {
			mListView = (ListView) view.findViewById(R.id.beerlist);
			mListView.setOnItemClickListener(itemClickListener);
			mProgress = (ProgressBar) view.findViewById(R.id.beerlist_progress);

			if (mList == null) {
				mHandler = new ListHandler(this);
				mHandler.sendEmptyMessage(LOAD_START);
			} else {
				mHandler.sendEmptyMessage(LOAD_END);
			}
		}

		return view;
	}

	private class LoadBeerList implements Runnable {
		@Override
		public void run() {
			mList = new BeerList(getActivity(), mId);
			mHandler.sendEmptyMessage(LOAD_END);
		}
	}

	private final static class ListHandler extends Handler {
		private WeakReference<BeerListFrag> mFrag;

		public ListHandler(BeerListFrag aFrag) {
			mFrag = new WeakReference<BeerListFrag>(aFrag);
		}

		@Override
		public void handleMessage(Message msg) {
			BeerListFrag theFrag = mFrag.get();

			switch (msg.what) {
			case LOAD_START:
				theFrag.mProgress.setVisibility(View.VISIBLE);
				new Thread(theFrag.new LoadBeerList(), "LoadBeerList").start();
				break;
			case LOAD_END:
				FragmentActivity activity = theFrag.getActivity();
				if (activity != null) {
					theFrag.mListView.setAdapter(theFrag.new ListAdapter(
							activity, R.id.beerlist, theFrag.mList));
				}
				theFrag.mProgress.setVisibility(View.GONE);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	public class ListAdapter extends ArrayAdapter<Beer> {
		private Context mContext;

		public ListAdapter(Context context, int resource, List<Beer> objects) {
			super(context, resource, objects);
			this.mContext = context;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.beerlist_row, null);
			}

			Beer beer = getItem(position);

			if (beer != null) {
				TextView nameView = (TextView) view
						.findViewById(R.id.beerlist_name);
				nameView.setText(beer.getName());

				RatingBar ratingBar = (RatingBar) view
						.findViewById(R.id.beerlist_rating);
				float myRating = beer.getMyRating(getActivity());
				float beermeRating = beer.getBeerMeRating();

				if (myRating > 0) {
					ratingBar.setRating(Utils.stars(myRating));
					ratingBar.setVisibility(View.VISIBLE);
					ratingBar.setBackgroundColor(getResources().getColor(
							android.R.color.background_light));
				} else if (beermeRating > 0) {
					ratingBar.setRating(Utils.stars(beermeRating));
					ratingBar.setVisibility(View.VISIBLE);
					ratingBar.setBackgroundColor(Color.TRANSPARENT);
				} else {
					ratingBar.setVisibility(View.INVISIBLE);
				}

				float abv = beer.getAbv();

				if (abv > 0) {
					TextView abvView = (TextView) view
							.findViewById(R.id.beerlist_abv);
					abvView.setText(beer.getAbv() + getString(R.string.pct_abv));
				}

				TextView styleView = (TextView) view
						.findViewById(R.id.beerlist_style);
				styleView.setText(beer.getStyle());
			}

			return view;
		}
	}

	public OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Beer beer = mList.get(position);

			if (beer != null) {
				Intent intent = new Intent(getActivity(), BeerActivity.class);
				intent.putExtra("id", beer.getId());
				startActivity(intent);
			}
		}
	};
}