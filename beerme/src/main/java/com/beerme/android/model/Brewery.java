package com.beerme.android.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.beerme.android.db.DBContentProvider;
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
    private int status;
    private String hours;
    private int services;
    private String phone;
    private String web;

    public Brewery(final Context context, final int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Brewery(" + id + "): Invalid id");
        }

        final DBHelper dbHelper = DBHelper.getInstance(context);
        final ContentResolver contentResolver = dbHelper.getContentResolver();
        final String[] projection = {"name", "address", "status", "hours", "services", "phone", "web"};
        final String selection = "_id=" + id;
        final Uri uri = Uri.parse("content://" + DBContentProvider.getAuthority() + "/" + DBContentProvider.getBreweryTable() + "/" + id);

        final Cursor c = contentResolver.query(uri, projection, selection, null, null);

        if ((c != null) && c.moveToFirst()) {
            c.moveToFirst();
            this.id = id;
            this.name = c.getString(0);
            Log.d("beerme", this.name);
            this.address = c.getString(1);
            this.status = c.getInt(2);
            this.hours = c.getString(3);
            this.services = c.getInt(4);
            this.phone = c.getString(5);
            this.web = c.getString(6);

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