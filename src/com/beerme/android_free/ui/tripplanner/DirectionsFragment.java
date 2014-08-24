package com.beerme.android_free.ui.tripplanner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.beerme.android_free.R;
import com.beerme.android_free.ui.tripplanner.MessageHandler.MessageListener;
import com.beerme.android_free.ui.tripplanner.directions.Directions;
import com.beerme.android_free.utils.ErrLog;
import com.beerme.android_free.utils.Utils;

public class DirectionsFragment extends Fragment {
	private static final String DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json?";
	private Directions mDirections;
	private Thread fetcherThread;
	private ArrayList<MessageListener> mMessageListeners = new ArrayList<MessageListener>();
	private ArrayList<DirectionsListener> mDirectionsListeners = new ArrayList<DirectionsListener>();

	public static interface DirectionsListener {
		public void onDirectionsCalculated(Directions directions, int distance);
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

	public void registerDirectionsListener(DirectionsListener callback) {
		if (!mDirectionsListeners.contains(callback)) {
			mDirectionsListeners.add(callback);
		}
	}

	public void unregisterDirectionsListener(DirectionsListener callback) {
		if (mDirectionsListeners.contains(callback)) {
			mDirectionsListeners.remove(callback);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	public void fetchDirections(String startString, String endString,
			int distance, HashMap<Long, String> stops, Location here) {
		URL url = buildDirectionsUrl(startString, endString, stops, here);
		mDirections = null;

		fetcherThread = new Thread(new DirectionsFetcher(url, distance),
				"DirectionsFetcher");
		fetcherThread.start();
	}

	private URL buildDirectionsUrl(String start, String end,
			HashMap<Long, String> stops, Location here) {
		URL url = null;

		if ("".equals(start)) {
			if (here != null) {
				double startLat = here.getLatitude();
				double startLng = here.getLongitude();
				start = startLat + "," + startLng;
			} else {
				ErrLog.log(getActivity(), "DirectionsFragment.buildDirections",
						null, R.string.Unknown_location);
			}
		}

		if (!"".equals(start)) {
			StringBuffer urlBuf = new StringBuffer(DIRECTIONS_URL);

			try {
				urlBuf.append("origin=" + URLEncoder.encode(start, "UTF-8"));
				urlBuf.append("&destination=" + URLEncoder.encode(end, "UTF-8"));

				if (stops.size() > 0) {
					urlBuf.append("&waypoints=optimize:true");
					for (long id : stops.keySet()) {
						urlBuf.append("|"
								+ URLEncoder.encode(stops.get(id), "UTF-8"));
					}
				}
			} catch (UnsupportedEncodingException e) {
				for (MessageListener l : mMessageListeners) {
					l.postException(e);
				}
			}

			urlBuf.append("&sensor=true");

			try {
				url = new URL(urlBuf.toString());
			} catch (MalformedURLException e) {
				Log.w(Utils.APPTAG, e.getLocalizedMessage());
				for (MessageListener l : mMessageListeners) {
					l.postException(e);
				}
			}
		}

		return url;
	}

	public Directions getDirections() {
		return this.mDirections;
	}

	private class DirectionsFetcher implements Runnable {
		private URL url;
		private int distance;

		public DirectionsFetcher(URL url, int distance) {
			this.url = url;
			this.distance = distance;
		}

		@Override
		public void run() {
			if (url != null && distance > 0) {
				try {
					for (MessageListener l : mMessageListeners) {
						l.postEmptyMessage(MessageHandler.DIRECTIONS_START);
					}
					mDirections = new Directions(url);
					for (DirectionsListener l : mDirectionsListeners) {
						l.onDirectionsCalculated(mDirections, distance);
					}
				} catch (MalformedURLException e) {
					mDirections = null;
					Log.e(Utils.APPTAG, url.toString());
					for (MessageListener l : mMessageListeners) {
						l.postException(new MalformedURLException(
								"Failed to download directions"));
					}
				} catch (JSONException e) {
					mDirections = null;
					Log.e(Utils.APPTAG, url.toString());
					for (MessageListener l : mMessageListeners) {
						l.postException(new JSONException(
								"Failed to download directions"));
					}
				} catch (IOException e) {
					mDirections = null;
					Log.e(Utils.APPTAG, url.toString());
					for (MessageListener l : mMessageListeners) {
						l.postException(new IOException(
								"Failed to download directions"));
					}
				} finally {
					for (MessageListener l : mMessageListeners) {
						l.postEmptyMessage(MessageHandler.DIRECTIONS_END);
					}
				}
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