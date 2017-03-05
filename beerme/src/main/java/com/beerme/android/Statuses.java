package com.beerme.android;

import android.util.SparseArray;

/**
 * Created by rstueven on 3/5/17.
 * <p/>
 * Brewery statuses. Probably better located in the Brewery class.
 */

public class Statuses {
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

        if ("".equals(s)) {
            s = null;
        } else {
            s = "(" + s + ")";
        }

        return s;
    }
}