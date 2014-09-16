package com.beerme.android.filechooser;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.beerme.android.utils.Utils;
import com.beerme.android.R;

public class FileExistsFragment extends DialogFragment {
	private static final String FILENAME_KEY = "filename";
	private String mFilename;
	private ArrayList<FileExistsListener> mListeners = new ArrayList<FileExistsListener>();

	public interface FileExistsListener {
		public void onFileExists(String name, boolean replace);
	}

	public static FileExistsFragment newInstance(String filename) {
		if (filename == null) {
			throw new IllegalArgumentException(
					"FileExistsFragment: null filename");
		}

		if (filename.equals("")) {
			throw new IllegalArgumentException(
					"FileExistsFragment: empty filename");
		}

		if (filename.contains("/")) {
			throw new IllegalArgumentException(
					"FileExistsFragment: invalid filename (" + filename + ")");
		}

		FileExistsFragment frag = new FileExistsFragment();
		Bundle args = new Bundle();
		args.putString(FILENAME_KEY, filename);
		frag.setArguments(args);

		return frag;
	}

	public void registerListener(FileExistsListener callback) {
		if (!mListeners.contains(callback)) {
			mListeners.add(callback);
		}
	}

	public void unregisterListener(FileExistsListener callback) {
		if (mListeners.contains(callback)) {
			mListeners.remove(callback);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		mFilename = args.getString(FILENAME_KEY);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Resources res = getResources();
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(
				String.format(res.getString(R.string.File_exists), mFilename))
				.setTitle(R.string.file_exists)
				.setPositiveButton(R.string.Replace,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								for (FileExistsListener l : mListeners) {
									l.onFileExists(mFilename, true);
								}
							}
						})
				.setNegativeButton(R.string.Cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								for (FileExistsListener l : mListeners) {
									l.onFileExists(mFilename, true);
								}
							}
						});
		// Create the AlertDialog object and return it
		return builder.create();
	}
}