package com.beerme.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beerme.android.R;
import com.beerme.android.database.Brewery;
import com.beerme.android.database.BreweryList;
import com.beerme.android.location.LocationFragment;
import com.beerme.android.prefs.BreweryStatusFilterPreference;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

public class BreweryListFrag extends Fragment implements LocationFragment.LocationListener {
    /**
     * Tag for the LoadBreweryList Thread
     */
    private static final String THREAD_TAG = "LoadBreweryList";
    /**
     * Tag for the LocationFragment, which keeps track of the user's location
     */
    private static final String LOCATION_FRAGMENT_TAG = "locationFragment";
    /**
     * savedInstanceState key for the list's latitude
     */
    private static final String SAVE_LAT_KEY = "latitude";
    /**
     * savedInstanceState key for the list's longitude
     */
    private static final String SAVE_LNG_KEY = "longitude";
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
     * The list itself
     */
    private BreweryList mList = null;
    /**
     * Current center point of the map
     */
    private LatLng mLocation = null;
    /**
     * View of the list in the Layout
     */
    private ListView mListView = null;
    private ProgressBar mProgress = null;
    private static BreweryListHandler mHandler = null;
    private int mDistUnit = -1;
    private SharedPreferences mPrefs;
    private boolean mOKtoLoad = false;

    // LOW: AND0035: RFE: Brewery list sort options

    /**
     * @return A new instance of the Fragment
     */

    public static BreweryListFrag getInstance() {
        return new BreweryListFrag();
    }

    public static BreweryListFrag getInstance(LatLng latLng) {
        BreweryListFrag frag = new BreweryListFrag();
        Bundle args = new Bundle();
        args.putDouble(SAVE_LAT_KEY, latLng.latitude);
        args.putDouble(SAVE_LNG_KEY, latLng.longitude);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            double lat = args.getDouble(SAVE_LAT_KEY, Double.MAX_VALUE);
            double lng = args.getDouble(SAVE_LNG_KEY, Double.MAX_VALUE);
            mLocation = new LatLng(lat, lng);
        }

        mPrefs = Prefs.getSettings(getActivity());

        LocationFragment mLocationFrag = LocationFragment.getInstance();
        mLocationFrag.registerLocationListener(this);
        getChildFragmentManager().beginTransaction().add(mLocationFrag, LOCATION_FRAGMENT_TAG).commit();

        mHandler = new BreweryListHandler(this);

        if (savedInstanceState != null) {
            double lat = savedInstanceState.getDouble(SAVE_LAT_KEY);
            double lng = savedInstanceState.getDouble(SAVE_LNG_KEY);
            mLocation = new LatLng(lat, lng);
            mTrackLocation = savedInstanceState.getBoolean(SAVE_TRACKING_KEY);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Utils.trackFragment(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.brewerylist_frag, container, false);

        mListView = view.findViewById(R.id.brewerylist);
        mListView.setOnItemClickListener(brewerylistItemClickListener);
        mProgress = view.findViewById(R.id.brewerylist_progress);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mOKtoLoad = true;

        int newDistUnit = Integer.parseInt(mPrefs.getString(Prefs.KEY_DIST_UNIT, "0"));
        int newFilter = mPrefs.getInt(Prefs.KEY_STATUS_FILTER, BreweryStatusFilterPreference.DEFAULT_VALUE);

        if (newDistUnit != mDistUnit || newFilter != mStatusFilter) {
            mDistUnit = newDistUnit;
            mStatusFilter = newFilter;
            if (mLocation != null) {
                new Thread(new LoadBreweryList(mLocation, mStatusFilter), THREAD_TAG).start();
            }
        } else {
            refresh();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mLocation != null) {
            outState.putDouble(SAVE_LAT_KEY, mLocation.latitude);
            outState.putDouble(SAVE_LNG_KEY, mLocation.longitude);
        }

        outState.putBoolean(SAVE_TRACKING_KEY, mTrackLocation);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        mOKtoLoad = false;
        super.onPause();
    }

    private void refresh() {
        FragmentActivity activity = getActivity();

        if (activity != null) {
            if (mList != null) {
                mListView.setAdapter(new BreweryListAdapter(getActivity(), R.id.brewerylist, mList));
            }

            mProgress.setVisibility(View.GONE);
            mOKtoLoad = true;
        }
    }

    public class BreweryListAdapter extends ArrayAdapter<Brewery> {
        private Context mContext;

        BreweryListAdapter(Context context, int resource, List<Brewery> objects) {
            super(context, 0, objects);
            this.mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.brewerylist_row, parent, false);
            }

            Brewery brewery = getItem(position);

            if (brewery != null) {
                TextView nameView = view.findViewById(R.id.brewerylist_name);
                nameView.setText(brewery.getName());

                TextView addressView = view.findViewById(R.id.brewerylist_address);
                addressView.setText(brewery.getAddress());

                TextView phoneView = view.findViewById(R.id.brewerylist_phone);
                String phone = brewery.getPhone();
                if (phone.equals("")) {
                    phoneView.setVisibility(View.GONE);
                } else {
                    phoneView.setText(brewery.getPhone());
                    phoneView.setVisibility(View.VISIBLE);
                }

                TextView distanceView = view.findViewById(R.id.brewerylist_distance);
                float distanceResults[] = new float[2];
                Location.distanceBetween(mLocation.latitude, mLocation.longitude,
                        brewery.getLatitude(), brewery.getLongitude(), distanceResults);
                float distance = distanceResults[0];
                String bearing = getString(Utils.bearingToCompass(distanceResults[1]));
                distanceView.setText(String.format(Locale.getDefault(), "%s %s", Utils.metersToUnits(getActivity(), distance), bearing));

                LinearLayout servicesView = view.findViewById(R.id.brewery_services);
                brewery.displayServiceIcons(getActivity(), servicesView);
            }

            return view;
        }
    }

    public OnItemClickListener brewerylistItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (mList != null) {
                Brewery brewery = mList.get(position);

                if (brewery != null) {
                    Intent intent = new Intent(getActivity(),
                            BreweryActivity.class);
                    intent.putExtra("id", brewery.getId());
                    startActivity(intent);
                }
            }
        }
    };

    private class LoadBreweryList implements Runnable {
        private Location mLoadLocation;
        private int mLoadFilter;

        LoadBreweryList(LatLng location, int filter) {
            this.mLoadLocation = new Location("");
            this.mLoadLocation.setLatitude(location.latitude);
            this.mLoadLocation.setLongitude(location.longitude);
            this.mLoadFilter = filter;
        }

        @Override
        public void run() {
            if (mOKtoLoad && mLoadLocation != null) {
                mOKtoLoad = false;
                mHandler.sendEmptyMessage(LOADING);
                mList = new BreweryList(getActivity(), mLoadFilter,
                        mLoadLocation);
                mHandler.sendEmptyMessage(LOADED);
            }
        }
    }

    private final static int LOADING = 1;
    private final static int LOADED = 2;

    private final static class BreweryListHandler extends Handler {
        private WeakReference<BreweryListFrag> mReference;

        BreweryListHandler(BreweryListFrag fragment) {
            mReference = new WeakReference<BreweryListFrag>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            BreweryListFrag frag = mReference.get();

            switch (msg.what) {
                case LOADING:
                    frag.mProgress.setVisibility(View.VISIBLE);
                    break;
                case LOADED:
                    frag.refresh();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        if (location != null) {
            if (mTrackLocation) {
                mLocation = new LatLng(location.getLatitude(), location.getLongitude());
                // mLocationFrag.setTimeout(60000);

                if (mOKtoLoad) {
                    new Thread(new LoadBreweryList(mLocation, mStatusFilter), THREAD_TAG).start();
                }
                // Only update the location once.
                mTrackLocation = false;
            }
        } else {
            Toast.makeText(getActivity(), R.string.Waiting_for_location, Toast.LENGTH_LONG).show();
        }
    }
}