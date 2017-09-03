package com.beerme.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.SparseArray;

/**
 * Created by rstueven on 3/5/17.
 * <p/>
 * Brewery Status. Probably better located in the Brewery class.
 */

// TODO: Should this be an enum?
public class Status {
    public static final int OPEN = 0x1;
    public static final int PLANNED = 0x2;
    public static final int NO_LONGER_BREWING = 0x4;
    public static final int CLOSED = 0x8;
    public static final int DELETED = 0x10;

    public static final SparseArray<String> STATUS = new SparseArray<>();

    static {
        STATUS.put(OPEN, "");
        STATUS.put(PLANNED, "Planned");
        STATUS.put(NO_LONGER_BREWING, "No longer brewing");
        STATUS.put(CLOSED, "Closed");
        STATUS.put(DELETED, "Deleted");
    }

    public static String statusString(final int status) {
        String s = STATUS.get(status);

        if ((s != null) && s.isEmpty()) {
            s = null;
        } else {
            s = "(" + s + ")";
        }

        return s;
    }

    public static String statusClause(final Context context) {
        return "(status & " + Status.statusMask(context) + ") != 0";
    }

    public static int statusMask(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final int open = prefs.getBoolean("status_filter_open", false) ? OPEN : 0;
        final int planned = prefs.getBoolean("status_filter_planned", false) ? PLANNED : 0;
        final int nlb = prefs.getBoolean("status_filter_no_longer_brewing", false) ? NO_LONGER_BREWING : 0;
        final int closed = prefs.getBoolean("status_filter_closed", false) ? CLOSED : 0;

        return open | planned | nlb | closed;
    }
}