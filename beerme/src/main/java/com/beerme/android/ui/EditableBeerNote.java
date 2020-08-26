package com.beerme.android.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.database.BeerNote;
import com.beerme.android.database.BeerNote.Source;
import com.beerme.android.utils.DatePickerFragment;
import com.beerme.android.utils.PkgPickerFragment;
import com.beerme.android.utils.RatingPickerFragment;
import com.beerme.android.utils.Utils;

public class EditableBeerNote extends BeerMeActivity implements
		DatePickerFragment.DateSetter, PkgPickerFragment.PkgSetter,
		RatingPickerFragment.RatingListener {
	private BeerNote mNote = null;
	private long mBeerId = -1;
	private TextView mSampledView;
	private TextView mScoreView;
	private TextView mPkgView;
	private EditText mPlaceView;
	private TextView mAppRatingView;
	private float mAppRating = 0;
	private EditText mAppView;
	private TextView mAroRatingView;
	private float mAroRating = 0;
	private EditText mAroView;
	private TextView mMouRatingView;
	private float mMouRating = 0;
	private EditText mMouView;
	private TextView mOvrRatingView;
	private float mOvrRating = 0;
	private EditText mNotesView;
	private DateFormat mDateFormat = DateFormat.getDateInstance(
			DateFormat.LONG, Locale.getDefault());

	// LOW: AND0092: RFE: Allow user to choose rating method
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editable_beer_note);

		mSampledView = (TextView) findViewById(R.id.editablebeernote_sampled);
		mSampledView.setOnClickListener(onSampledViewClicked);

		mScoreView = (TextView) findViewById(R.id.editablebeernote_score);

		mPkgView = (TextView) findViewById(R.id.editablebeernote_pkg);
		mPkgView.setOnClickListener(onPkgViewClicked);

		mPlaceView = (EditText) findViewById(R.id.editablebeernote_place);

		mAppRatingView = (TextView) findViewById(R.id.editablebeernote_appRating);
		mAppRatingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RatingPickerFragment newFragment = RatingPickerFragment
						.newInstance(mAppRatingView.getId(), mNote == null ? 0
								: mNote.getAppscore(), 3);

				newFragment
						.show(getSupportFragmentManager(), "appRatingPicker");
			}
		});

		mAppView = (EditText) findViewById(R.id.editablebeernote_appearance);

		mAroRatingView = (TextView) findViewById(R.id.editablebeernote_aroRating);
		mAroRatingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RatingPickerFragment newFragment = RatingPickerFragment
						.newInstance(mAroRatingView.getId(), mNote == null ? 0
								: mNote.getAroscore(), 4);
				newFragment
						.show(getSupportFragmentManager(), "aroRatingPicker");
			}
		});

		mAroView = (EditText) findViewById(R.id.editablebeernote_aroma);

		mMouRatingView = (TextView) findViewById(R.id.editablebeernote_mouRating);
		mMouRatingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RatingPickerFragment newFragment = RatingPickerFragment
						.newInstance(mMouRatingView.getId(), mNote == null ? 0
								: mNote.getMouscore(), 10);
				newFragment
						.show(getSupportFragmentManager(), "mouRatingPicker");
			}
		});

		mMouView = (EditText) findViewById(R.id.editablebeernote_mouthfeel);

		mOvrRatingView = (TextView) findViewById(R.id.editablebeernote_ovrRating);
		mOvrRatingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RatingPickerFragment newFragment = RatingPickerFragment
						.newInstance(mOvrRatingView.getId(), mNote == null ? 0
								: mNote.getOvrscore(), 3);
				newFragment
						.show(getSupportFragmentManager(), "ovrRatingPicker");
			}
		});

		mNotesView = (EditText) findViewById(R.id.editablebeernote_notes);

		if (savedInstanceState != null) {
			mNote = (BeerNote) savedInstanceState.getSerializable("note");
		} else {
			Intent intent = getIntent();
			mNote = (BeerNote) intent.getSerializableExtra("note");
			mBeerId = intent.getLongExtra("beerid", -1);
		}

		if (mNote != null) {
			mBeerId = mNote.getBeerId();
			mSampledView.setText(mNote.getSampled());
			mScoreView.setText(getString(R.string.N_out_of_20_points,
					Utils.toFrac(mNote.getScore())));
			mPkgView.setText(mNote.getPkg());
			mPlaceView.setText(mNote.getPlace());
			mAppRating = mNote.getAppscore();
			mAppRatingView.setText(getString(R.string.N_outOfThree,
					Utils.toFrac(mAppRating)));
			mAppView.setText(mNote.getAppearance());
			mAroRating = mNote.getAroscore();
			mAroRatingView.setText(getString(R.string.N_outOfFour,
					Utils.toFrac(mAroRating)));
			mAroView.setText(mNote.getAroma());
			mMouRating = mNote.getMouscore();
			mMouRatingView.setText(getString(R.string.N_outOfTen,
					Utils.toFrac(mMouRating)));
			mMouView.setText(mNote.getMouthfeel());
			mOvrRating = mNote.getOvrscore();
			mOvrRatingView.setText(getString(R.string.N_outOfThree,
					Utils.toFrac(mOvrRating)));
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
			String sampled = mSampledView.getText().toString();

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
		mSampledView.setText(mDateFormat.format(cal.getTime()));
	}

	private OnClickListener onPkgViewClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogFragment newFragment = new PkgPickerFragment();
			newFragment.show(getSupportFragmentManager(), "pkgPicker");
		}
	};

	@Override
	public void setPkgFromPicker(String p) {
		mPkgView.setText(p);
	}

	@Override
	public void onRatingSet(int viewId, float rating) {
		switch (viewId) {
		case R.id.editablebeernote_appRating:
			mAppRatingView.setText(getString(R.string.N_outOfThree,
					Utils.toFrac(rating)));
			mAppRating = rating;
			break;
		case R.id.editablebeernote_aroRating:
			mAroRatingView.setText(getString(R.string.N_outOfFour,
					Utils.toFrac(rating)));
			mAroRating = rating;
			break;
		case R.id.editablebeernote_mouRating:
			mMouRatingView.setText(getString(R.string.N_outOfTen,
					Utils.toFrac(rating)));
			mMouRating = rating;
			break;
		case R.id.editablebeernote_ovrRating:
			mOvrRatingView.setText(getString(R.string.N_outOfThree,
					Utils.toFrac(rating)));
			mOvrRating = rating;
			break;
		}

		mScoreView
				.setText(getString(
						R.string.N_out_of_20_points,
						Utils.toFrac(mAppRating + mAroRating + mMouRating
								+ mOvrRating)));
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

	private BeerNote makeNewNote(final long noteid) {
		BeerNote note = BeerNote.newInstance(this, noteid, Source.MY);
		note.setAppearance(mAppView.getText().toString());
		note.setAppscore(mAppRating);
		note.setAroma(mAroView.getText().toString());
		note.setAroscore(mAroRating);
		note.setBeerId(mBeerId);
		note.setMouscore(mMouRating);
		note.setMouthfeel(mMouView.getText().toString());
		note.setNotes(mNotesView.getText().toString());
		note.setOvrscore(mOvrRating);
		note.setPkg(mPkgView.getText().toString());
		note.setPlace(mPlaceView.getText().toString());
		note.setSampled(mSampledView.getText().toString());
		return note;
	}
}