package com.beerme.android_free.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.beerme.android_free.R;

public class PkgPickerFragment extends DialogFragment {
	private PkgSetter mPkgSetter;
	private String[] pkgArray;
	private static final int PKG_ARRAY = R.array.pkg_value;

	public interface PkgSetter {
		public void setPkgFromPicker(String p);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mPkgSetter = (PkgSetter) activity;

		pkgArray = getResources().getStringArray(PKG_ARRAY);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.choose_pkg).setItems(PKG_ARRAY,
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						getPkgString(which);
					}
				});

		return builder.create();
	}

	private void getPkgString(int which) {
		mPkgSetter.setPkgFromPicker(pkgArray[which]);
	}
}