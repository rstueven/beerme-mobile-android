package com.beerme.android.search;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.ui.BeerActivity;
import com.beerme.android.ui.BeerMeActivity;
import com.beerme.android.ui.BreweryActivity;
import com.beerme.android.ui.BreweryListActivity;
import com.beerme.android.ui.MapFactory;
import com.beerme.android.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

public class SearchActivity extends BeerMeActivity implements LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter mCursorAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        ListView mListView = findViewById(R.id.search_listview);
        TextView empty = findViewById(android.R.id.empty);
        mListView.setEmptyView(empty);

        mCursorAdapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.suggestions_row, null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2},
                new int[]{R.id.suggestion_value, R.id.suggestion_detail}, 0);

        mListView.setAdapter(mCursorAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                // id = _ID field.
                if (id == 0) {
                    Cursor c = mCursorAdapter.getCursor();
                    if (c.moveToPosition(position)) {
                        launchMap(c.getString(4));
                    }
                } else {
                    launch(id);
                }
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.trackActivityStart(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.trackActivityStop(this);
    }

    private void launch(long id) {
        Class<?> clazz;

        if (id > 0) {
            clazz = BreweryActivity.class;
        } else {
            clazz = BeerActivity.class;
            id = -id;
        }

        Intent intent = new Intent(this, clazz);
        intent.putExtra("id", id);
        startActivity(intent);
        finish();
    }

    private void launchMap(String latLngString) {
        String[] latLngArray = latLngString.split(",");
        LatLng latLng = new LatLng(Double.parseDouble(latLngArray[0]), Double.parseDouble(latLngArray[1]));

        Intent mapIntent = MapFactory.newIntent(this, latLng);

        String className = mapIntent.getComponent().getClassName();
        if (!className.equals("com.beerme.android.ui.BeerMeMapActivity")) {
            mapIntent = new Intent(this, BreweryListActivity.class);
            mapIntent.putExtra(BreweryListActivity.LAT_KEY, latLng.latitude);
            mapIntent.putExtra(BreweryListActivity.LNG_KEY, latLng.longitude);
        }

        startActivity(mapIntent);
        finish();
    }

    private void doSearch(String query) {
        LoaderManager mgr = getSupportLoaderManager();
        Bundle args = new Bundle();
        args.putString("query", query);
        mgr.restartLoader(0, args, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = SuggestionProvider.CONTENT_URI;
        return new CursorLoader(getBaseContext(), uri, null, null,
                new String[]{args.getString("query")}, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_VIEW:
                // id == SUGGEST_COLUMN_INTENT_DATA_ID
                Uri uri = Uri.parse(intent.getData().toString());
                long id = Long.parseLong(uri.getLastPathSegment());
                if (id == 0) {
                    // Location
                    launchMap(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
                } else {
                    // Brewery/Beer
                    launch(id);
                }
                break;
            case Intent.ACTION_SEARCH:
                // New search
                String query = intent.getStringExtra(SearchManager.QUERY);
                doSearch(query);
                break;
            default:
                Log.w(Utils.APPTAG, "SearchActivity.handleIntent(): WTF ACTION:" + action);
                break;
        }
    }
}