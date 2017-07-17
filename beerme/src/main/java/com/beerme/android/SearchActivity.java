package com.beerme.android;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.beerme.android.db.DBContract;

/**
 * Created by rstueven on 7/16/17.
 * <p/>
 * Handles searches.
 */

public class SearchActivity extends BeerMeActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int BREWERY_LOADER = 0;
    private final int BEER_LOADER = 1;
    private final int LOCATION_LOADER = 2;

    private SearchAdapter breweryAdapter;

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
        if (!TextUtils.isEmpty(query)) {
            searchBreweries(query);
//            searchBeers(query);
//            searchLocations(query);
        } else {
            this.finish();
        }
    }

    private void searchBreweries(final String query) {
        final String[] projection = new String[]{DBContract.Brewery.COLUMN_ID, DBContract.Brewery.COLUMN_NAME, DBContract.Brewery.COLUMN_ADDRESS};
        final Bundle breweryArgs = new Bundle();
        breweryArgs.putStringArray("selection_args", new String[]{"%" + query + "%"});
        breweryArgs.putStringArray("projection", projection);
        final ListView listView = (ListView) findViewById(R.id.brewery_list);
        breweryAdapter = new SearchAdapter(this, null);
        listView.setAdapter(breweryAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final Intent intent = new Intent(SearchActivity.this, BreweryActivity.class);
                intent.putExtra("id", (int) id);
                startActivity(intent);            }
        });
        getLoaderManager().initLoader(BREWERY_LOADER, breweryArgs, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final String SELECTION = "name LIKE ?";
        final String ORDER = "name ASC";

        switch (id) {
            case BREWERY_LOADER:
                return new CursorLoader(this,
                        DBContract.Brewery.CONTENT_URI,
                        args.getStringArray("projection"),
                        SELECTION,
                        args.getStringArray("selection_args"),
                        ORDER);
            case BEER_LOADER:
                break;
            case LOCATION_LOADER:
                break;
            default:
                throw new IllegalArgumentException("Unknown id: " + id);
        }

        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        final int id = loader.getId();

        switch (id) {
            case BREWERY_LOADER:
                breweryAdapter.swapCursor(data);
                break;
            case BEER_LOADER:
                break;
            case LOCATION_LOADER:
                break;
            default:
                throw new IllegalArgumentException("Unknown id: " + id);
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        final int id = loader.getId();

        switch (id) {
            case BREWERY_LOADER:
                breweryAdapter.swapCursor(null);
                break;
            case BEER_LOADER:
                break;
            case LOCATION_LOADER:
                break;
            default:
                throw new IllegalArgumentException("Unknown id: " + id);
        }
    }
}