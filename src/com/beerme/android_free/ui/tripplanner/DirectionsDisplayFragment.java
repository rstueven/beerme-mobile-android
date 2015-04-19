package com.beerme.android_free.ui.tripplanner;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.beerme.android_free.R;
import com.beerme.android_free.ui.tripplanner.directions.Directions;
import com.beerme.android_free.ui.tripplanner.directions.Leg;
import com.beerme.android_free.ui.tripplanner.directions.Route;
import com.beerme.android_free.ui.tripplanner.directions.Step;
import com.beerme.android_free.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

public class DirectionsDisplayFragment extends Fragment {
	private Activity mActivity;
	private ExpandableListView mListView;
	private Directions directions;
	private Route route;
	List<Route> routes;
	List<Leg> legs;

	public static DirectionsDisplayFragment newInstance(String savedDirections) {
		DirectionsDisplayFragment f = new DirectionsDisplayFragment();

		Bundle args = new Bundle();
		args.putString(TripPlannerFrag.DIRECTIONS_FILE_TAG, savedDirections);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivity = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		Bundle args = getArguments();
		if (args != null) {
			String dirFile = args
					.getString(TripPlannerFrag.DIRECTIONS_FILE_TAG);
			directions = TripPlannerFrag.loadDirections(mActivity, dirFile);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		routes = directions.getRoutes();
		route = routes.get(0);

		legs = route.getLegs();

		mListView.setAdapter(new DirectionsDisplayAdapter(mActivity, legs));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.directionsdisplayfragment,
				container, false);
		setHasOptionsMenu(true);

		mListView = (ExpandableListView) view.findViewById(R.id.directionslist);

		return view;
	}

	// http://stackoverflow.com/questions/18594744/how-to-navigate-of-a-map-android-maps-api-v2

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.directions_actions, menu);
		if (!Utils.isNavigationAvailable(getActivity())) {
			menu.findItem(R.id.action_navigation).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_navigation:
			Leg lastLeg = legs.get(legs.size() - 1);
			LatLng endLoc = lastLeg.getEndLocation();
			double lat = endLoc.latitude;
			double lng = endLoc.longitude;
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("google.navigation:q=" + lat + "," + lng));
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// http://about-android.blogspot.ca/2010/04/steps-to-implement-expandablelistview.html
	public class DirectionsDisplayAdapter extends BaseExpandableListAdapter {
		private Context mContext;
		private List<Leg> mLegs;
		private List<List<Step>> mSteps = new ArrayList<List<Step>>();

		public DirectionsDisplayAdapter(Context context, List<Leg> legs) {
			this.mContext = context;
			this.mLegs = legs;

			for (Leg leg : mLegs) {
				mSteps.add(loadSteps(leg));
			}
		}

		private List<Step> loadSteps(Leg leg) {
			List<Step> steps = new ArrayList<Step>();

			List<Step> legSteps = leg.getSteps();
			for (Step step : legSteps) {
				steps.add(step);
			}

			return steps;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mSteps.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public TextView getGenericView() {
			// Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 64);

			TextView textView = new TextView(mContext);
			textView.setLayoutParams(lp);
			// Center the text vertically
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			textView.setTextColor(0xffff0000);
			// Set the text starting position
			textView.setPadding(36, 0, 0, 0);
			return textView;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TextView textView = getGenericView();
			// textView.setText(getGroup(groupPosition).toString());
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View groupView = inflater.inflate(R.layout.directionsdisplaygroup,
					parent, false);

			Leg leg = mLegs.get(groupPosition);

			TextView originView = (TextView) groupView
					.findViewById(R.id.origin);
			TextView destinationView = (TextView) groupView
					.findViewById(R.id.destination);
			TextView distanceView = (TextView) groupView
					.findViewById(R.id.distance);
			TextView durationView = (TextView) groupView
					.findViewById(R.id.duration);

			originView.setText("From: " + leg.getStartAddress());
			destinationView.setText("To: " + leg.getEndAddress());
			distanceView.setText(leg.getDistanceText());
			durationView.setText(leg.getDurationText());

			return groupView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View groupView = inflater.inflate(R.layout.directionsdisplayitem,
					parent, false);

			Step step = mSteps.get(groupPosition).get(childPosition);

			ImageView imageView = (ImageView) groupView
					.findViewById(R.id.direction_image);
			TextView textView = (TextView) groupView
					.findViewById(R.id.direction_text);
			TextView distanceView = (TextView) groupView
					.findViewById(R.id.direction_distance);
			TextView durationView = (TextView) groupView
					.findViewById(R.id.direction_duration);

			textView.setText(Html.fromHtml(step.getHtmlInstructions()));
			distanceView.setText(step.getDistanceText());
			durationView.setText(step.getDurationText());

			int maneuverImage = R.drawable.ic_blank;
			String maneuver = step.getManeuver();

			if (maneuver != null) {
				if (maneuver.equals("turn-left")) {
					maneuverImage = R.drawable.ic_turn_left;
				} else if (maneuver.equals("turn-right")) {
					maneuverImage = R.drawable.ic_turn_right;
				} else if (maneuver.equals("fork-left")) {
					maneuverImage = R.drawable.ic_fork_left;
				} else if (maneuver.equals("fork-right")) {
					maneuverImage = R.drawable.ic_fork_right;
				} else if (maneuver.equals("turn-slight-left")) {
					maneuverImage = R.drawable.ic_turn_slight_left;
				} else if (maneuver.equals("turn-slight-right")) {
					maneuverImage = R.drawable.ic_turn_slight_right;
				} else if (maneuver.equals("merge")) {
					maneuverImage = R.drawable.ic_merge;
				} else if (maneuver.equals("straight")) {
					maneuverImage = R.drawable.ic_straight;
				} else if (maneuver.equals("ramp")) { /* *** */
				} else if (maneuver.equals("ramp-left")) {
					maneuverImage = R.drawable.ic_ramp_left;
				} else if (maneuver.equals("ramp-right")) {
					maneuverImage = R.drawable.ic_ramp_right;
				} else if (maneuver.equals("keep-left")) { /* *** */
				} else if (maneuver.equals("keep-right")) { /* *** */
				}
			}

			imageView.setImageResource(maneuverImage);

			return groupView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int i = 0;
			try {
				i = mSteps.get(groupPosition).size();
			} catch (IndexOutOfBoundsException e) {
				// Log.i(Utils.APPTAG, e.getLocalizedMessage());
				// Ignore
			}

			return i;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mLegs.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return mLegs.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
}