package com.beerme.android.ui.tripplanner;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.beerme.android.R;
import com.beerme.android.utils.Utils;

public class MarkerDialogFragment extends DialogFragment {

	public interface MarkerDialogListener {
		public void onBreweryDetails(long id);

		public void onAddToRoute(long id);

		public void onRemoveFromRoute(long id);

		public void onDeleteMarker(long id);

		public void onUseAsOrigin(long id);

		public void onUseAsDestination(long id);

		public void onZoomHere(long id);
	}

	private ArrayList<MarkerDialogListener> mListeners = new ArrayList<MarkerDialogListener>();
	private long mId;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	public void registerListener(MarkerDialogListener callback) {
		if (!mListeners.contains(callback)) {
			mListeners.add(callback);
		}
	}

	public void unregisterListener(MarkerDialogListener callback) {
		if (mListeners.contains(callback)) {
			mListeners.remove(callback);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mId = this.getArguments().getLong("id");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setItems(R.array.marker_dialog,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Same order as R.array.marker_dialog!
						switch (which) {
						case 0:
							for (MarkerDialogListener l : mListeners) {
								l.onBreweryDetails(mId);
							}
							break;
						case 1:
							for (MarkerDialogListener l : mListeners) {
								l.onAddToRoute(mId);
							}
							break;
						case 2:
							for (MarkerDialogListener l : mListeners) {
								l.onRemoveFromRoute(mId);
							}
							break;
						case 3:
							for (MarkerDialogListener l : mListeners) {
								l.onDeleteMarker(mId);
							}
							break;
						case 4:
							for (MarkerDialogListener l : mListeners) {
								l.onUseAsOrigin(mId);
							}
							break;
						case 5:
							for (MarkerDialogListener l : mListeners) {
								l.onUseAsDestination(mId);
							}
							break;
						case 6:
							for (MarkerDialogListener l : mListeners) {
								l.onZoomHere(mId);
							}
							break;
						default:
							break;
						}
					}
				});

		return builder.create();
	}
}