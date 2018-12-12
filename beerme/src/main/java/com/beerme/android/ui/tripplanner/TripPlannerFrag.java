package com.beerme.android.ui.tripplanner;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beerme.android.R;

public class TripPlannerFrag extends Fragment {
    public static TripPlannerFrag getInstance() {
        return new TripPlannerFrag();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tripplanner_frag, container, false);

        TextView textView = v.findViewById(R.id.directions_api_problem);

        textView.setText("Google has changed the terms of use for their Directions API; it's no longer free to use."
                + " I've had to disable the Trip Planner until I can figure out a way to make it work for everybody."
                + " I apologize for the inconvenience.");

        return v;
    }
}

//public class TripPlannerFrag extends Fragment implements OnInfoWindowClickListener, LocationFragment.LocationListener,
//        MessageHandler.MessageListener, DirectionsFragment.DirectionsListener, BreweriesFragment.BreweriesListener,
//        BreweriesDisplayer.BreweriesDisplayerListener, MarkerDialogFragment.MarkerDialogListener,
//        SaveTrip.SaveTripListener, FileListFragment.FileListListener, FileExistsFragment.FileExistsListener,
//        YesNoListener {
//    protected static final int CONTEXT_MENU_ID = R.menu.tripplanner;
//    private static final String TAG_MAP = "map";
//    public static final String DIRECTIONS_TAG = "directions";
//    public static final String CANDIDATES_TAG = "candidates";
//    public static final String LOCATION_FRAGMENT_TAG = "location";
//    public static final String DIRECTIONS_FILE_TAG = "directionsFile";
//    private static final String SAVE_TRIP_KEY = "saveTrip";
//    private static final String LOAD_TRIP_KEY = "loadTrip";
//    private static final String FILE_SUFFIX = "trip";
//    private static final String FILE_EXISTS_KEY = "fileExists";
//    private static final int MAX_STOPS = 8; // Google limit
//    private static final int REMOVE_FROM_ROUTE = 1;
//    private static final int DELETE_MARKER = 2;
//    private AutoCompleteTextView mStart;
//    private AutoCompleteTextView mEnd;
//    private EditText mDist;
//    private ProgressBar mProgressBar;
//    private TextView mProgressText;
//    private SupportMapFragment mMapFrag;
//    private DirectionsFragment mDirectionsFrag;
//    private BreweriesFragment mBreweriesFrag;
//    private LocationFragment mLocationFrag;
//    protected GoogleMap mMap;
//    protected HashMap<Long, String> mStops = new HashMap<>();
//    private Location mHere;
//    protected Directions mDirections;
//    private HashMap<Long, Brewery> mBreweries;
//    private List<Long> mDeletedBreweries = new ArrayList<Long>();
//    private MessageHandler mHandler = new MessageHandler(this);
//    private HashMap<Long, Marker> mMarkers;
//    private Polyline currentSegment = null;
//    private PlacesAutoCompleteAdapter mStartAdapter;
//    private PlacesAutoCompleteAdapter mEndAdapter;
//
//    public static TripPlannerFrag getInstance() {
//        return new TripPlannerFrag();
//    }
//
//    protected void setupContextMenu(ViewGroup layoutView) {
//        registerForContextMenu(layoutView);
//    }
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//
//        this.setRetainInstance(true);
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceStatee) {
//        super.onCreate(savedInstanceStatee);
//
//        this.setHasOptionsMenu(true);
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.tripplanner_frag, container, false);
//        ViewGroup layoutView = view.findViewById(R.id.tripplanner_layout);
//        setupContextMenu(layoutView);
//
//        FragmentManager fragMgr = getChildFragmentManager();
//        FragmentTransaction fragTrans = fragMgr.beginTransaction();
//
//        if (mLocationFrag == null) {
//            mLocationFrag = LocationFragment.getInstance();
//            mLocationFrag.registerLocationListener(this);
//            fragTrans.add(mLocationFrag, LOCATION_FRAGMENT_TAG);
//        }
//
//        if (mMapFrag == null) {
//            mMapFrag = new SupportMapFragment();
//            fragTrans.add(R.id.tripplanner_map, mMapFrag, TAG_MAP);
//        }
//
//        if (mDirectionsFrag == null) {
//            mDirectionsFrag = new DirectionsFragment();
//            mDirectionsFrag.registerDirectionsListener(this);
//            mDirectionsFrag.registerMessageListener(this);
//            fragTrans.add(mDirectionsFrag, DIRECTIONS_TAG);
//        }
//
//        if (mBreweriesFrag == null) {
//            mBreweriesFrag = new BreweriesFragment();
//            mBreweriesFrag.registerBreweriesListener(this);
//            mBreweriesFrag.registerMessageListener(this);
//            fragTrans.add(mBreweriesFrag, CANDIDATES_TAG);
//        }
//
//        fragTrans.commit();
//
//        mStart = view.findViewById(R.id.start);
//        mStartAdapter = new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
//        mStart.setAdapter(mStartAdapter);
//
//        mEnd = view.findViewById(R.id.end);
//        mEndAdapter = new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
//        mEnd.setAdapter(mEndAdapter);
//
//        mDist = view.findViewById(R.id.dist);
//        Button mGo = view.findViewById(R.id.go);
//        mProgressBar = view.findViewById(R.id.progressBar);
//        mProgressText = view.findViewById(R.id.progressText);
//
//        mGo.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onGoClick();
//            }
//        });
//
//        return view;
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        if (Utils.isOnline(getActivity())) {
//            mMapFrag.getExtendedMapAsync(new OnMapReadyCallback() {
//                @Override
//                public void onMapReady(GoogleMap googleMap) {
//                    mMap = googleMap;
//                }
//            });
//        } else {
//            ErrLog.log(getActivity(), "TripPlannerFrag.onCreateView()", null, R.string.No_network_connection);
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        if (Utils.isOnline(getActivity())) {
//            final TripPlannerFrag self = this;
//            mMapFrag.getExtendedMapAsync(new OnMapReadyCallback() {
//                @Override
//                public void onMapReady(GoogleMap googleMap) {
//                    mMap = googleMap;
//                    // Check if we were successful in obtaining the map.
//                    if (mMap != null) {
//                        mMap.setMyLocationEnabled(true);
//                        mMap.setInfoWindowAdapter(new PopupAdapter(getActivity().getLayoutInflater()));
//                        mMap.setOnInfoWindowClickListener(self);
//                    } else {
//                        Log.e(Utils.APPTAG, "null map");
//                    }
//                }
//            });
//        } else {
//            ErrLog.log(getActivity(), "TripPlannerFrag.onResume()", null, R.string.No_network_connection);
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        interruptThreads();
//    }
//
//    private void onGoClick() {
//        String startString = mStart.getText().toString();
//        String endString = mEnd.getText().toString();
//        String distString = mDist.getText().toString();
//
//        if ("".equals(endString)) {
//            Toast.makeText(getActivity(), R.string.Enter_destination, Toast.LENGTH_LONG).show();
//        } else if ("".equals(distString)) {
//            Toast.makeText(getActivity(), R.string.Enter_distance, Toast.LENGTH_LONG).show();
//        } else {
//            // GO!!!
//            performClearMap();
//            int distance = (int) Utils.unitsToMeters(getActivity(), Integer.parseInt(distString));
//
//            mDirectionsFrag.fetchDirections(startString, endString, distance, mStops, mHere);
//        }
//    }
//
//    public ProgressBar getProgressBar() {
//        return this.mProgressBar;
//    }
//
//    public TextView getProgressText() {
//        return this.mProgressText;
//    }
//
//    /*
//     * performClearMap() does not clear stops
//     */
//    protected void performClearMap() {
//        interruptThreads();
//        if (mMap != null) {
//            mMap.clear();
//        }
//        mDirections = null;
//        mBreweries = null;
//    }
//
//    private void interruptThreads() {
//        mDirectionsFrag.interrupt();
//        mBreweriesFrag.interrupt();
//    }
//
//    @Override
//    public void postEmptyMessage(int what) {
//        mHandler.sendEmptyMessage(what);
//    }
//
//    @Override
//    public void postMessage(int what, int arg1, int arg2) {
//        Message msg = mHandler.obtainMessage(what, arg1, arg2);
//        mHandler.sendMessage(msg);
//    }
//
//    @Override
//    public void postException(Exception e) {
//        Message msg = mHandler.obtainMessage(MessageHandler.DISPLAY_EXCEPTION, e);
//        mHandler.sendMessage(msg);
//    }
//
//    @Override
//    public void onDirectionsCalculated(Directions directions, int distance) {
//        if (directions != null) {
//            mDirections = directions;
//            displayRoute(directions, distance);
//            mBreweriesFrag.getBreweries(directions, mHere, distance);
//        } else {
//            ShowToast.show(getActivity(), R.string.No_directions);
//        }
//    }
//
//    private void displayRoute(Directions directions, int distance) {
//        Activity activity = getActivity();
//
//        if (directions != null) {
//            String status = directions.getStatus();
//            if (status.equals("OK")) {
//                ArrayList<Route> routes = directions.getRoutes();
//                if (routes.size() > 0) {
//                    // Success!
//                    if (activity != null) {
//                        mDirections = directions;
//                        final Route route = routes.get(0);
//                        Segment overview = route.getOverviewPolyline();
//                        activity.runOnUiThread(new RouteDisplayer(mMapFrag, route, overview, distance));
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ArrayList<Leg> legs = route.getLegs();
//                                Leg startLeg = legs.get(0);
//                                Leg endLeg = legs.get(legs.size() - 1);
//                                String startAddress = startLeg.getStartAddress();
//                                String endAddress = endLeg.getEndAddress();
//                                mStart.setAdapter(null);
//                                mEnd.setAdapter(null);
//                                mStart.setText(startAddress);
//                                mEnd.setText(endAddress);
//                                mStart.setAdapter(mStartAdapter);
//                                mEnd.setAdapter(mEndAdapter);
//                            }
//                        });
//                    }
//                } else {
//                    ShowToast.show(activity, R.string.No_routes);
//                }
//            } else {
//                if (activity != null) {
//                    ShowToast.show(activity, activity.getString(R.string.Status) + ": <" + status + ">");
//                }
//            }
//        } else {
//            ShowToast.show(activity, R.string.No_directions);
//        }
//    }
//
//    @Override
//    public void onBreweriesFetched(HashMap<Long, Brewery> breweries) {
//        Activity activity = getActivity();
//        if (activity != null) {
//            this.mBreweries = breweries;
//            activity.runOnUiThread(new BreweriesDisplayer(getActivity(), mMapFrag, breweries, mDeletedBreweries, this, this));
//        }
//    }
//
//    public void onBreweriesFetched(ArrayList<Long> breweryIds) {
//        HashMap<Long, Brewery> breweries = new HashMap<>();
//
//        for (long id : breweryIds) {
//            breweries.put(id, new Brewery(getActivity(), id));
//        }
//
//        onBreweriesFetched(breweries);
//    }
//
//    @Override
//    public void drawCurrentPolyline(final PolylineOptions options) {
//        Activity activity = getActivity();
//        if (activity != null) {
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    currentSegment = mMap.addPolyline(options);
//                }
//            });
//        }
//    }
//
//    @Override
//    public void removeCurrentPolyline() {
//        Activity activity = getActivity();
//        if (activity != null) {
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (currentSegment != null) {
//                        currentSegment.remove();
//                    }
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onLocationUpdated(Location location) {
//        if (location != null) {
//            mHere = location;
//        } else {
//            Log.w(Utils.APPTAG, "TripPlannerFrag.onLocationUpdated(null)");
//        }
//    }
//
//    @Override
//    public void onBreweriesDisplayed(HashMap<Long, Marker> markers) {
//        this.mMarkers = markers;
//    }
//
//    private long getMarkerId(Marker marker) {
//        long id = -1;
//
//        for (Long key : mMarkers.keySet()) {
//            if (mMarkers.get(key).equals(marker)) {
//                id = key;
//                break;
//            }
//        }
//
//        return id;
//    }
//
//    @Override
//    public void onInfoWindowClick(Marker marker) {
//        long id = getMarkerId(marker);
//        if (id >= 0) {
//            MarkerDialogFragment dialog = new MarkerDialogFragment();
//            Bundle args = new Bundle();
//            args.putLong("id", id);
//            dialog.setArguments(args);
//            dialog.registerListener(this);
//            dialog.show(getChildFragmentManager(), "MarkerDialogFragment");
//        }
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(CONTEXT_MENU_ID, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_clear_map:
//                performClearMap();
//                mStops = new HashMap<>();
//                return true;
//            case R.id.action_zoom_to_route:
//                performZoomToRoute();
//                return true;
//            case R.id.action_directions:
//                performDirections();
//                return true;
//            case R.id.action_save:
//                performSave();
//                return true;
//            case R.id.action_load:
//                performLoad();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    protected void performZoomToRoute() {
//        if (mDirections != null) {
//            ArrayList<Route> routes = mDirections.getRoutes();
//            Route route = routes.get(0);
//            LatLngBounds bounds = route.getBounds();
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Utils.DEFAULT_ZOOM_PADDING));
//        }
//    }
//
//    protected void performDirections() {
//        Activity activity = getActivity();
//        if (activity != null) {
//            if (mDirections != null) {
//                Bundle bundle = new Bundle();
//                String dirFile = saveDirections(activity, mDirections);
//                bundle.putString(DIRECTIONS_FILE_TAG, dirFile);
//                Intent intent = new Intent(activity, DirectionsDisplay.class);
//                intent.putExtras(bundle);
//                startActivity(intent);
//            }
//        }
//    }
//
//    protected void performSave() {
//        if (mDirections != null) {
//            SaveTrip saveDialog = SaveTrip.newInstance(mStart.getText().toString(), mEnd.getText().toString());
//            saveDialog.registerListener(this);
//            saveDialog.show(getChildFragmentManager(), SAVE_TRIP_KEY);
//        } else {
//            ShowToast.show(getActivity(), R.string.No_trip);
//        }
//    }
//
//    protected void performLoad() {
//        Activity activity = getActivity();
//        if (activity != null) {
//            FileListFragment loadDialog = FileListFragment.newInstance(activity.getFilesDir(), FILE_SUFFIX);
//            loadDialog.registerListener(this);
//            loadDialog.show(getChildFragmentManager(), LOAD_TRIP_KEY);
//        }
//    }
//
//    // saveDirections() and loadDirections() serve to circumvent the 512K limit
//    // on Bundle data-sharing.
//    public static String saveDirections(Context context, Directions directions) {
//        Time now = new Time();
//        long millis = now.toMillis(true);
//        String filename = "directions" + millis;
//
//        try {
//            File file = new File(context.getFilesDir(), filename);
//            FileOutputStream output = new FileOutputStream(file);
//            output.write(directions.toString().getBytes());
//            output.close();
//        } catch (IOException e) {
//            Log.i(Utils.APPTAG, e.getLocalizedMessage());
//            filename = null;
//        }
//
//        return filename;
//    }
//
//    public static Directions loadDirections(Context context, String savedDirections) {
//        StringBuilder dirString = new StringBuilder();
//        Directions dir = null;
//        byte[] buffer = new byte[1024];
//        FileInputStream input = null;
//        File file = null;
//
//        try {
//            file = new File(context.getFilesDir(), savedDirections);
//            input = new FileInputStream(file);
//            while (input.read(buffer) != -1) {
//                dirString.append(new String(buffer));
//            }
//            input.close();
//        } catch (IOException e) {
//            Log.e(Utils.APPTAG, e.getLocalizedMessage());
//        } finally {
//            if (input != null) {
//                try {
//                    input.close();
//                } catch (IOException e) {
//                    Log.e(Utils.APPTAG, e.getLocalizedMessage());
//                }
//            }
//            if (file != null) {
//                if (!file.delete()) {
//                    Log.e(Utils.APPTAG, "TripPlannerFrag.loadDirections(): file delete failed");
//                }
//            }
//        }
//
//        try {
//            dir = new Directions(dirString.toString());
//        } catch (JSONException e) {
//            Log.e(Utils.APPTAG, e.getLocalizedMessage());
//        }
//
//        return dir;
//    }
//
//    @Override
//    public void onBreweryDetails(long id) {
//        if (id > 0) {
//            Intent intent = new Intent(getActivity(), BreweryActivity.class);
//            intent.putExtra("id", id);
//            startActivity(intent);
//        }
//    }
//
//    @Override
//    public void onAddToRoute(long id) {
//        Activity activity = getActivity();
//        if (activity != null) {
//            if (mStops.containsKey(id)) {
//                ShowToast.show(activity, "Already on the route");
//            } else {
//                if (mStops.size() < MAX_STOPS) {
//                    mStops.put(id, mBreweries.get(id).getAddress());
//                    this.onGoClick();
//                } else {
//                    ShowToast.show(activity, R.string.Google_stops_limit);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onRemoveFromRoute(long id) {
//        if (mStops.containsKey(id)) {
//            YesNoDialog dialog = YesNoDialog.getInstance(REMOVE_FROM_ROUTE, getString(R.string.Remove_from_route), id);
//            dialog.registerListener(this);
//            dialog.show(getChildFragmentManager(), "YesNoFragment");
//        }
//    }
//
//    private void performRemoveFromRoute(long id) {
//        mStops.remove(id);
//        this.onGoClick();
//    }
//
//    @Override
//    public void onDeleteMarker(long id) {
//        YesNoDialog dialog = YesNoDialog.getInstance(DELETE_MARKER, getString(R.string.Delete_marker), id);
//        dialog.registerListener(this);
//        dialog.show(getChildFragmentManager(), "YesNoFragment");
//    }
//
//    @Override
//    public void onUseAsOrigin(long id) {
//        if (mBreweries != null) {
//            String startAddress = mBreweries.get(id).getAddress();
//            mStart.setAdapter(null);
//            mStart.setText(startAddress);
//            mStart.setAdapter(mStartAdapter);
//        }
//    }
//
//    @Override
//    public void onUseAsDestination(long id) {
//        if (mBreweries != null) {
//            String endAddress = mBreweries.get(id).getAddress();
//            mEnd.setAdapter(null);
//            mEnd.setText(endAddress);
//            mEnd.setAdapter(mEndAdapter);
//        }
//    }
//
//    @Override
//    public void onZoomHere(long id) {
//        if (mMarkers == null) {
//            ShowToast.show(getActivity(), R.string.No_markers);
//            return;
//        }
//
//        Marker marker = mMarkers.get(id);
//        if (marker == null) {
//            ShowToast.show(getActivity(), R.string.Marker_not_found);
//            return;
//        }
//
//        if (mMap == null) {
//            ShowToast.show(getActivity(), R.string.No_map);
//            return;
//        }
//
//        CameraPosition cameraPosition = new CameraPosition.Builder().target(marker.getPosition()).zoom(12).build();
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//    }
//
//    @Override
//    public void onSaveTrip(String name) {
//        Activity activity = getActivity();
//        if (activity != null) {
//            File file = new File(activity.getFilesDir(), name + "." + FILE_SUFFIX);
//
//            if (file.exists()) {
//                FileExistsFragment existsDialog = FileExistsFragment.newInstance(file.getName());
//                existsDialog.registerListener(this);
//                existsDialog.show(getChildFragmentManager(), FILE_EXISTS_KEY);
//            } else {
//                doSaveTrip(file);
//            }
//        }
//    }
//
//    private void doSaveTrip(File file) {
//        SaveTripData data = new SaveTripData(mDirections.toString(), mBreweries.keySet(), mStops,
//                Integer.parseInt(mDist.getText().toString()));
//        FileOutputStream fos = null;
//        ObjectOutputStream oos = null;
//
//        try {
//            fos = new FileOutputStream(file);
//            oos = new ObjectOutputStream(fos);
//            oos.writeObject(data);
//        } catch (IOException e) {
//            postException(e);
//        } finally {
//            try {
//                if (oos != null)
//                    oos.close();
//                if (fos != null)
//                    fos.close();
//            } catch (IOException e) {
//                postException(e);
//            }
//        }
//    }
//
//    @Override
//    public void onFileExists(String name, boolean replace) {
//        if (replace) {
//            Activity activity = getActivity();
//            if (activity != null) {
//                File file = new File(activity.getFilesDir(), name);
//                doSaveTrip(file);
//            }
//        }
//    }
//
//    @Override
//    public void onFileSelected(String name) {
//        Activity activity = getActivity();
//        if (activity != null) {
//            File file = new File(activity.getFilesDir(), name + "." + FILE_SUFFIX);
//            FileInputStream fis;
//            ObjectInputStream ois;
//            SaveTripData data = null;
//
//            try {
//                fis = new FileInputStream(file);
//                ois = new ObjectInputStream(fis);
//                data = (SaveTripData) ois.readObject();
//            } catch (FileNotFoundException e) {
//                postException(e);
//            } catch (StreamCorruptedException e) {
//                postException(e);
//            } catch (IOException e) {
//                postException(e);
//            } catch (ClassNotFoundException e) {
//                postException(e);
//            }
//
//            if (data != null) {
//                try {
//                    mMap.clear();
//                    Directions dir = new Directions(data.getDirections());
//                    mStops = data.getStops();
//                    int dist = data.getDist();
//                    displayRoute(dir, 0);
//                    mDist.setText(String.format(Locale.getDefault(), "%d", dist));
//                    onBreweriesFetched(data.getBreweries());
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(dir.getRoutes().get(0).getBounds(), Utils.DEFAULT_ZOOM_PADDING));
//                } catch (JSONException e) {
//                    postException(e);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onYes(int key, long data) {
//        switch (key) {
//            case REMOVE_FROM_ROUTE:
//                performRemoveFromRoute(data);
//                break;
//            case DELETE_MARKER:
//                Marker marker = mMarkers.get(data);
//                marker.remove();
//                mMarkers.remove(data);
//
//                mBreweries.remove(data);
//                mDeletedBreweries.add(data);
//
//                if (mStops.containsKey(data)) {
//                    performRemoveFromRoute(data);
//                }
//
//                break;
//        }
//    }
//}