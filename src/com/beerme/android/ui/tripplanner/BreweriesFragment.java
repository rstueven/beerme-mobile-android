package com.beerme.android.ui.tripplanner;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.androidmapsextensions.PolylineOptions;
import com.beerme.android.database.Brewery;
import com.beerme.android.database.BreweryList;
import com.beerme.android.prefs.BreweryStatusFilterPreference;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.ui.MainActivity;
import com.beerme.android.ui.tripplanner.MessageHandler.MessageListener;
import com.beerme.android.ui.tripplanner.directions.Directions;
import com.beerme.android.ui.tripplanner.directions.Route;
import com.beerme.android.ui.tripplanner.directions.Segment;
import com.beerme.android.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class BreweriesFragment extends Fragment {
	private ArrayList<MessageListener> mMessageListeners = new ArrayList<MessageListener>();
	private ArrayList<BreweriesListener> mBreweriesListeners = new ArrayList<BreweriesListener>();
	private Thread fetcherThread;

	public static interface BreweriesListener {
		public void onBreweriesFetched(HashMap<Long, Brewery> breweries);

		public void drawCurrentPolyline(PolylineOptions options);

		public void removeCurrentPolyline();
	}

	public void registerMessageListener(MessageListener callback) {
		if (!mMessageListeners.contains(callback)) {
			mMessageListeners.add(callback);
		}
	}

	public void unregisterMessageListener(MessageListener callback) {
		if (mMessageListeners.contains(callback)) {
			mMessageListeners.remove(callback);
		}
	}

	public void registerBreweriesListener(BreweriesListener callback) {
		if (!mBreweriesListeners.contains(callback)) {
			mBreweriesListeners.add(callback);
		}
	}

	public void unregisterBreweriesListener(BreweriesListener callback) {
		if (mBreweriesListeners.contains(callback)) {
			mBreweriesListeners.remove(callback);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	public void getBreweries(Directions directions, Location location,
			int distance) {
		fetcherThread = new Thread(new BreweriesFetcher(directions, location,
				distance), "BreweriesFetcher");
		fetcherThread.start();
	}

	private class BreweriesFetcher implements Runnable {
		private Directions mDirections;
		private Location mLocation;
		private int mDistance;
		private Segment mOverview;

		public BreweriesFetcher(Directions directions, Location location,
				int distance) {
			this.mDirections = directions;
			this.mLocation = location;
			this.mDistance = distance;

			if (mDirections == null) {
				for (MessageListener l : mMessageListeners) {
					l.postException(new IllegalArgumentException(
							"BreweriesFetcher: null directions"));
				}
			} else {
				if (mLocation == null) {
					mLocation = ((MainActivity) getActivity()).getHere();
				}
				String status = mDirections.getStatus();
				if (!status.equals("OK")) {
					for (MessageListener l : mMessageListeners) {
						l.postException(new IllegalStateException(
								"BreweriesFetcher: directions status <"
										+ status + ">"));
					}
				} else {
					ArrayList<Route> routes = mDirections.getRoutes();
					if (routes.size() <= 0) {
						for (MessageListener l : mMessageListeners) {
							l.postException(new IllegalStateException(
									"BreweriesFetcher: no routes"));
						}
					} else {
						Route route = routes.get(0);
						this.mOverview = route.getOverviewPolyline();
						this.mDistance = distance;
					}
				}
			}
		}

		@Override
		public void run() {
			if (mOverview == null) {
				return;
			}

			SharedPreferences prefs = Prefs.getSettings(getActivity());
			int statusFilter = prefs.getInt(Prefs.KEY_STATUS_FILTER,
					BreweryStatusFilterPreference.DEFAULT_VALUE);

			Thread currentThread = Thread.currentThread();

			HashMap<Long, Brewery> breweries = new HashMap<Long, Brewery>();

			final double distTimes2 = mDistance * 2;
			int nSegments = mOverview.size();

			for (MessageListener l : mMessageListeners) {
				l.postMessage(MessageHandler.SEGMENTS_START, nSegments, 0);
			}

			Segment currentSegment = new Segment();
			LatLng currentSegmentStart = mOverview.get(0);
			currentSegment.add(currentSegmentStart);
			double segmentLength = 0;

			for (int i = 1; i < nSegments; i++) {
				if (currentThread.isInterrupted()) {
					clearResults();
					return;
				}

				for (MessageListener l : mMessageListeners) {
					l.postMessage(MessageHandler.SEGMENTS_INCREMENT, i, 0);
				}

				LatLng currentSegmentEnd = mOverview.get(i);
				currentSegment.add(currentSegmentEnd);
				segmentLength = Utils.distance(currentSegmentStart,
						currentSegmentEnd);

				if (segmentLength >= distTimes2 || i == nSegments - 1) {
					LatLngBounds bounds = Utils.expandBounds(getActivity(),
							currentSegment.getBounds(), mDistance);

					for (BreweriesListener l : mBreweriesListeners) {
						l.removeCurrentPolyline();
						l.drawCurrentPolyline(new PolylineOptions().zIndex(10)
								.color(0xff00ffff).addAll(currentSegment));
					}

					BreweryList breweryList = new BreweryList(getActivity(),
							statusFilter, mLocation, bounds);

					if (breweryList != null) {
						for (Brewery brewery : breweryList) {
							if (currentThread.isInterrupted()) {
								clearResults();
								return;
							}

							long id = Long.valueOf(brewery.getId());
							if (!breweries.containsKey(id)) {
								LatLng p = new LatLng(brewery.getLatitude(),
										brewery.getLongitude());
								double distToPoint = currentSegment
										.distanceToPoint(p);
								if (distToPoint <= mDistance) {
									breweries.put(id, brewery);
								}
							}
						}

						currentSegment = new Segment();
						currentSegmentStart = currentSegmentEnd;
						currentSegment.add(currentSegmentStart);
						segmentLength = 0;
					}
				}
			}

			for (MessageListener l : mMessageListeners) {
				l.postEmptyMessage(MessageHandler.SEGMENTS_END);
			}

			for (BreweriesListener l : mBreweriesListeners) {
				l.removeCurrentPolyline();
				l.onBreweriesFetched(breweries);
			}
		}

		private void clearResults() {

			for (MessageListener l : mMessageListeners) {
				l.postEmptyMessage(MessageHandler.SEGMENTS_END);
			}

			for (BreweriesListener l : mBreweriesListeners) {
				l.removeCurrentPolyline();
				l.onBreweriesFetched(null);
			}
		}
	}

	public void interrupt() {
		if (isRunning()) {
			fetcherThread.interrupt();
		}
	}

	public boolean isRunning() {
		return (fetcherThread != null && fetcherThread.isAlive());
	}
}