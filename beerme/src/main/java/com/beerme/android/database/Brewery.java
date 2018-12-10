package com.beerme.android.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.prefs.BreweryStatusFilterPreference;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.Utils;

import java.io.Serializable;
import java.util.Locale;

public class Brewery implements Serializable {
    private static final long serialVersionUID = 6814750021557887747L;
    private static final String TABLE = "brewery";
    private long id;
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
    private String updated;

    public Brewery(Context context, long breweryId) {
        if (context == null) {
            throw new IllegalArgumentException("null context");
        }

        if (breweryId <= 0) {
            throw new IllegalArgumentException("Invalid breweryId(" + breweryId
                    + ")");
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DbOpenHelper.getInstance(context).getReadableDatabase();
            String[] mColumns = TableDefs.getColumns(db, TABLE);

            cursor = db.query(TABLE, mColumns, "_id=" + breweryId, null, null,
                    null, null);

            cursor.moveToFirst();
            id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            latitude = cursor.getDouble(cursor
                    .getColumnIndexOrThrow("latitude"));
            longitude = cursor.getDouble(cursor
                    .getColumnIndexOrThrow("longitude"));
            status = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
            hours = cursor.getString(cursor.getColumnIndexOrThrow("hours"));
            phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            web = cursor.getString(cursor.getColumnIndexOrThrow("web"));
            services = cursor.getInt(cursor.getColumnIndexOrThrow("services"));
            image = cursor.getString(cursor.getColumnIndexOrThrow("image"));
            updated = cursor.getString(cursor.getColumnIndexOrThrow("updated"));

        } catch (SQLiteException e) {
            ErrLog.log(context, "Brewery(" + breweryId + ")", e,
                    R.string.Database_is_busy);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        if (address == null || "null".equals(address)) {
            return "";
        }
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
        if (hours == null || "null".equals(hours)) {
            return "";
        }
        return hours;
    }

    public String getPhone() {
        if (phone == null || "null".equals(phone)) {
            return "";
        }
        return phone;
    }

    public String getWeb() {
        if (web == null || "null".equals(web)) {
            return "";
        }
        return web;
    }

    public int getServices() {
        return this.services;
    }

    public boolean isOpenPublic() {
        return (services & Utils.OPEN) != 0;
    }

    public boolean hasBar() {
        return (services & Utils.BAR) != 0;
    }

    public boolean hasBeergarden() {
        return (services & Utils.BEERGARDEN) != 0;
    }

    public boolean hasFood() {
        return (services & Utils.FOOD) != 0;
    }

    public boolean hasGiftshop() {
        return (services & Utils.GIFTSHOP) != 0;
    }

    public boolean hasHotel() {
        return (services & Utils.HOTEL) != 0;
    }

    public boolean hasInternet() {
        return (services & Utils.INTERNET) != 0;
    }

    public boolean hasRetail() {
        return (services & Utils.RETAIL) != 0;
    }

    public boolean hasTours() {
        return (services & Utils.TOURS) != 0;
    }

    public String getImage() {
        if (image == null || "null".equals(image)) {
            return "";
        }
        return image;
    }

    public String getUpdated() {
        return this.updated;
    }

    public float getRating(Context context) {
        float rating = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DbOpenHelper.getInstance(context).getReadableDatabase();

            cursor = db.rawQuery(
                    "SELECT AVG(rating) FROM brewerynotes WHERE breweryid="
                            + id, null);

            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                rating = cursor.getFloat(0);
            }
        } catch (SQLiteException e) {
            ErrLog.log(context, "Brewery.getRating()", e,
                    R.string.Database_is_busy);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return rating;
    }

    public int getNoteCount(Context context) {
        int count = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DbOpenHelper.getInstance(context).getReadableDatabase();

            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM brewerynotes WHERE breweryid=" + id,
                    null);

            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
        } catch (SQLiteException e) {
            ErrLog.log(context, "Brewery.getNoteCount()", e,
                    R.string.Database_is_busy);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return count;
    }

    public void displayServiceIcons(Context context, LinearLayout services) {
        services.removeAllViews();

        int status = this.getStatus();

        if ((status & BreweryStatusFilterPreference.OPEN) == 0) {
            TextView statusTextView = new TextView(context);
            String statusText = context.getResources().getStringArray(
                    R.array.status_value)[BreweryStatusFilterPreference
                    .getIndex(status)];
            statusTextView.setText(String.format(Locale.getDefault(), "(%s)", statusText));
            services.addView(statusTextView);
        } else {
            if (!this.isOpenPublic()) {
                insertServiceIcon(context, services, R.drawable.donotenter);
            } else {
                if (this.hasBar()) {
                    insertServiceIcon(context, services, R.drawable.bar);
                }
                if (this.hasBeergarden()) {
                    insertServiceIcon(context, services, R.drawable.beergarden);
                }
                if (this.hasFood()) {
                    insertServiceIcon(context, services, R.drawable.food);
                }
                if (this.hasGiftshop()) {
                    insertServiceIcon(context, services, R.drawable.giftshop);
                }
                if (this.hasHotel()) {
                    insertServiceIcon(context, services, R.drawable.hotel);
                }
                if (this.hasInternet()) {
                    insertServiceIcon(context, services, R.drawable.internet);
                }
                if (this.hasRetail()) {
                    insertServiceIcon(context, services, R.drawable.retail);
                }
                if (this.hasTours()) {
                    insertServiceIcon(context, services, R.drawable.tours);
                }
            }
        }
    }

    private static void insertServiceIcon(Context context, LinearLayout svc,
                                          int id) {
        ImageView img = new ImageView(context);
        img.setImageResource(id);
        svc.addView(img);
    }

    @Override
    public String toString() {
        return ("_id: " + getId() + "\n") +
                "name: " + getName() + "\n" +
                "address: " + getAddress() + "\n" +
                "latitude: " + getLatitude() + "\n" +
                "longitude: " + getLongitude() + "\n" +
                "status: " + getStatus() + "\n" +
                "hours: " + getHours() + "\n" +
                "phone: " + getPhone() + "\n" +
                "web: " + getWeb() + "\n" +
                "openPublic: " + isOpenPublic() + "\n" +
                "bar: " + hasBar() + "\n" +
                "beergarden: " + hasBeergarden() + "\n" +
                "food: " + hasFood() + "\n" +
                "giftshop: " + hasGiftshop() + "\n" +
                "hotel: " + hasHotel() + "\n" +
                "internet: " + hasInternet() + "\n" +
                "retail: " + hasRetail() + "\n" +
                "tours: " + hasTours() + "\n" +
                "image: " + getImage() + "\n" +
                "updated: " + getUpdated() + "\n";
    }
}