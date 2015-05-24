package com.beerme.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.beerme.android.R;
import com.beerme.android.prefs.Prefs;
import com.beerme.android.ui.actionbar.BeerMeActionBarActivity;
import com.beerme.android.ui.tripplanner.TripPlannerFrag;
import com.beerme.android.utils.Utils;

public class MainActivity extends BeerMeActionBarActivity {
	private ViewPager mViewPager;
	private MainPagerAdapter mAdapter;
	private int mOpenGLVersion;
	private boolean mServicesAvailable;
	private BeerMeMapFragment mMapFrag = null;
	private BreweryListFrag mBreweryListFrag = null;
	private NewsFrag mNewsFrag = null;
	private UpdatesFrag mUpdatesFrag = null;
	private TripPlannerFrag mTripPlannerFrag = null;
	private Location mHere = null;
	private SharedPreferences mPrefs;
	private String mNearbyPref = Prefs.KEY_NEARBY_DISPLAY_LIST;
	private boolean mStarting = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		mOpenGLVersion = Utils.checkOpenGLVersion(this);
		mServicesAvailable = Utils.checkPlayServices(this);

		mPrefs = Prefs.getSettings(this);
		mNearbyPref = mPrefs.getString(Prefs.KEY_NEARBY_DISPLAY,
				Prefs.KEY_NEARBY_DISPLAY_LIST);

		mAdapter = new MainPagerAdapter(this, getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.main_pager);
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
			String newNearbyPref = mPrefs.getString(Prefs.KEY_NEARBY_DISPLAY,
					Prefs.KEY_NEARBY_DISPLAY_LIST);

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
		private Context mContext = null;
		private FragmentManager mFragmentManager = null;
		private Fragment mFrag0 = null;

		public MainPagerAdapter(Context context, FragmentManager fragmentManager) {
			super(fragmentManager);
			this.mContext = context;
			this.mFragmentManager = fragmentManager;
		}

		private Fragment getNearbyBreweriesFragment() {
			Fragment frag = null;

			String mapPref = mPrefs.getString(Prefs.KEY_NEARBY_DISPLAY,
					Prefs.KEY_NEARBY_DISPLAY_MAP);

			if (mServicesAvailable && (mOpenGLVersion >= 2)
					&& (mapPref.equals(Prefs.KEY_NEARBY_DISPLAY_MAP))) {
				mMapFrag = BeerMeMapFragment.getInstance();
				mBreweryListFrag = null;
				frag = mMapFrag;
			} else {
				Editor editor = Prefs.getSettingsEditor(mContext);
				editor.putString(Prefs.KEY_NEARBY_DISPLAY,
						Prefs.KEY_NEARBY_DISPLAY_LIST);
				Prefs.getSettingsSaver(mContext).savePreferences(editor, false);
				mBreweryListFrag = BreweryListFrag.getInstance();
				mMapFrag = null;
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
				mNewsFrag = NewsFrag.getInstance();
				return mNewsFrag;
			case 2:
				mUpdatesFrag = UpdatesFrag.getInstance();
				return mUpdatesFrag;
			case 3:
				mTripPlannerFrag = TripPlannerFrag.getInstance();
				return mTripPlannerFrag;
			}

			return null;
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}

		@Override
		public int getItemPosition(Object object) {
			if ((object instanceof BeerMeMapFragment && mFrag0 instanceof BreweryListFrag)
					|| (object instanceof BreweryListFrag && mFrag0 instanceof BeerMeMapFragment)) {
				return POSITION_NONE;
			}
			return POSITION_UNCHANGED;
		}

		public void switchNearby() {
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

	public Location getHere() {
		return mHere;
	}
}