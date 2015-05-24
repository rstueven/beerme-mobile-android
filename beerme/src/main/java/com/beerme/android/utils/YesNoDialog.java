package com.beerme.android.utils;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.beerme.android.R;

public class YesNoDialog extends DialogFragment {
	private static final String TAG_KEY = "key";
	private static final String TAG_TITLE = "title";
	private static final String TAG_DATA = "data";
	protected ArrayList<YesNoListener> mCallbacks = new ArrayList<YesNoListener>();
	private int mKey;
	private String mTitle;
	private long mData;

	public interface YesNoListener {
		public void onYes(int key, long data);
	}

	/**
	 * @param frag
	 *            invoking Fragment that should implement YesNoListener
	 * @param key
	 *            int used to differentiate calls to onYes(int key, long data)
	 * @param title
	 *            String resource used as dialog title
	 * @param data
	 *            long passed through to onYes(int key, long data)
	 * @return new instance of YesNoDialog
	 */
	public static YesNoDialog getInstance(int key, String title, long data) {
		YesNoDialog dialog = new YesNoDialog();
		Bundle args = new Bundle();
		args.putInt(TAG_KEY, key);
		args.putString(TAG_TITLE, title);
		args.putLong(TAG_DATA, data);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		mKey = args.getInt(TAG_KEY);
		mTitle = args.getString(TAG_TITLE);
		mData = args.getLong(TAG_DATA);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(mTitle)
				.setPositiveButton(R.string.Yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								notifyListeners(mKey, mData);
							}
						})
				.setNegativeButton(R.string.No,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		return builder.create();
	}

	public void registerListener(YesNoListener callback) {
		if (!mCallbacks.contains(callback)) {
			mCallbacks.add(callback);
		}
	}

	public void unregisterListener(YesNoListener callback) {
		if (mCallbacks.contains(callback)) {
			mCallbacks.remove(callback);
		}
	}

	private void notifyListeners(int key, long data) {
		for (YesNoListener listener : mCallbacks) {
			listener.onYes(key, data);
		}
	}
}