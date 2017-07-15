package com.beerme.android.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.beerme.android.db.DBContract;
import com.beerme.android.db.DBHelper;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by rstueven on 7/3/17.
 * Brewery data.
 */

public class Brewery {
    private int id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int status;
    private String hours;
    private String phone;
    private String web;
    private int services;
    private String image;

    public Brewery(final Context context, final int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Brewery(" + id + "): Invalid id");
        }

        final DBHelper dbHelper = DBHelper.getInstance(context);
        final ContentResolver contentResolver = dbHelper.getContentResolver();
        final Uri uri = DBContract.Brewery.buildUri(id);

        final Cursor c = contentResolver.query(uri, DBContract.Brewery.COLUMNS, null, null, null);

        if ((c != null) && c.moveToFirst()) {
            c.moveToFirst();
            this.id = id;
            this.name = c.getString(c.getColumnIndex("name"));
            this.address = c.getString(c.getColumnIndex("address"));
            this.latitude = c.getDouble(c.getColumnIndex("latitude"));
            this.longitude = c.getDouble(c.getColumnIndex("longitude"));
            this.status = c.getInt(c.getColumnIndex("status"));
            this.hours = c.getString(c.getColumnIndex("hours"));
            this.phone = c.getString(c.getColumnIndex("phone"));
            this.web = c.getString(c.getColumnIndex("web"));
            this.services = c.getInt(c.getColumnIndex("services"));
            this.image = c.getString(c.getColumnIndex("image"));

            c.close();
        }
    }

    @Override
    public String toString() {
        String s = "";
        s += "id: " + this.id + "\n";
        s += "name: " + this.name + "\n";
        s += "address: " + this.address + "\n";
        s += "status: " + this.status + "\n";
        s += "hours: " + this.hours + "\n";
        s += "services: " + this.services + "\n";
        s += "phone: " + this.phone + "\n";
        s += "web: " + this.web + "\n";

        return s;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getStatus() {
        return status;
    }

    public String getHours() {
        return hours;
    }

    public int getServices() {
        return services;
    }

    public String getPhone() {
        return phone;
    }

    public String getWeb() {
        return web;
    }

    public String getImage() {
        return image;
    }

    public String getWebForDisplay() {
        if ((web == null) || web.isEmpty()) {
            return "";
        } else {
            // Not sure why a regex didn't work here.
            final URL url;
            String webForDisplay;
            try {
                url = new URL(web);
                webForDisplay = url.getHost();
                if (!"/".equals(url.getPath())) {
                    webForDisplay += "/" + url.getPath();
                }
            } catch (final MalformedURLException e) {
                webForDisplay = "";
                Log.w("beerme", "getWebForDisplay(" + web + "): " + e.getLocalizedMessage());
            }
            return webForDisplay;
        }
    }
}