package com.beerme.android;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by rstueven on 7/16/17.
 * <p/>
 * Handles searches.
 */

public class SearchActivity extends BeerMeActivity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }
    }

    private void search(final String query) {
        Toast.makeText(this, query, Toast.LENGTH_LONG).show();
    }
}