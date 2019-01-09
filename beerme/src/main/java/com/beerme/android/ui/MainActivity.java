package com.beerme.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.beerme.android.R;
import com.beerme.android.location.LocationFragment;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.ui.tripplanner.TripPlannerFrag;
import com.beerme.android.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends BeerMeActivity {
    public static final String LAT_KEY = "latitude";
    public static final String LNG_KEY = "longitude";
    private LatLng latlng = null;
    private MainPagerAdapter mAdapter;
    private int mOpenGLVersion;
    private boolean mServicesAvailable;
    private SharedPreferences mPrefs;
    private String mNearbyPref = Prefs.KEY_NEARBY_DISPLAY_LIST;
    private boolean mStarting = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        if (args != null) {
            double lat = args.getDouble(LAT_KEY);
            double lng = args.getDouble(LNG_KEY);
            if (lat != 0.0 && lng != 0.0) {
                latlng = new LatLng(lat, lng);
            }
        }

        mOpenGLVersion = Utils.checkOpenGLVersion(this);
        mServicesAvailable = Utils.checkPlayServices(this);

        // Checks location permissions
        new LocationFragment();

        mPrefs = Prefs.getSettings(this);
        mNearbyPref = mPrefs.getString(Prefs.KEY_NEARBY_DISPLAY, Prefs.KEY_NEARBY_DISPLAY_LIST);

        mAdapter = new MainPagerAdapter(this, getSupportFragmentManager());
        ViewPager mViewPager = findViewById(R.id.main_pager);
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.trackActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.trackActivityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mStarting) {
            String newNearbyPref = mPrefs.getString(Prefs.KEY_NEARBY_DISPLAY, Prefs.KEY_NEARBY_DISPLAY_LIST);

            if (!mNearbyPref.equals(newNearbyPref)) {
                mNearbyPref = newNearbyPref;
                mAdapter.switchNearby();
            }
        }

        mStarting = false;
    }

    public class MainPagerAdapter extends FragmentPagerAdapter {
        // Trip Planner is not available if OpenGLVersion < 2
        private final int NUM_PAGES = (mOpenGLVersion < 2) ? 3 : 4;
        private Context mContext;
        private FragmentManager mFragmentManager;
        private Fragment mFrag0 = null;

        MainPagerAdapter(Context context, FragmentManager fragmentManager) {
            super(fragmentManager);
            this.mContext = context;
            this.mFragmentManager = fragmentManager;
        }

        private Fragment getNearbyBreweriesFragment() {
            Fragment frag;

            String mapPref = mPrefs.getString(Prefs.KEY_NEARBY_DISPLAY, Prefs.KEY_NEARBY_DISPLAY_MAP);

            BeerMeMapFragment mMapFrag;
            BreweryListFrag mBreweryListFrag;
            if (Utils.isOnline(mContext)
                    && mServicesAvailable && (mOpenGLVersion >= 2)
                    && (mapPref != null && mapPref.equals(Prefs.KEY_NEARBY_DISPLAY_MAP))) {
                if (latlng == null) {
                    mMapFrag = BeerMeMapFragment.getInstance();
                } else {
                    mMapFrag = BeerMeMapFragment.getInstance(latlng, false);
                }
                frag = mMapFrag;
            } else {
                // Editor editor = Prefs.getSettingsEditor(mContext);
                // editor.putString(Prefs.KEY_NEARBY_DISPLAY,  Prefs.KEY_NEARBY_DISPLAY_LIST);
                // Prefs.getSettingsSaver(mContext).savePreferences(editor, false);
//                mBreweryListFrag = BreweryListFrag.getInstance();
                if (latlng == null) {
                    mBreweryListFrag = BreweryListFrag.getInstance();
                } else {
                    mBreweryListFrag = BreweryListFrag.getInstance(latlng, false);
                }
                frag = mBreweryListFrag;
            }

            return frag;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (mFrag0 == null) {
                        mFrag0 = getNearbyBreweriesFragment();
                    }
                    return mFrag0;
                case 1:
                    return NewsFrag.getInstance();
                case 2:
                    return UpdatesFrag.getInstance();
                case 3:
                    return TripPlannerFrag.getInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            if ((object instanceof BeerMeMapFragment && mFrag0 instanceof BreweryListFrag)
                    || (object instanceof BreweryListFrag && mFrag0 instanceof BeerMeMapFragment)) {
                return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }

        void switchNearby() {
            mFragmentManager.beginTransaction().remove(mFrag0).commit();
            mFrag0 = getNearbyBreweriesFragment();
            notifyDataSetChanged();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.Nearby_breweries);
                case 1:
                    return getString(R.string.News);
                case 2:
                    return getString(R.string.Updates);
                case 3:
                    return getString(R.string.Trip_Planner);
                default:
                    return getString(R.string.Unknown);
            }
        }
    }
}