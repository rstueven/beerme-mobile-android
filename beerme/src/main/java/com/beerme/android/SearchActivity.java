package com.beerme.android;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.beerme.android.db.DBContract;

/**
 * Created by rstueven on 7/16/17.
 * <p/>
 * Handles searches.
 */

public class SearchActivity extends BeerMeActivity {
    BreweryListFragment breweryFrag;

    interface BeerMeSearch {
        void search(String query);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final FragmentManager fm = getFragmentManager();
        breweryFrag = (BreweryListFragment) fm.findFragmentById(R.id.brewery_list_fragment);

        final Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }
    }

    private void search(final String query) {
        if (!TextUtils.isEmpty(query)) {
            breweryFrag.search(query);
        } else {
            this.finish();
        }
    }
}