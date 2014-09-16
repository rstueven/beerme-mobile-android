package com.beerme.android.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.beerme.android.database.BreweryNotesList;
import com.beerme.android.ui.BreweryNoteFrag.BreweryNoteCallbacks;
import com.beerme.android.utils.Utils;
import com.beerme.android.R;

public class BreweryNotesFrag extends Fragment implements BreweryNoteCallbacks {
	private static final String TAG_ID = "id";
	private long mBreweryId = -1;
	private BreweryNotesList mList = null;
	private ViewPager mViewPager = null;
	private BreweryNotePagerAdapter mAdapter = null;
	private ProgressBar mProgress = null;
	private static BreweryNotesHandler mHandler = null;

	public static BreweryNotesFrag getInstance(long breweryId) {
		BreweryNotesFrag frag = new BreweryNotesFrag();

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
			mBreweryId = args.getLong(TAG_ID, -1);
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
		View view = inflater.inflate(R.layout.brewerynotes_frag, container,
				false);
		setHasOptionsMenu(true);

		if (mBreweryId > 0) {
			mProgress = (ProgressBar) view
					.findViewById(R.id.brewerynotes_progress);

			mHandler = new BreweryNotesHandler(this);

			mViewPager = (ViewPager) view.findViewById(R.id.brewerynotes_pager);
			mAdapter = new BreweryNotePagerAdapter(this, null);
			mViewPager.setAdapter(mAdapter);
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		mProgress.setVisibility(View.VISIBLE);
		new Thread(new LoadNotesList(), "LoadBreweryNotes").start();
	}

	private void refresh() {
		mAdapter.setData(mList);
		mAdapter.notifyDataSetChanged();
		mViewPager.invalidate();

		mProgress.setVisibility(View.GONE);
	}

	private class LoadNotesList implements Runnable {
		public LoadNotesList() {
		}

		@Override
		public void run() {
			mList = new BreweryNotesList(getActivity(), mBreweryId);
			mHandler.sendEmptyMessage(LOADED);
		}
	}

	private final static int LOADED = 1;

	private final static class BreweryNotesHandler extends Handler {
		private WeakReference<BreweryNotesFrag> mFrag;

		public BreweryNotesHandler(BreweryNotesFrag aFrag) {
			mFrag = new WeakReference<BreweryNotesFrag>(aFrag);
		}

		@Override
		public void handleMessage(Message msg) {
			BreweryNotesFrag theFrag = mFrag.get();

			switch (msg.what) {
			case LOADED:
				theFrag.refresh();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	public class BreweryNotePagerAdapter extends FragmentStatePagerAdapter {
		private BreweryNotesFrag mFragment;
		private BreweryNotesList list;

		public BreweryNotePagerAdapter(Fragment fragment, BreweryNotesList list) {
			super(getChildFragmentManager());

			mFragment = (BreweryNotesFrag) fragment;
			setData(list);
		}

		@Override
		public Fragment getItem(int position) {
			BreweryNoteFrag frag = null;

			if (list != null) {
				frag = BreweryNoteFrag.getInstance(mFragment,
						list.get(position));
				frag.registerListener(mFragment);
			}

			return frag;
		}

		public void setData(BreweryNotesList list) {
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
		Intent intent = new Intent(getActivity(), EditableBreweryNote.class);
		intent.putExtra("breweryid", mBreweryId);
		startActivity(intent);
	}

	@Override
	public void onBreweryNoteDeleted() {
		this.onResume();
	}
}