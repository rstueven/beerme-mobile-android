package com.beerme.android.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beerme.android.db.DBHelper;

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

    public Brewery(final Context context, final int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Brewery(" + id + "): Invalid id");
        }

        final DBHelper dbHelper = DBHelper.getInstance(context);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();

        final String sql = "SELECT name, address, status, hours, services FROM brewery WHERE _id = ?";
        final Cursor c = db.rawQuery(sql, new String[]{Integer.toString(id)});

        if (c.getCount() == 1) {
            c.moveToFirst();
            this.id = id;
            this.name = c.getString(0);
            this.address = c.getString(1);
            this.status = c.getInt(2);
            this.hours = c.getString(3);
            this.services = c.getInt(4);
        }

        c.close();
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
}