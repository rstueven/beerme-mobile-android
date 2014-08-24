package com.beerme.android_free.utils;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.beerme.android_free.R;

public class StatusPickerFragment extends DialogFragment {
	private static final int STATUS_ARRAY = R.array.status_value;
	protected ArrayList<StatusSetter> mCallbacks = new ArrayList<StatusSetter>();

	public interface StatusSetter {
		public void setStatusFromPicker(int status);
	}

	public static StatusPickerFragment newInstance() {
		StatusPickerFragment frag = new StatusPickerFragment();
		return frag;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.choose_status).setItems(STATUS_ARRAY,
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						notifyListeners(which);
					}
				});

		return builder.create();
	}

	public void registerListener(StatusSetter callback) {
		if (!mCallbacks.contains(callback)) {
			mCallbacks.add(callback);
		}
	}

	public void unregisterListener(StatusSetter callback) {
		if (mCallbacks.contains(callback)) {
			mCallbacks.remove(callback);
		}
	}

	private void notifyListeners(int status) {
		for (StatusSetter listener : mCallbacks) {
			listener.setStatusFromPicker(status);
		}
	}
}