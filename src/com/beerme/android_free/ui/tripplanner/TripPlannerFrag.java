package com.beerme.android_free.ui.tripplanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.OnInfoWindowClickListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.Polyline;
import com.androidmapsextensions.PolylineOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.beerme.android_free.R;
import com.beerme.android_free.database.Brewery;
import com.beerme.android_free.filechooser.FileExistsFragment;
import com.beerme.android_free.filechooser.FileListFragment;
import com.beerme.android_free.location.LocationFragment;
import com.beerme.android_free.search.PlacesAutoCompleteAdapter;
import com.beerme.android_free.ui.BreweryActivity;
import com.beerme.android_free.ui.tripplanner.directions.Directions;
import com.beerme.android_free.ui.tripplanner.directions.Leg;
import com.beerme.android_free.ui.tripplanner.directions.Route;
import com.beerme.android_free.ui.tripplanner.directions.Segment;
import com.beerme.android_free.utils.ErrLog;
import com.beerme.android_free.utils.ShowToast;
import com.beerme.android_free.utils.Utils;
import com.beerme.android_free.utils.YesNoDialog;
import com.beerme.android_free.utils.YesNoDialog.YesNoListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;

public class TripPlannerFrag extends Fragment implements
		OnInfoWindowClickListener, LocationFragment.LocationCallbacks,
		MessageHandler.MessageListener, DirectionsFragment.DirectionsListener,
		BreweriesFragment.BreweriesListener,
		BreweriesDisplayer.BreweriesDisplayerListener,
		MarkerDialogFragment.MarkerDialogListener, SaveTrip.SaveTripListener,
		FileListFragment.FileListListener,
		FileExistsFragment.FileExistsListener, YesNoListener {
	protected static final int CONTEXT_MENU_ID = R.menu.tripplanner;
	private static final String TAG_MAP = "map";
	public static final String DIRECTIONS_TAG = "directions";
	public static final String CANDIDATES_TAG = "candidates";
	public static final String LOCATION_FRAGMENT_TAG = "location";
	public static final String DIRECTIONS_FILE_TAG = "directionsFile";
	private static final String SAVE_TRIP_KEY = "saveTrip";
	private static final String LOAD_TRIP_KEY = "loadTrip";
	private static final String FILE_SUFFIX = "trip";
	private static final String FILE_EXISTS_KEY = "fileExists";
	private static final int MAX_STOPS = 8; // Google limit
	private static final int REMOVE_FROM_ROUTE = 1;
	private static final int DELETE_MARKER = 2;
	private AutoCompleteTextView mStart;
	private AutoCompleteTextView mEnd;
	private EditText mDist;
	private Button mGo;
	private ProgressBar mProgressBar;
	private TextView mProgressText;
	private SupportMapFragment mMapFrag;
	private DirectionsFragment mDirectionsFrag;
	private BreweriesFragment mBreweriesFrag;
	private LocationFragment mLocationFrag;
	protected GoogleMap mMap;
	protected HashMap<Long, String> mStops = new HashMap<Long, String>();
	private Location mHere;
	protected Directions mDirections;
	private HashMap<Long, Brewery> mBreweries;
	private MessageHandler mHandler = new MessageHandler(this);
	private HashMap<Long, Marker> markers;
	private Polyline currentSegment = null;
	private PlacesAutoCompleteAdapter mStartAdapter;
	private PlacesAutoCompleteAdapter mEndAdapter;

	public static TripPlannerFrag getInstance() {
		TripPlannerFrag frag = new TripPlannerFrag();
		return frag;
	}

	protected void setupContextMenu(ViewGroup layoutView) {
		registerForContextMenu(layoutView);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.setRetainInstance(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceStatee) {
		super.onCreate(savedInstanceStatee);

		this.setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tripplanner_frag, container,
				false);
		ViewGroup layoutView = (ViewGroup) view
				.findViewById(R.id.tripplanner_layout);
		setupContextMenu(layoutView);

		FragmentManager fragMgr = getChildFragmentManager();
		FragmentTransaction fragTrans = fragMgr.beginTransaction();

		if (mLocationFrag == null) {
			mLocationFrag = LocationFragment.getInstance(getActivity());
			mLocationFrag.registerListener(this);
			fragTrans.add(mLocationFrag, LOCATION_FRAGMENT_TAG);
		}

		if (mMapFrag == null) {
			mMapFrag = new SupportMapFragment();
			fragTrans.add(R.id.tripplanner_map, mMapFrag, TAG_MAP);
		}

		if (mDirectionsFrag == null) {
			mDirectionsFrag = new DirectionsFragment();
			mDirectionsFrag.registerDirectionsListener(this);
			mDirectionsFrag.registerMessageListener(this);
			fragTrans.add(mDirectionsFrag, DIRECTIONS_TAG);
		}

		if (mBreweriesFrag == null) {
			mBreweriesFrag = new BreweriesFragment();
			mBreweriesFrag.registerBreweriesListener(this);
			mBreweriesFrag.registerMessageListener(this);
			fragTrans.add(mBreweriesFrag, CANDIDATES_TAG);
		}

		fragTrans.commit();

		mStart = (AutoCompleteTextView) view.findViewById(R.id.start);
		mStartAdapter = new PlacesAutoCompleteAdapter(getActivity(),
				android.R.layout.simple_list_item_1);
		mStart.setAdapter(mStartAdapter);

		mEnd = (AutoCompleteTextView) view.findViewById(R.id.end);
		mEndAdapter = new PlacesAutoCompleteAdapter(getActivity(),
				android.R.layout.simple_list_item_1);
		mEnd.setAdapter(mEndAdapter);

		mDist = (EditText) view.findViewById(R.id.dist);
		mGo = (Button) view.findViewById(R.id.go);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		mProgressText = (TextView) view.findViewById(R.id.progressText);

		mGo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onGoClick();
			}
		});

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (Utils.isOnline(getActivity())) {
			mMap = mMapFrag.getExtendedMap();
		} else {
			ErrLog.log(getActivity(), "TripPlannerFrag.onCreateView()", null,
					R.string.No_network_connection);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (Utils.isOnline(getActivity())) {
			mMap = mMapFrag.getExtendedMap();

			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				mMap.setMyLocationEnabled(true);
				mMap.setInfoWindowAdapter(new PopupAdapter(getActivity()
						.getLayoutInflater()));
				mMap.setOnInfoWindowClickListener(this);
			} else {
				Log.e(Utils.APPTAG, "null map");
			}
		} else {
			ErrLog.log(getActivity(), "TripPlannerFrag.onResume()", null,
					R.string.No_network_connection);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		interruptThreads();
	}

	private void onGoClick() {
		String startString = mStart.getText().toString();
		String endString = mEnd.getText().toString();
		String distString = mDist.getText().toString();

		if ("".equals(endString)) {
			Toast.makeText(getActivity(), R.string.Enter_destination,
					Toast.LENGTH_LONG).show();
		} else if ("".equals(distString)) {
			Toast.makeText(getActivity(), R.string.Enter_distance,
					Toast.LENGTH_LONG).show();
		} else {
			// GO!!!
			performClearMap();
			int distance = (int) Utils.unitsToMeters(getActivity(),
					Integer.parseInt(distString));

			mDirectionsFrag.fetchDirections(startString, endString, distance,
					mStops, mHere);
		}
	}

	public ProgressBar getProgressBar() {
		return this.mProgressBar;
	}

	public TextView getProgressText() {
		return this.mProgressText;
	}

	/*
	 * performClearMap() does not clear stops
	 */
	protected void performClearMap() {
		interruptThreads();
		if (mMap != null) {
			mMap.clear();
		}
		mDirections = null;
		mBreweries = null;
	}

	private void interruptThreads() {
		mDirectionsFrag.interrupt();
		mBreweriesFrag.interrupt();
	}

	@Override
	public void postEmptyMessage(int what) {
		mHandler.sendEmptyMessage(what);
	}

	@Override
	public void postMessage(int what, int arg1, int arg2) {
		Message msg = mHandler.obtainMessage(what, arg1, arg2);
		mHandler.sendMessage(msg);
	}

	@Override
	public void postException(Exception e) {
		Message msg = mHandler.obtainMessage(MessageHandler.DISPLAY_EXCEPTION,
				e);
		mHandler.sendMessage(msg);
	}

	@Override
	public void onDirectionsCalculated(Directions directions, int distance) {
		if (directions != null) {
			mDirections = directions;
			displayRoute(directions, distance);
			mBreweriesFrag.getBreweries(directions, mHere, distance);
		} else {
			ShowToast.show(getActivity(), R.string.No_directions);
		}
	}

	private void displayRoute(Directions directions, int distance) {
		if (directions != null) {
			String status = directions.getStatus();
			if (status.equals("OK")) {
				ArrayList<Route> routes = directions.getRoutes();
				if (routes.size() > 0) {
					// Success!
					mDirections = directions;
					final Route route = routes.get(0);
					Segment overview = route.getOverviewPolyline();
					getActivity().runOnUiThread(
							new RouteDisplayer(mMapFrag, route, overview,
									distance));
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ArrayList<Leg> legs = route.getLegs();
							Leg startLeg = legs.get(0);
							Leg endLeg = legs.get(legs.size() - 1);
							String startAddress = startLeg.getStartAddress();
							String endAddress = endLeg.getEndAddress();
							mStart.setAdapter(null);
							mEnd.setAdapter(null);
							mStart.setText(startAddress);
							mEnd.setText(endAddress);
							mStart.setAdapter(mStartAdapter);
							mEnd.setAdapter(mEndAdapter);
						}
					});
				} else {
					ShowToast.show(getActivity(), R.string.No_routes);
				}
			} else {
				ShowToast.show(getActivity(),
						getActivity().getString(R.string.Status) + ": <"
								+ status + ">");
			}
		} else {
			ShowToast.show(getActivity(), R.string.No_directions);
		}
	}

	@Override
	public void onBreweriesFetched(HashMap<Long, Brewery> breweries) {
		this.mBreweries = breweries;
		getActivity().runOnUiThread(
				new BreweriesDisplayer(getActivity(), mMapFrag, breweries,
						this, this));
	}

	public void onBreweriesFetched(ArrayList<Long> breweryIds) {
		HashMap<Long, Brewery> breweries = new HashMap<Long, Brewery>();

		for (long id : breweryIds) {
			breweries.put(id, new Brewery(getActivity(), id));
		}

		onBreweriesFetched(breweries);
	}

	@Override
	public void drawCurrentPolyline(final PolylineOptions options) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				currentSegment = mMap.addPolyline(options);
			}
		});
	}

	@Override
	public void removeCurrentPolyline() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (currentSegment != null) {
					currentSegment.remove();
				}
			}
		});
	}

	@Override
	public void onLocationReceived(Location location) {
		if (location != null) {
			mHere = location;
		} else {
			Log.w(Utils.APPTAG, "TripPlannerFrag.onLocationReceived(null)");
		}
	}

	@Override
	public void onBreweriesDisplayed(HashMap<Long, Marker> markers) {
		this.markers = markers;
	}

	private long getMarkerId(Marker marker) {
		long id = -1;

		for (Long key : markers.keySet()) {
			if (markers.get(key).equals(marker)) {
				id = key;
				break;
			}
		}

		return id;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		long id = getMarkerId(marker);
		if (id >= 0) {
			MarkerDialogFragment dialog = new MarkerDialogFragment();
			Bundle args = new Bundle();
			args.putLong("id", id);
			dialog.setArguments(args);
			dialog.registerListener(this);
			dialog.show(getChildFragmentManager(), "MarkerDialogFragment");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(CONTEXT_MENU_ID, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_clear_map:
			performClearMap();
			mStops = new HashMap<Long, String>();
			return true;
		case R.id.action_zoom_to_route:
			performZoomToRoute();
			return true;
		case R.id.action_directions:
			performDirections();
			return true;
		case R.id.action_save:
			performSave();
			return true;
		case R.id.action_load:
			performLoad();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void performZoomToRoute() {
		if (mDirections != null) {
			ArrayList<Route> routes = mDirections.getRoutes();
			Route route = routes.get(0);
			LatLngBounds bounds = route.getBounds();
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,
					Utils.DEFAULT_ZOOM_PADDING));
		}
	}

	protected void performDirections() {
		if (mDirections != null) {
			Bundle bundle = new Bundle();
			String dirFile = saveDirections(getActivity(), mDirections);
			bundle.putString(DIRECTIONS_FILE_TAG, dirFile);
			Intent intent = new Intent(getActivity(), DirectionsDisplay.class);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	protected void performSave() {
		if (mDirections != null) {
			SaveTrip saveDialog = SaveTrip.newInstance(mStart.getText()
					.toString(), mEnd.getText().toString());
			saveDialog.registerListener(this);
			saveDialog.show(getChildFragmentManager(), SAVE_TRIP_KEY);
		} else {
			ShowToast.show(getActivity(), R.string.No_trip);
		}
	}

	protected void performLoad() {
		FileListFragment loadDialog = FileListFragment.newInstance(
				getActivity().getFilesDir(), FILE_SUFFIX);
		loadDialog.registerListener(this);
		loadDialog.show(getChildFragmentManager(), LOAD_TRIP_KEY);
	}

	// saveDirections() and loadDirections() serve to circumvent the 512K limit
	// on Bundle data-sharing.
	public static String saveDirections(Context context, Directions directions) {
		Time now = new Time();
		long millis = now.toMillis(true);
		String filename = "directions" + millis;

		try {
			File file = new File(context.getFilesDir(), filename);
			FileOutputStream output = new FileOutputStream(file);
			output.write(directions.toString().getBytes());
			output.close();
		} catch (IOException e) {
			Log.i(Utils.APPTAG, e.getLocalizedMessage());
			filename = null;
		}

		return filename;
	}

	public static Directions loadDirections(Context context,
			String savedDirections) {
		StringBuffer dirString = new StringBuffer();
		Directions dir = null;
		byte[] buffer = new byte[1024];
		FileInputStream input = null;
		File file = null;

		try {
			file = new File(context.getFilesDir(), savedDirections);
			input = new FileInputStream(file);
			while (input.read(buffer) != -1) {
				dirString.append(new String(buffer));
			}
			input.close();
		} catch (IOException e) {
			Log.e(Utils.APPTAG, e.getLocalizedMessage());
			dir = null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					Log.e(Utils.APPTAG, e.getLocalizedMessage());
				}
			}
			if (file != null) {
				file.delete();
			}
		}

		try {
			dir = new Directions(dirString.toString());
		} catch (JSONException e) {
			Log.e(Utils.APPTAG, e.getLocalizedMessage());
			dir = null;
		}

		return dir;
	}

	@Override
	public void onBreweryDetails(long id) {
		if (id > 0) {
			Intent intent = new Intent(getActivity(), BreweryActivity.class);
			intent.putExtra("id", id);
			startActivity(intent);
		}
	}

	@Override
	public void onAddToRoute(long id) {
		if (mStops.containsKey(id)) {
			ShowToast.show(getActivity(), "Already on the route");
		} else {
			if (mStops.size() < MAX_STOPS) {
				mStops.put(id, mBreweries.get(id).getAddress());
				this.onGoClick();
			} else {
				ShowToast.show(getActivity(), R.string.Google_stops_limit);
			}
		}
	}

	@Override
	public void onRemoveFromRoute(long id) {
		if (mStops.containsKey(id)) {
			YesNoDialog dialog = YesNoDialog.getInstance(REMOVE_FROM_ROUTE,
					getString(R.string.Remove_from_route), id);
			dialog.registerListener(this);
			dialog.show(getChildFragmentManager(), "YesNoFragment");
		}
	}

	private void performRemoveFromRoute(long id) {
		mStops.remove(id);
		this.onGoClick();
	}

	@Override
	public void onDeleteMarker(long id) {
		YesNoDialog dialog = YesNoDialog.getInstance(DELETE_MARKER,
				getString(R.string.Delete_marker), id);
		dialog.registerListener(this);
		dialog.show(getChildFragmentManager(), "YesNoFragment");
	}

	@Override
	public void onUseAsOrigin(long id) {
		if (mBreweries != null) {
			String startAddress = mBreweries.get(id).getAddress();
			mStart.setAdapter(null);
			mStart.setText(startAddress);
			mStart.setAdapter(mStartAdapter);
		}
	}

	@Override
	public void onUseAsDestination(long id) {
		if (mBreweries != null) {
			String endAddress = mBreweries.get(id).getAddress();
			mEnd.setAdapter(null);
			mEnd.setText(endAddress);
			mEnd.setAdapter(mEndAdapter);
		}
	}

	@Override
	public void onZoomHere(long id) {
		if (markers == null) {
			ShowToast.show(getActivity(), R.string.No_markers);
			return;
		}

		Marker marker = markers.get(id);
		if (marker == null) {
			ShowToast.show(getActivity(), R.string.Marker_not_found);
			return;
		}

		if (mMap == null) {
			ShowToast.show(getActivity(), R.string.No_map);
			return;
		}

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(marker.getPosition()).zoom(12).build();
		mMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}

	@Override
	public void onSaveTrip(String name) {
		File file = new File(getActivity().getFilesDir(), name + "."
				+ FILE_SUFFIX);

		if (file.exists()) {
			FileExistsFragment existsDialog = FileExistsFragment
					.newInstance(file.getName());
			existsDialog.registerListener(this);
			existsDialog.show(getChildFragmentManager(), FILE_EXISTS_KEY);
		} else {
			doSaveTrip(file);
		}
	}

	private void doSaveTrip(File file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		SaveTripData data = new SaveTripData(mDirections.toString(),
				mBreweries.keySet(), mStops);

		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(data);
		} catch (IOException e) {
			postException(e);
		} finally {
			try {
				if (oos != null)
					oos.close();
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				postException(e);
			}
		}
	}

	@Override
	public void onFileExists(String name, boolean replace) {
		if (replace) {
			File file = new File(getActivity().getFilesDir(), name);
			doSaveTrip(file);
		}
	}

	@Override
	public void onFileSelected(String name) {
		File file = new File(getActivity().getFilesDir(), name + "."
				+ FILE_SUFFIX);
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		SaveTripData data = null;

		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			data = (SaveTripData) ois.readObject();
		} catch (FileNotFoundException e) {
			postException(e);
		} catch (StreamCorruptedException e) {
			postException(e);
		} catch (IOException e) {
			postException(e);
		} catch (ClassNotFoundException e) {
			postException(e);
		}

		if (data != null) {
			try {
				mMap.clear();
				Directions dir = new Directions(data.getDirections());
				mStops = data.getStops();
				displayRoute(dir, 10);
				onBreweriesFetched(data.getBreweries());
				mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(dir
						.getRoutes().get(0).getBounds(),
						Utils.DEFAULT_ZOOM_PADDING));
			} catch (JSONException e) {
				postException(e);
			}
		}
	}

	@Override
	public void onYes(int key, long data) {
		switch (key) {
		case REMOVE_FROM_ROUTE:
			performRemoveFromRoute(data);
			break;
		case DELETE_MARKER:
			Marker marker = markers.get(data);
			marker.remove();

			if (mStops.containsKey(data)) {
				performRemoveFromRoute(data);
			}

			break;
		}
	}
}