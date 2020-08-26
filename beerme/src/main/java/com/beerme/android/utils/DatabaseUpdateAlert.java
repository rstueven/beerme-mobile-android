package com.beerme.android.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.beerme.android.R;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.prefs.SharedPreferenceSaver;

public class DatabaseUpdateAlert extends DialogFragment {
	private FragmentActivity mContext;
	private DatabaseUpdateAlerter mCallback = null;
	public static final String SHOW_DB_UPDATE_ALERT_PREF = "showDbUpdateAlert";

	public interface DatabaseUpdateAlerter {
		public void onDatabaseUpdateAlert();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = this.getActivity();
		try {
			mCallback = (DatabaseUpdateAlerter) mContext;
		} catch (ClassCastException e) {
			mCallback = null;
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
		View view = inflater.inflate(R.layout.database_update, container);
		CheckBox dontShow = (CheckBox) view.findViewById(R.id.dontshowagain);
		Button okButton = (Button) view.findViewById(R.id.ok);
		getDialog().setTitle(R.string.Database_update);

		dontShow.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences.Editor editor = Prefs
						.getSettingsEditor(mContext);
				SharedPreferenceSaver saver = Prefs.getSettingsSaver(mContext);
				editor.putBoolean(SHOW_DB_UPDATE_ALERT_PREF, !isChecked);
				saver.savePreferences(editor, false);
			}
		});

		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.onDatabaseUpdateAlert();
				}
				getDialog().dismiss();
			}
		});

		return view;
	}
}