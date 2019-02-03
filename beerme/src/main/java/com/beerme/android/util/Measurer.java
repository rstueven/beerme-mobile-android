package com.beerme.android.util;

import android.util.Log;

import java.util.Locale;

import androidx.annotation.NonNull;

public class Measurer {
    public enum DistanceUnit {
        MILES(0, 0.000621371),
        KILOMETERS(1, 0.001),
        METERS(2, 1),
        YARDS(3, 1.09361),
        FEET(4, 3.28084);

        public static final int DEFAULT = MILES.code;

        // TODO: code shouldn't actually be necessary
        private final int code;
        private final double scale;

        DistanceUnit(int code, double scale) {
            this.code = code;
            this.scale = scale;
        }

        public static DistanceUnit byCode(int code) {
            for (DistanceUnit s : values()) {
                if (s.code == code) {
                    return s;
                }
            }

            return byCode(DEFAULT);
        }

        public static final int size = DistanceUnit.values().length;

        public static String[] names() {
            String[] arr = new String[size];
            String name;

            int i = 0;
            for (DistanceUnit s : values()) {
                name = s.name().replace("_", " ");
                arr[i] = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                i++;
            }

            return arr;
        }
    }

    public static String distanceToUnit(float distance, @NonNull DistanceUnit unit) {
        double scaled = distance * unit.scale;
        int places = scaled >= 10 ? 0 : 1;
        String unitName = unit == DistanceUnit.KILOMETERS ? "km" : unit.name().toLowerCase(Locale.getDefault());
        String format = String.format(Locale.getDefault(), "%%.%df %s", places, unitName);
        return String.format(Locale.getDefault(), format, scaled);
    }

    public static String bearingToDirection(float bearing) {
        if (bearing > -22.5 && bearing <= 22.5) {
            return "N";
        } else if (bearing > 22.5 && bearing <= 67.5) {
            return "NE";
        } else if (bearing > 67.5 && bearing <= 112.5) {
            return "E";
        } else if (bearing > 112.5 && bearing <= 157.5) {
            return "SE";
        } else if ((bearing > 157.5 && bearing <= 180.0) || (bearing < -157.5 && bearing >= -180.0)) {
            return "S";
        } else if (bearing > -157.5 && bearing <= -112.5) {
            return "SW";
        } else if (bearing > -112.5 && bearing <= -67.5) {
            return "W";
        } else if (bearing > -67.5 && bearing <= -22.5) {
            return "NW";
        } else {
            Log.w("beerme", "Measurer.bearingToDirection(" + bearing + "): Illegal bearing");
            return "";
        }
    }
}