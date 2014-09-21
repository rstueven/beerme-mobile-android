package com.beerme.android_free.ui.tripplanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.beerme.android_free.R;

public class DirectionsDisplay extends FragmentActivity {
	public static final String DIRECTIONS_DISPLAY_TAG = "directions_display";
	private DirectionsDisplayFragment mDisplayFrag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directionsdisplay);

		Intent intent = getIntent();

		String directionsFile = intent
				.getStringExtra(TripPlannerFrag.DIRECTIONS_FILE_TAG);

		if (savedInstanceState == null) {
			mDisplayFrag = DirectionsDisplayFragment
					.newInstance(directionsFile);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.directions_display_frame, mDisplayFrag).commit();
		}
	}
}