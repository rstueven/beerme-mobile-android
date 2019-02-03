package com.beerme.android.db;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.location.Location;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.util.Measurer;
import com.beerme.android.util.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index("name"), @Index("latitude"), @Index("longitude"), @Index("status"), @Index("updated")})
public class Brewery implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    public long id;
    @NonNull
    public String name;
    @NonNull
    public String address;
    public double latitude;
    public double longitude;
    public int status;
    public String hours;
    public String phone;
    public String web;
    public int services;
    public String image;
    @NonNull
//    @TypeConverters(DateConverter.class)
    public String updated;

    public enum Status {
        OPEN(1),
        PLANNED(2),
        NO_LONGER_BREWING(4),
        CLOSED(8),
        DELETED(16);

        final int code;

        public final static int DEFAULT = OPEN.code | PLANNED.code;

        Status(int code) {
            this.code = code;
        }

        static Status byCode(int code) {
            for (Status s : values()) {
                if (s.code == code) {
                    return s;
                }
            }

            return null;
        }

        public static final int size = Status.values().length;

        public static String[] names() {
            String[] arr = new String[size];
            String name;

            int i = 0;
            for (Status s : values()) {
                name = s.name().replace("_", " ");
                arr[i] = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                i++;
            }

            return arr;
        }
    }

    public enum Service {
        OPEN(0x0001, 0, null),
        BAR(0x0002, R.drawable.bar, "Bar / Tasting Room"),
        BEERGARDEN(0x0004, R.drawable.beergarden, "Beer Garden / Outdoor Seating"),
        FOOD(0x0008, R.drawable.food, "Food"),
        GIFTSHOP(0x0010, R.drawable.giftshop, "Retail Items (Shirts, hats, glassware, etc)"),
        HOTEL(0x0020, R.drawable.hotel, "Hotel Rooms"),
        INTERNET(0x0040, R.drawable.wifi, "Internet Access"),
        RETAIL(0x0080, R.drawable.retail, "Beer to Go / Off-License Sales"),
        TOURS(0x0100, R.drawable.tours, "Tours");

        private final int code;
        private final int iconRes;
        private final String description;

        Service(int code, int iconRes, String description) {
            this.code = code;
            this.iconRes = iconRes;
            this.description = description;
        }
    }

    // TODO: Some of these could be e.g. optString()
    public Brewery(@NonNull JSONObject obj) throws JSONException {
        try {
            this.id = obj.getInt("id");
            this.name = obj.getString("name");
            this.address = obj.getString("address");
            this.latitude = obj.getDouble("latitude");
            this.longitude = obj.getDouble("longitude");
            this.status = obj.getInt("status");
            this.hours = obj.getString("hours");
            this.phone = obj.getString("phone");
            this.web = obj.getString("web");
            this.services = obj.getInt("services");
            this.image = obj.getString("gfx");
            this.updated = obj.getString("updated");
        } catch (JSONException e) {
            Log.e("beerme", "Brewery(" + obj.toString() + ")");
            throw new JSONException("Brewery(): " + e.getLocalizedMessage());
        }
    }

    public Brewery(long id, @NonNull String name, @NonNull String address, double latitude, double longitude, int status, String hours, String phone, String web, int services, String image, @NonNull String updated) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.hours = hours;
        this.phone = phone;
        this.web = web;
        this.services = services;
        this.image = image;
        this.updated = updated;
    }

    public String getDistanceText(Location location) {
        if (location == null) {
            return "";
        } else {
            float[] results = new float[2];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), latitude, longitude, results);
            Measurer.DistanceUnit distanceUnit = Measurer.DistanceUnit.byCode(SharedPref.read(SharedPref.Pref.DISTANCE_UNIT, Measurer.DistanceUnit.DEFAULT));
            String distance = Measurer.distanceToUnit(results[0], distanceUnit);
            float bearing = results[1];

            return String.format(Locale.getDefault(), "%s %s", distance, Measurer.bearingToDirection(bearing));
        }
    }

    public String getStatusText() {
        Status s = Status.byCode(status);

        if (s == null || s == Status.OPEN) {
            return "";
        } else {
            String t = s.name().replace("_", " ");

            return "(" + t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase() + ")";
        }
    }

    public void showServiceIcons(@NonNull Activity activity, @NonNull LinearLayout servicesView) {
        servicesView.removeAllViews();

        for (Service svc : Service.values()) {
            if ((services & svc.code) != 0) {
                ImageView icon = new ImageView(activity);
                icon.setImageResource(svc.iconRes);
                ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorPrimary)));
                servicesView.addView(icon);
            }
        }
    }

    public void showServicesByName(@NonNull Activity activity, @NonNull LinearLayout servicesView) {
        servicesView.removeAllViews();

        for (Service svc : Service.values()) {
            if ((services & svc.code) != 0) {
                TextView row = new TextView(activity);
                row.setCompoundDrawablesWithIntrinsicBounds(svc.iconRes, 0, 0, 0);
                row.setCompoundDrawablePadding((int)(4 * activity.getResources().getDisplayMetrics().density));
                row.setText(svc.description);
                servicesView.addView(row);
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Brewery{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", status=" + status +
                ", hours='" + hours + '\'' +
                ", phone='" + phone + '\'' +
                ", web='" + web + '\'' +
                ", services=" + services +
                ", image='" + image + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}