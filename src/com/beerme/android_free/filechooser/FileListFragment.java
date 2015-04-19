package com.beerme.android_free.filechooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.beerme.android_free.R;
import com.beerme.android_free.utils.Utils;
import com.beerme.android_free.utils.YesNoDialog;
import com.beerme.android_free.utils.YesNoDialog.YesNoListener;

public class FileListFragment extends DialogFragment implements YesNoListener {
	private static final String DIRECTORY_KEY = "directory";
	private static final String SUFFIX_KEY = "suffix";
	private static final int DELETE_FILE = 1;
	private Activity mActivity;
	private ArrayList<FileListListener> mListeners = new ArrayList<FileListListener>();
	private File mDirectory;
	private String mSuffix;
	private List<String> mFileNames;
	private FileListFragment mThis;

	public interface FileListListener {
		public void onFileSelected(String name);
	}

	public static FileListFragment newInstance(File directory, String suffix) {
		if (directory == null) {
			throw new IllegalArgumentException(
					"FileListFragment: null directory");
		}

		FileListFragment frag = new FileListFragment();
		frag.mThis = frag;
		Bundle args = new Bundle();
		args.putSerializable(DIRECTORY_KEY, directory);
		args.putString(SUFFIX_KEY, suffix);
		frag.setArguments(args);

		return frag;
	}

	public void registerListener(FileListListener callback) {
		if (!mListeners.contains(callback)) {
			mListeners.add(callback);
		}
	}

	public void unregisterListener(FileListListener callback) {
		if (mListeners.contains(callback)) {
			mListeners.remove(callback);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivity = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		this.mDirectory = (File) args.getSerializable(DIRECTORY_KEY);
		this.mSuffix = "." + args.getString(SUFFIX_KEY);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.filelist, null);
		ListView listView = (ListView) view.findViewById(R.id.filelist);
		mFileNames = new ArrayList<String>();

		builder.setTitle(R.string.Choose_a_file);

		File[] fileList = mDirectory.listFiles();

		for (File f : fileList) {
			if (f.isFile()) {
				String name = f.getName();
				if (name.endsWith(mSuffix)) {
					mFileNames.add(name.substring(0,
							name.length() - mSuffix.length()));
				}
			}
		}

		Collections.sort(mFileNames, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}
		});

		listView.setAdapter(new ArrayAdapter<String>(mActivity,
				android.R.layout.simple_list_item_1, mFileNames));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				String s = adapterView.getItemAtPosition(position).toString();
				for (FileListListener l : mListeners) {
					l.onFileSelected(s);
				}
				FileListFragment.this.getDialog().cancel();
			}

		});

		listView.setLongClickable(true);
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView,
					View view, int position, long id) {
				YesNoDialog dialog = YesNoDialog.getInstance(DELETE_FILE,
						mThis.getString(R.string.Delete_file), (long) position);
				dialog.registerListener(mThis);
				dialog.show(getChildFragmentManager(), "YesNoFragment");
				return false;
			}

		});

		builder.setView(view).setNegativeButton(R.string.Cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						FileListFragment.this.getDialog().cancel();
					}
				});
		return builder.create();
	}

	@Override
	public void onYes(int key, long data) {
		switch (key) {
		case DELETE_FILE:
			String s = mFileNames.get((int) data);
			String fileName = mDirectory + "/" + s + mSuffix;
			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}

			this.dismiss();
			break;
		}
	}
}