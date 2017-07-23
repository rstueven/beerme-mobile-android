package com.beerme.android;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by rstueven on 7/16/17.
 * <p/>
 * Handles searches.
 */

public class SearchActivity extends BeerMeActivity {
    private static final int BREWERY_TAB = 0;
    private static final int BEER_TAB = 1;
    private String mQuery;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQuery = intent.getStringExtra(SearchManager.QUERY);
        }

        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        final SearchPagerAdapter mSearchPagerAdapter = new SearchPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(mSearchPagerAdapter);
    }

    private class SearchPagerAdapter extends FragmentPagerAdapter {
        SearchPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            switch (position) {
                case BREWERY_TAB:
                    return BreweryListFragment.newInstance(mQuery);
                case BEER_TAB:
                    return BeerListFragment.newInstance(mQuery);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            switch (position) {
                case BREWERY_TAB:
                    return "Breweries";
                case BEER_TAB:
                    return "Beers";
                default:
                    return null;
            }
        }
    }
}