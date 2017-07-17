package com.beerme.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by rstueven on 7/16/17.
 * <p/>
 * Manages lists of search results.
 */

class SearchAdapter extends CursorAdapter {
    final private LayoutInflater inflater;

    SearchAdapter(final Context context, final Cursor c) {
        super(context, c, 0);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        return inflater.inflate(R.layout.row_search, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final TextView row1 = (TextView) view.findViewById(R.id.row1);
        final TextView row2 = (TextView) view.findViewById(R.id.row2);

        // Assume that column 0 is the _id
        if (cursor.getColumnCount() >= 2) {
            row1.setText(cursor.getString(1));
        }
        if (cursor.getColumnCount() >= 3) {
            row2.setText(cursor.getString(2));
        }
    }
}