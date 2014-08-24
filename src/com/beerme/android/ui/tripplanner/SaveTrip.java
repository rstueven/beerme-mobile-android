package com.beerme.android.ui.tripplanner;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.beerme.android.R;

public class SaveTrip extends DialogFragment {
	private static final String START_TAG = "start";
	private static final String END_TAG = "end";
	private ArrayList<SaveTripListener> mListeners = new ArrayList<SaveTripListener>();
	private String mStart;
	private String mEnd;

	public interface SaveTripListener {
		public void onSaveTrip(String name);
	}

	public static SaveTrip newInstance(String startLoc, String endLoc) {
		SaveTrip f = new SaveTrip();

		Bundle args = new Bundle();
		args.putString(START_TAG, (startLoc == null) ? "" : startLoc);
		args.putString(END_TAG, (endLoc == null) ? "" : endLoc);
		f.setArguments(args);

		return f;
	}

	public void registerListener(SaveTripListener callback) {
		if (!mListeners.contains(callback)) {
			mListeners.add(callback);
		}
	}

	public void unregisterListener(SaveTripListener callback) {
		if (mListeners.contains(callback)) {
			mListeners.remove(callback);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mStart = args.getString(START_TAG);
			mEnd = args.getString(END_TAG);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.savetrip, null);
		final EditText nameView = (EditText) view.findViewById(R.id.name);

		StringBuffer defaultFileName = new StringBuffer();

		if (!"".equals(mStart)) {
			defaultFileName.append(mStart + '-');
		}

		defaultFileName.append(mEnd);

		nameView.setText(defaultFileName.toString());

		builder.setTitle(R.string.action_save);

		builder.setView(view)
				// Add action buttons
				.setPositiveButton(R.string.Save,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								final String name = nameView.getText()
										.toString();
								if (!name.equals("")) {
									for (SaveTripListener l : mListeners) {
										l.onSaveTrip(name);
									}
								}
							}
						})
				.setNegativeButton(R.string.Cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								SaveTrip.this.getDialog().cancel();
							}
						});
		return builder.create();
	}
}