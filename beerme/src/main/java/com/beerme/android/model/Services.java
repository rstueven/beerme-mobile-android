package com.beerme.android.model;

import android.util.SparseArray;

/**
 * Created by rstueven on 3/5/17.
 * <p/>
 * Brewery services.
 */

public class Services {
    public static final int OPEN = 0x0001;
    public static final int BAR = 0x0002;
    public static final int BEERGARDEN = 0x0004;
    public static final int FOOD = 0x0008;
    public static final int GIFTSHOP = 0x0010;
    public static final int HOTEL = 0x0020;
    public static final int INTERNET = 0x0040;
    public static final int RETAIL = 0x0080;
    public static final int TOURS = 0x0100;

    public static final SparseArray<String> ICONS = new SparseArray<>();

    static {
        ICONS.put(OPEN, "\uD83D\uDEAB");
        ICONS.put(BAR, "\uD83C\uDF7B");
        ICONS.put(BEERGARDEN, "\uD83C\uDF33");
        ICONS.put(FOOD, "\uD83C\uDF74");
        ICONS.put(GIFTSHOP, "\uD83D\uDC55");
        ICONS.put(HOTEL, "\uD83D\uDECC");
        ICONS.put(INTERNET, "\uD83D\uDCF6");
        ICONS.put(RETAIL, "\uD83C\uDF7E");
        ICONS.put(TOURS, "\uD83D\uDC63");
    }

    public static String serviceString(final int svc) {
        String services = "";

        if ((svc & OPEN) == OPEN) {
            if ((svc & BAR) == BAR) {
                services += ICONS.get(BAR);
            }
            if ((svc & BEERGARDEN) == BEERGARDEN) {
                services += ICONS.get(BEERGARDEN);
            }
            if ((svc & FOOD) == FOOD) {
                services += ICONS.get(FOOD);
            }
            if ((svc & GIFTSHOP) == GIFTSHOP) {
                services += ICONS.get(GIFTSHOP);
            }
            if ((svc & HOTEL) == HOTEL) {
                services += ICONS.get(HOTEL);
            }
            if ((svc & INTERNET) == INTERNET) {
                services += ICONS.get(INTERNET);
            }
            if ((svc & RETAIL) == RETAIL) {
                services += ICONS.get(RETAIL);
            }
            if ((svc & TOURS) == TOURS) {
                services += ICONS.get(TOURS);
            }
        } else {
            services = ICONS.get(OPEN);
        }

        return services;
    }
}