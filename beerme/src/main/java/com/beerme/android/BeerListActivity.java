package com.beerme.android;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.beerme.android.db.DBContract;

public class BeerListActivity extends BeerMeActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    int breweryId = -1;
    BeerListAdapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_list);

        final Intent intent = getIntent();
        breweryId = intent.getIntExtra("id", -1);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new ActionButtonOnClickListener());

        if (breweryId <= 0) {
            throw new IllegalArgumentException("BeerListActivity: invalid breweryId (" + breweryId + ")");
        }

        final String breweryName = intent.getStringExtra("name");

        final TextView breweryNameView = (TextView) findViewById(R.id.brewery_name);
        breweryNameView.setText(breweryName);

        final ListView beerListView = (ListView) findViewById(R.id.beer_list);
        adapter = new BeerListAdapter(this, null);
        beerListView.setAdapter(adapter);
        beerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final Intent intent = new Intent(BeerListActivity.this, BeerActivity.class);
                intent.putExtra("id", (int) id);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final String[] PROJECTION = DBContract.Beer.COLUMNS;
        final String SELECTION = DBContract.Beer.COLUMN_BREWERYID + " = ?";

        return new CursorLoader(this, DBContract.Beer.CONTENT_URI,
                PROJECTION,
                SELECTION, new String[]{Integer.toString(breweryId)},
                DBContract.Beer.COLUMN_NAME);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private static class ActionButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View view) {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}