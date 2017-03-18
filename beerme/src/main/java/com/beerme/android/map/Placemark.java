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
        this(c.getInt(0), c.getString(1), new LatLng(c.getFloat(2), c.getFloat(3)));
    }
}