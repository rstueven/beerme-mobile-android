package com.beerme.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.beerme.android.db.DBContract;
import com.beerme.android.model.Style;

import java.util.Locale;

/**
 * Created by rstueven on 7/15/17.
 * <p/>
 * Manages the beer list.
 */

public class BeerListAdapter extends CursorAdapter {
    final private LayoutInflater inflater;

    public BeerListAdapter(final Context context, final Cursor c) {
        super(context, c, 0);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        return inflater.inflate(R.layout.row_beer_list, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final TextView beerNameView = (TextView) view.findViewById(R.id.beer_name);
        final String beerName = cursor.getString(cursor.getColumnIndex(DBContract.Beer.COLUMN_NAME));
        beerNameView.setText(beerName);

        final int styleId = cursor.getInt(cursor.getColumnIndex(DBContract.Beer.COLUMN_STYLE));
        if (styleId > 0) {
            final Style style = new Style(context, styleId);
            final String styleName = style.getName();
            final TextView styleNameView = (TextView) view.findViewById(R.id.style_name);
            styleNameView.setText(styleName);
        }

        final double abv = cursor.getDouble(cursor.getColumnIndex(DBContract.Beer.COLUMN_ABV));
        if (abv > 0) {
            final TextView abvView = (TextView) view.findViewById(R.id.abv);
            abvView.setText(String.format(Locale.getDefault(), "%.2f%%", abv));
        }

        final double beermerating = cursor.getDouble(cursor.getColumnIndex(DBContract.Beer.COLUMN_BEERMERATING));
        if (beermerating > 0) {
            final TextView beermeratingView = (TextView) view.findViewById(R.id.beerme_rating);
            beermeratingView.setText(String.format(Locale.getDefault(), "%.1f points", beermerating));
        }
    }
}
