package com.beerme.android;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.beerme.android.db.DBContract;

public class BreweryListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, SearchActivity.BeerMeSearch {
    private final int BREWERY_LOADER = 0;
    private SearchAdapter breweryAdapter;
    private Context mContext;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_brewery_list, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.brewery_list);
        listView.setEmptyView(view.findViewById(R.id.brewery_list_empty));
        breweryAdapter = new SearchAdapter(mContext, null);
        listView.setAdapter(breweryAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final Intent intent = new Intent(mContext, BreweryActivity.class);
                intent.putExtra("id", (int) id);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void search(final String query) {
        final String[] projection = new String[]{DBContract.Brewery.COLUMN_ID, DBContract.Brewery.COLUMN_NAME, DBContract.Brewery.COLUMN_ADDRESS};
        final Bundle breweryArgs = new Bundle();
        breweryArgs.putStringArray("selection_args", new String[]{"%" + query + "%"});
        breweryArgs.putStringArray("projection", projection);
        getLoaderManager().initLoader(BREWERY_LOADER, breweryArgs, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final String SELECTION = "name LIKE ?";
        final String ORDER = "name ASC";

        switch (id) {
            case BREWERY_LOADER:
                return new CursorLoader(mContext,
                        DBContract.Brewery.CONTENT_URI,
                        args.getStringArray("projection"),
                        SELECTION,
                        args.getStringArray("selection_args"),
                        ORDER);
            default:
                throw new IllegalArgumentException("Unknown id: " + id);
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        final int id = loader.getId();

        switch (id) {
            case BREWERY_LOADER:
                breweryAdapter.swapCursor(data);
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
            default:
                throw new IllegalArgumentException("Unknown id: " + id);
        }
    }
}
