package com.beerme.android.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.beerme.android.database.BreweryNote;
import com.beerme.android.ui.actionbar.BeerMeActionBarActivity;
import com.beerme.android.utils.DatePickerFragment;
import com.beerme.android.utils.RatingPickerFragment;
import com.beerme.android.utils.Utils;
import com.beerme.android.R;

public class EditableBreweryNote extends BeerMeActionBarActivity implements
		DatePickerFragment.DateSetter, RatingPickerFragment.RatingListener {
	private BreweryNote mNote = null;
	private long mBreweryId = -1;
	private TextView mVisitedView;
	private TextView mRatingView;
	private float mRating = 0;
	private EditText mNotesView;
	private DateFormat mDateFormat = DateFormat.getDateInstance(
			DateFormat.LONG, Locale.getDefault());

	// LOW: AND0092: RFE: Allow user to choose rating method

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editable_brewery_note);

		mVisitedView = (TextView) findViewById(R.id.editablebrewerynote_visited);
		mVisitedView.setOnClickListener(onSampledViewClicked);

		mRatingView = (TextView) findViewById(R.id.editablebrewerynote_rating);
		mRatingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RatingPickerFragment newFragment = RatingPickerFragment
						.newInstance(mRatingView.getId(), mNote == null ? 0
								: mNote.getRating(), 5);
				newFragment.show(getSupportFragmentManager(),
						"breweryRatingPicker");
			}
		});

		mNotesView = (EditText) findViewById(R.id.editablebrewerynote_notes);

		if (savedInstanceState != null) {
			mNote = (BreweryNote) savedInstanceState.getSerializable("note");
		} else {
			Intent intent = getIntent();
			mNote = (BreweryNote) intent.getSerializableExtra("note");
			mBreweryId = intent.getLongExtra("breweryid", -1);
		}

		if (mNote != null) {
			mBreweryId = mNote.getBreweryId();
			String date = mNote.getDate();
			mVisitedView.setText(date);
			mRating = mNote.getRating();
			mRatingView.setText(getString(R.string.N_out_of_5_points,
					Utils.toFrac(mRating)));
			mNotesView.setText(mNote.getNotes());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.trackActivityStart(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		mNote = makeNewNote(mNote == null ? -1 : mNote.getId());
		outState.putSerializable("note", mNote);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		super.onStop();
		Utils.trackActivityStop(this);
	}

	private OnClickListener onSampledViewClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Calendar cal = Calendar.getInstance(Locale.getDefault());
			String sampled = mVisitedView.getText().toString();

			if (!"".equals(sampled)) {
				try {
					cal.setTime(mDateFormat.parse(sampled));
				} catch (ParseException e) {
					Log.w(Utils.APPTAG, e.getLocalizedMessage());
				}
			}

			DialogFragment dialog = DatePickerFragment.newInstance(cal);
			dialog.show(getSupportFragmentManager(), "datePicker");
		}
	};

	@Override
	public void setDateFromDatePicker(int y, int m, int d) {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.set(y, m, d);
		mVisitedView.setText(mDateFormat.format(cal.getTime()));
	}

	@Override
	public void onRatingSet(int viewId, float rating) {
		switch (viewId) {
		case R.id.editablebrewerynote_rating:
			mRatingView.setText(getString(R.string.N_out_of_5_points,
					Utils.toFrac(rating)));
			mRating = rating;
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.editable_note, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_save:
			mNote = makeNewNote(mNote == null ? -1 : mNote.getId());
			mNote.save(this);
			this.finish();
			return true;
		case R.id.action_cancel:
			this.finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private BreweryNote makeNewNote(final long noteid) {
		BreweryNote note = BreweryNote.newInstance(this, noteid);
		note.setBreweryId(mBreweryId);
		note.setDate(mVisitedView.getText().toString());
		note.setNotes(mNotesView.getText().toString());
		note.setRating(mRating);
		return note;
	}
}