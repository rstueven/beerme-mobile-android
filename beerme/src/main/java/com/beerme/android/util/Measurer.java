package com.beerme.android.util;

import android.util.Log;

import java.util.Locale;

import androidx.annotation.NonNull;

public class Measurer {
    public enum DistanceUnit {
        METERS(1),
        KM(0.001),
        FEET(3.28084),
        YARDS(0.9144),
        MILES(0.000621371);

        private final double scale;

        DistanceUnit(double scale) {
            this.scale = scale;
        }
    }

    public static String distanceToUnit(float distance, @NonNull DistanceUnit unit) {
        double scaled = distance * unit.scale;
        int places = scaled >= 10 ? 0 : 1;

        String format = String.format(Locale.getDefault(), "%%.%df %s", places, unit.name().toLowerCase(Locale.getDefault()));
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