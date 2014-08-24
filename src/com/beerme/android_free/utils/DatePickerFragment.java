package com.beerme.android_free.utils;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {
	private static final String TAG_CALENDAR = "calendar";
	private DateSetter mDateSetter;
	private Calendar cal;

	public interface DateSetter {
		void setDateFromDatePicker(int y, int m, int d);
	}

	public static DatePickerFragment newInstance() {
		return newInstance(Calendar.getInstance());
	}

	public static DatePickerFragment newInstance(Calendar c) {
		DatePickerFragment dpf = new DatePickerFragment();
		Bundle args = new Bundle();
		args.putSerializable(TAG_CALENDAR, c);
		dpf.setArguments(args);
		return dpf;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mDateSetter = (DateSetter) activity;

		Bundle args = getArguments();
		if (args != null) {
			cal = (Calendar) args.getSerializable(TAG_CALENDAR);
		} else {
			cal = Calendar.getInstance();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		mDateSetter.setDateFromDatePicker(year, month, day);
	}
}