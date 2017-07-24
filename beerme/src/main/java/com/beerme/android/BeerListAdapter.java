package com.beerme.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerme.android.db.DBContract;
import com.beerme.android.model.Beer;

import java.util.Locale;

/**
 * Created by rstueven on 7/15/17.
 * <p/>
 * Manages the beer list.
 */

class BeerListAdapter extends CursorAdapter {
    final private LayoutInflater inflater;

    BeerListAdapter(final Context context, final Cursor c) {
        super(context, c, 0);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        return inflater.inflate(R.layout.row_beer_list, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final Beer beer = new Beer(context, cursor.getInt(cursor.getColumnIndex(DBContract.Beer.COLUMN_ID)));
        final TextView beerNameView = (TextView) view.findViewById(R.id.beer_name);
        final String beerName = beer.getName();
        beerNameView.setText(beerName);

        final TextView breweryNameView = (TextView) view.findViewById(R.id.brewery_name);
        final String breweryName = beer.getBreweryName();
        breweryNameView.setText(breweryName);

        final TextView styleNameView = (TextView) view.findViewById(R.id.style_name);
        styleNameView.setText(beer.getStyle());

        final double abv = beer.getAbv();
        final TextView abvView = (TextView) view.findViewById(R.id.abv);
        if (abv > 0) {
            abvView.setText(String.format(Locale.getDefault(), "%.1f%% abv", abv));
        }

        final FrameLayout starFrame = (FrameLayout) view.findViewById(R.id.stars_layout);
        if (beer.getBeermerating() <= 0) {
            starFrame.setVisibility(View.INVISIBLE);
        } else {
            final LinearLayout starLayout = beer.getStars();
            starFrame.addView(starLayout);
            starFrame.setVisibility(View.VISIBLE);
        }
    }
}
