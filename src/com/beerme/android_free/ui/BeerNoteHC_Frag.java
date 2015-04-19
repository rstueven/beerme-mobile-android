package com.beerme.android_free.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import com.beerme.android_free.R;
import com.beerme.android_free.utils.Utils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BeerNoteHC_Frag extends BeerNoteFrag implements
		ActionMode.Callback {
	ActionMode mActionMode = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	protected void setupContextMenu(final ViewGroup layoutView) {
		layoutView.setLongClickable(true);
		layoutView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (mActionMode != null) {
					return false;
				}

				mActionMode = startBeerNoteActionMode();
				layoutView.setSelected(true);
				return true;
			}
		});
	}

	private ActionMode startBeerNoteActionMode() {
		return getActivity().startActionMode(this);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share:
			share();
			mode.finish();
			return true;
		case R.id.edit:
			edit();
			mode.finish();
			return true;
		case R.id.delete:
			delete();
			mode.finish();
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// Inflate a menu resource providing context menu items
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(CONTEXT_MENU_ID, menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mActionMode = null;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

}