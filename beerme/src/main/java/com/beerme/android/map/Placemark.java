package com.beerme.android.map;

import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rstueven on 2/26/17.
 * <p/>
 * Details for a map marker.
 */

public class Placemark {
    public int id;
    public String name;
    public LatLng position;
    public String status;

    public Placemark(final int id, final String name, final LatLng position) {
        this.id = id;
        this.name = name;
        this.position = position;
    }

    public Placemark(final Cursor c) {
        this(c.getInt(c.getColumnIndex("_id")), c.getString(c.getColumnIndex("name")), new LatLng(c.getFloat(c.getColumnIndex("latitude")), c.getFloat(c.getColumnIndex("longitude"))));
    }
}