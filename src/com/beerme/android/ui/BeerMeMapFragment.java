package com.beerme.android.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.OnCameraChangeListener;
import com.androidmapsextensions.GoogleMap.OnInfoWindowClickListener;
import com.androidmapsextensions.GoogleMap.OnMyLocationButtonClickListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.beerme.android.R;
import com.beerme.android.database.Brewery;
import com.beerme.android.database.DbOpenHelper;
import com.beerme.android.location.LocationFragment;
import com.beerme.android.prefs.BreweryStatusFilterPreference;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.ui.maps.BeerMeClusterOptionsProvider;
import com.beerme.android.ui.maps.BreweryInfoWindowAdapter;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.TouchableWrapper;
import com.beerme.android.utils.TouchableWrapper.UpdateMapAfterUserInteraction;
import com.beerme.android.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Displays a map of breweries.
 * 
 * @author rstueven
 * 
 */
public class BeerMeMapFragment extends Fragment implements
		LocationFragment.LocationCallbacks, UpdateMapAfterUserInteraction,
		OnCameraChangeListener, OnInfoWindowClickListener {
	/**
	 * Tag for the LocationFragment, which keeps track of the user's location
	 */
	public static final String LOCATION_FRAGMENT_TAG = "locationFragment";
	/**
	 * Parameter controlling the density of clustered Markers
	 */
	private static final double DEFAULT_CLUSTER_SIZE = 80;
	/**
	 * Default zoom level for the map
	 */
	private static final float DEFAULT_ZOOM = 15;
	/**
	 * savedInstanceState key for the map's latitude
	 */
	private static final String SAVE_LAT_KEY = "mMap.lat";
	/**
	 * savedInstanceState key for the map's longitude
	 */
	private static final String SAVE_LNG_KEY = "mMap.lng";
	/**
	 * savedInstanceState key for the map's zoom level
	 */
	private static final String SAVE_ZOOM_KEY = "mMap.zoom";
	/**
	 * savedInstanceState key for tracking the user's location
	 */
	private static final String SAVE_TRACKING_KEY = "tracking";
	/**
	 * Representation of the user's current brewery status filter.
	 * 
	 * @see BreweryStatusFilterPreference
	 */
	private int mStatusFilter = BreweryStatusFilterPreference.DEFAULT_VALUE;
	/**
	 * True if map should track user's location, false otherwise
	 */
	private boolean mTrackLocation = true;
	/**
	 * The map itself
	 */
	private GoogleMap mMap = null;
	/**
	 * {@link android.support.v4.app.FragmentManager}
	 */
	private FragmentManager mFragMgr;
	/**
	 * {@link pl.mg6.android.maps.extensions.SupportMapFragment}
	 */
	private SupportMapFragment mMapFrag = null;
	/**
	 * Current center point of the map
	 */
	private LatLng mLocation;
	/**
	 * List of visible markers, parallel with {@link visibleBreweries}
	 */
	private HashMap<Long, Marker> visibleMarkers = new HashMap<Long, Marker>();
	/**
	 * List of visible breweries, parallel with {@link visibleMarkers}
	 */
	private HashMap<Long, Brewery> visibleBreweries = new HashMap<Long, Brewery>();
	/**
	 * View of the map in the Layout
	 */
	private View mMapView;

	// HIGH: Integrate Trip Planner
	// MEDIUM: A new constructor that zooms out until at least N breweries are
	// displayed

	/**
	 * @return A new instance of the Fragment
	 */
	public static BeerMeMapFragment getInstance() {
		final BeerMeMapFragment frag = new BeerMeMapFragment();
		return frag;
	}

	/**
	 * @return A new instance of the Fragment
	 */
	public static BeerMeMapFragment getInstance(double latitude,
			double longitude) {
		final BeerMeMapFragment frag = new BeerMeMapFragment();
		Bundle args = new Bundle();
		args.putDouble(SAVE_LAT_KEY, latitude);
		args.putDouble(SAVE_LNG_KEY, longitude);
		frag.setArguments(args);
		return frag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mMapView != null) {
			final ViewGroup parent = (ViewGroup) mMapView.getParent();
			if (parent != null) {
				parent.removeView(mMapView);
			}
		}

		try {
			mMapView = inflater.inflate(R.layout.map_fragment, container, false);

			TouchableWrapper mapLayout = (TouchableWrapper) mMapView.findViewById(R.id.mapLayout);
			mapLayout.setUpdateMapAfterUserInteraction(this);
		} catch (InflateException e) {
			// MapFactory is already there, ignore.
		}

		return mMapView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);

		mFragMgr = getActivity().getSupportFragmentManager();

		mMapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);

		LocationFragment locationFragment = LocationFragment.getInstance(getActivity());
		locationFragment.registerListener(this);
		FragmentTransaction trans = mFragMgr.beginTransaction();
		trans.add(locationFragment, LOCATION_FRAGMENT_TAG);
		trans.commit();

		mMap = setupMap();

		Bundle params;
		Bundle args = getArguments();

		if (savedInstanceState != null) {
			params = savedInstanceState;
			mTrackLocation = params.getBoolean(SAVE_TRACKING_KEY, true);
		} else {
			params = args;
			if (params != null) {
				mTrackLocation = false;
			}
		}

		double lat = 0;
		double lng = 0;
		float zoom = DEFAULT_ZOOM;

		if (params != null) {
			lat = params.getDouble(SAVE_LAT_KEY, 0);
			lng = params.getDouble(SAVE_LNG_KEY, 0);
			zoom = params.getFloat(SAVE_ZOOM_KEY, DEFAULT_ZOOM);
		}

		mLocation = new LatLng(lat, lng);
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, zoom));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();

		SharedPreferences prefs = Prefs.getSettings(getActivity());

		int newFilter = prefs.getInt(Prefs.KEY_STATUS_FILTER,
				BreweryStatusFilterPreference.DEFAULT_VALUE);

		if (newFilter != mStatusFilter) {
			mStatusFilter = newFilter;
			loadMarkers();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mMap != null) {
			CameraPosition cameraPosition = mMap.getCameraPosition();
			LatLng target = cameraPosition.target;
			double lat = target.latitude;
			double lng = target.longitude;
			float zoom = cameraPosition.zoom;
			outState.putDouble(SAVE_LAT_KEY, lat);
			outState.putDouble(SAVE_LNG_KEY, lng);
			outState.putFloat(SAVE_ZOOM_KEY, zoom);
			outState.putBoolean(SAVE_TRACKING_KEY, mTrackLocation);
		}

		super.onSaveInstanceState(outState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.beerme.android.utils.TouchableWrapper.UpdateMapAfterUserInteraction
	 * #onUpdateMapAfterUserInteraction()
	 */
	@Override
	public void onUpdateMapAfterUserInteraction() {
		mTrackLocation = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.mg6.android.maps.extensions.GoogleMap.OnInfoWindowClickListener#
	 * onInfoWindowClick(pl.mg6.android.maps.extensions.Marker)
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {
		if (marker.isCluster()) {
			List<Marker> markers = marker.getMarkers();
			LatLngBounds.Builder builder = LatLngBounds.builder();
			for (Marker m : markers) {
				builder.include(m.getPosition());
			}
			LatLngBounds bounds = builder.build();
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,
					getResources().getDimensionPixelSize(R.dimen.padding)));
		} else {
			long id = getMarkerId(marker);
			if (id > 0) {
				Intent intent = new Intent(getActivity(), BreweryActivity.class);
				intent.putExtra("id", id);
				startActivity(intent);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.mg6.android.maps.extensions.GoogleMap.OnCameraChangeListener#
	 * onCameraChange(com.google.android.gms.maps.model.CameraPosition)
	 */
	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		mLocation = cameraPosition.target;
		loadMarkers();
	}

	/**
	 * Go through the {@link visibleBreweries list of breweries} and place or
	 * remove Markers as appropriate.
	 */
	private void loadMarkers() {
		Brewery brewery = null;
		long id = -1;
		Cursor cursor = null;
		SQLiteDatabase db = null;

		try {
			final Iterator<Brewery> iterator = visibleBreweries.values()
					.iterator();

			// Remove markers that were filtered out
			while (iterator.hasNext()) {
				brewery = iterator.next();
				if (brewery != null) {
					if (!BreweryStatusFilterPreference.match(getActivity(),
							brewery.getStatus())) {
						id = brewery.getId();
						visibleMarkers.get(id).remove();
						visibleMarkers.remove(id);
						iterator.remove();
					}
				}
			}

			LatLngBounds mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

			db = DbOpenHelper.getInstance(getActivity()).getReadableDatabase();
			String[] columns = { "_id", "latitude", "longitude" };
			String selection = "(status & " + mStatusFilter + ") != 0";

			cursor = db.query("brewery", columns, selection, null, null, null,
					null);

			id = -1;

			while (cursor.moveToNext()) {
				id = cursor.getInt(0);
				if (mapBounds.contains(new LatLng(cursor.getDouble(1), cursor
						.getDouble(2)))) {
					// On-screen
					if (visibleMarkers.get(id) == null) {
						// Not already displayed
						brewery = new Brewery(getActivity(), id);
						visibleMarkers.put(id,
								mMap.addMarker(initMarker(brewery)));
						visibleBreweries.put(id, brewery);
					}
				} else {
					// Off-screen
					if (visibleMarkers.get(id) != null) {
						// No longer displayed
						brewery = visibleBreweries.get(id);
						visibleMarkers.get(id).remove();
						visibleMarkers.remove(id);
						visibleBreweries.remove(id);
					}
				}
			}
		} catch (SQLiteException e) {
			// LOW: SQLiteDatabaseLockedException requires API 11
			ErrLog.log(getActivity(), "BeerMeMapFragment.loadMarkers()", e,
					R.string.Database_is_busy);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}

	/**
	 * Initializes the title and snippet of the Brewery's Marker
	 * 
	 * @param brewery
	 *            The brewery in question
	 * @return {@link MarkerOptions} object suitable for
	 *         {@link GoogleMap#addMarker(MarkerOptions)}
	 */
	private MarkerOptions initMarker(Brewery brewery) {
		MarkerOptions opts = new MarkerOptions();

		opts.position(new LatLng(brewery.getLatitude(), brewery.getLongitude()));

		StringBuffer title = new StringBuffer(brewery.getName());

		int status = brewery.getStatus();
		if (!BreweryStatusFilterPreference.isOpen(status)) {
			String statusString = getResources().getStringArray(
					R.array.status_value)[BreweryStatusFilterPreference
					.getIndex(status)];
			title.append(" (" + statusString + ")");
		}

		StringBuffer snippet = new StringBuffer(64);
		if (!brewery.getAddress().equals("")) {
			snippet.append(brewery.getAddress());
		}

		if (!brewery.getHours().equals("")) {
			snippet.append("\n" + brewery.getHours());
		}

		opts.title(title.toString());
		opts.snippet(snippet.toString());

		return opts;
	}

	/**
	 * Initializes map parameters
	 */
	private GoogleMap setupMap() {
		GoogleMap map = mMapFrag.getExtendedMap();
		if (map != null) {
			LatLng latLng = (mLocation != null) ? mLocation : new LatLng(0, 0);
			map.setMyLocationEnabled(true);
			mTrackLocation = true;
			map.setOnCameraChangeListener(this);
			map.setClustering(new ClusteringSettings()
					.clusterSize(DEFAULT_CLUSTER_SIZE)
					.clusterOptionsProvider(
							new BeerMeClusterOptionsProvider(getResources()))
					.addMarkersDynamically(true));
			map.setInfoWindowAdapter(new BreweryInfoWindowAdapter(
					getActivity(), (LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE)));

			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,
					DEFAULT_ZOOM));

			map.setOnInfoWindowClickListener(this);
			map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
				@Override
				public boolean onMyLocationButtonClick() {
					mTrackLocation = true;
					return false;
				}
			});
		}

		return map;
	}

	/**
	 * @param marker
	 * @return long corresponding to the {@link Brewery} associated with this
	 *         {@link Marker}
	 */
	private long getMarkerId(Marker marker) {
		long id = -1;

		for (long key : visibleMarkers.keySet()) {
			if (visibleMarkers.get(key).equals(marker)) {
				id = key;
				break;
			}
		}

		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.beerme.android.location.LocationFragment.LocationCallbacks#
	 * onLocationReceived(android.location.Location)
	 */
	@Override
	public void onLocationReceived(Location location) {
		// If the user's location is being tracked, move the center of the map
		// to the new Location
		if (location != null) {
			if (mTrackLocation && mMap != null) {
				float zoom = mMap.getCameraPosition().zoom;
				mLocation = new LatLng(location.getLatitude(),
						location.getLongitude());
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
						location.getLatitude(), location.getLongitude()), zoom));
			}
		}
	}
}