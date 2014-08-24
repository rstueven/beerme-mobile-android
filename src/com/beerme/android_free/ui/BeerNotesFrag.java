package com.beerme.android_free.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import com.beerme.android_free.R;
import com.beerme.android_free.database.BeerNotesList;
import com.beerme.android_free.database.BeerNote.Source;
import com.beerme.android_free.ui.BeerNoteFrag.BeerNoteCallbacks;
import com.beerme.android_free.utils.Utils;

public class BeerNotesFrag extends Fragment implements BeerNoteCallbacks {
	private static final String TAG_ID = "id";
	protected final static String TAG_BEERNOTE_FRAG = "beernoteFrag";
	private long mBeerId = -1;
	private BeerNotesList mBeermeList = null;
	private BeerNotesList mMyList = null;
	private ViewPager mViewPager = null;
	private BeerNotePagerAdapter mAdapter = null;
	private ProgressBar mProgress = null;
	private RadioButton mMyNotesButton = null;
	private RadioButton mBeermeNotesButton = null;
	private static BeerNotesHandler mHandler = null;
	private Source mSource;

	public static BeerNotesFrag getInstance(long beerId) {
		BeerNotesFrag frag = new BeerNotesFrag();

		Bundle args = new Bundle();
		args.putLong(TAG_ID, beerId);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setRetainInstance(true);

		Bundle args = getArguments();
		if (args != null) {
			mBeerId = args.getLong(TAG_ID, -1);
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
		View view = inflater.inflate(R.layout.beernotes_frag, container, false);
		setHasOptionsMenu(true);

		if (mBeerId > 0) {
			mProgress = (ProgressBar) view
					.findViewById(R.id.beernotes_progress);
			mBeermeNotesButton = (RadioButton) view
					.findViewById(R.id.beerme_notes_button);
			mBeermeNotesButton.setOnCheckedChangeListener(radioButtonListener);
			mMyNotesButton = (RadioButton) view
					.findViewById(R.id.my_notes_button);
			mMyNotesButton.setOnCheckedChangeListener(radioButtonListener);

			mHandler = new BeerNotesHandler(this);

			mViewPager = (ViewPager) view.findViewById(R.id.beernotes_pager);
			mAdapter = new BeerNotePagerAdapter(this, null);
			mViewPager.setAdapter(mAdapter);

			radioButtonListener.onCheckedChanged(mBeermeNotesButton,
					mBeermeNotesButton.isChecked());
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		mMyList = null;
		radioButtonListener.onCheckedChanged(mMyNotesButton,
				mMyNotesButton.isChecked());
	}

	private OnCheckedChangeListener radioButtonListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			BeerNotesList list;

			if (isChecked) {
				switch (buttonView.getId()) {
				case R.id.my_notes_button:
					list = mMyList;
					mSource = Source.MY;
					break;
				case R.id.beerme_notes_button:
					list = mBeermeList;
					mSource = Source.BEERME;
					break;
				default:
					list = null;
					mSource = null;
				}

				if (mSource != null) {
					if (list == null) {
						mProgress.setVisibility(View.VISIBLE);
						new Thread(new LoadNotesList(mSource), "LoadBeerNotes")
								.start();
					} else {
						refresh(list);
					}
				} else {
					Log.e(Utils.APPTAG,
							"BeerNotesFrag.onCheckedChanged(): null source");
				}
			}
		}
	};

	private class LoadNotesList implements Runnable {
		private Source mSource;

		public LoadNotesList(Source source) {
			this.mSource = source;
		}

		@Override
		public void run() {
			switch (mSource) {
			case BEERME:
				mBeermeList = new BeerNotesList(getActivity(), mBeerId, mSource);
				mHandler.sendEmptyMessage(LOADED_BEERME);
				break;
			case MY:
				mMyList = new BeerNotesList(getActivity(), mBeerId, mSource);
				mHandler.sendEmptyMessage(LOADED_MY);
				break;
			}
		}
	}

	private final static int LOADED_BEERME = 1;
	private final static int LOADED_MY = 2;

	private final static class BeerNotesHandler extends Handler {
		private WeakReference<BeerNotesFrag> mFrag;

		public BeerNotesHandler(BeerNotesFrag aFrag) {
			mFrag = new WeakReference<BeerNotesFrag>(aFrag);
		}

		@Override
		public void handleMessage(Message msg) {
			BeerNotesFrag theFrag = mFrag.get();

			switch (msg.what) {
			case LOADED_BEERME:
				theFrag.refresh(theFrag.mBeermeList);
				break;
			case LOADED_MY:
				theFrag.refresh(theFrag.mMyList);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void refresh(BeerNotesList list) {
		mAdapter.setData(list);
		mAdapter.notifyDataSetChanged();
		mViewPager.invalidate();

		mProgress.setVisibility(View.GONE);
	}

	public class BeerNotePagerAdapter extends FragmentStatePagerAdapter {
		private BeerNotesFrag mFragment;
		private BeerNotesList list;

		public BeerNotePagerAdapter(Fragment fragment, BeerNotesList list) {
			super(getChildFragmentManager());

			mFragment = (BeerNotesFrag) fragment;
			setData(list);
		}

		@Override
		public Fragment getItem(int position) {
			BeerNoteFrag frag = null;

			if (list != null) {
				frag = BeerNoteFrag.getInstance(list.get(position));
				frag.registerListener(mFragment);
			}

			return frag;
		}

		public void setData(BeerNotesList list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list != null) {
				return list.size();
			} else {
				return 0;
			}
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.note_actions, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			addNote();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void addNote() {
		Intent intent = new Intent(getActivity(), EditableBeerNote.class);
		intent.putExtra("beerid", mBeerId);
		startActivity(intent);
	}

	@Override
	public void onBeerNoteDeleted() {
		this.onResume();
	}
}