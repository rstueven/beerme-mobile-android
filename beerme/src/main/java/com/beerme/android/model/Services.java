package com.beerme.android.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by rstueven on 3/5/17.
 * <p/>
 * Brewery services.
 */

// TODO: Should this be an enum?
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

    public static final SparseArray<Service> ICONS = new SparseArray<>();

    static class Service {
        public int code;
        public String emoji;
        public String text;
        public Drawable icon;

        public Service(final int code, final String emoji, final String text, final Drawable icon) {
            this.code = code;
            this.emoji = emoji;
            this.text = text;
            this.icon = icon;
        }
    }

    static {
        ICONS.put(OPEN, new Service(OPEN, "\uD83D\uDEAB", "Not open to the public", null));
        ICONS.put(BAR, new Service(BAR, "\uD83C\uDF7B", "Bar/Tasting Room", null));
        ICONS.put(BEERGARDEN, new Service(BEERGARDEN, "\uD83C\uDF33", "Beer Garden", null));
        ICONS.put(FOOD, new Service(FOOD, "\uD83C\uDF74", "Food", null));
        ICONS.put(GIFTSHOP, new Service(GIFTSHOP, "\uD83D\uDC55", "Items for Sale", null));
        ICONS.put(HOTEL, new Service(HOTEL, "\uD83D\uDECC", "Hotel Rooms", null));
        ICONS.put(INTERNET, new Service(INTERNET, "\uD83D\uDCF6", "Internet Access", null));
        ICONS.put(RETAIL, new Service(RETAIL, "\uD83C\uDF7E", "Beer to Go (Off License)", null));
        ICONS.put(TOURS, new Service(TOURS, "\uD83D\uDC63", "Tours", null));
    }

    public static String serviceString(final int svc) {
        String services = "";

        if ((svc & OPEN) == OPEN) {
            if ((svc & BAR) == BAR) {
                services += ICONS.get(BAR).emoji;
            }
            if ((svc & BEERGARDEN) == BEERGARDEN) {
                services += ICONS.get(BEERGARDEN).emoji;
            }
            if ((svc & FOOD) == FOOD) {
                services += ICONS.get(FOOD).emoji;
            }
            if ((svc & GIFTSHOP) == GIFTSHOP) {
                services += ICONS.get(GIFTSHOP).emoji;
            }
            if ((svc & HOTEL) == HOTEL) {
                services += ICONS.get(HOTEL).emoji;
            }
            if ((svc & INTERNET) == INTERNET) {
                services += ICONS.get(INTERNET).emoji;
            }
            if ((svc & RETAIL) == RETAIL) {
                services += ICONS.get(RETAIL).emoji;
            }
            if ((svc & TOURS) == TOURS) {
                services += ICONS.get(TOURS).emoji;
            }
        } else {
            services = ICONS.get(OPEN).emoji;
        }

        return services;
    }

    public static TableLayout serviceView(final Context context, final int svc) {
        final TableLayout tableLayout = new TableLayout(context);
        final ArrayList<Service> cells = new ArrayList<>();

        if ((svc & OPEN) == OPEN) {
            if ((svc & BAR) == BAR) {
                cells.add(ICONS.get(BAR));
            }
            if ((svc & BEERGARDEN) == BEERGARDEN) {
                cells.add(ICONS.get(BEERGARDEN));
            }
            if ((svc & FOOD) == FOOD) {
                cells.add(ICONS.get(FOOD));
            }
            if ((svc & GIFTSHOP) == GIFTSHOP) {
                cells.add(ICONS.get(GIFTSHOP));
            }
            if ((svc & HOTEL) == HOTEL) {
                cells.add(ICONS.get(HOTEL));
            }
            if ((svc & INTERNET) == INTERNET) {
                cells.add(ICONS.get(INTERNET));
            }
            if ((svc & RETAIL) == RETAIL) {
                cells.add(ICONS.get(RETAIL));
            }
            if ((svc & TOURS) == TOURS) {
                cells.add(ICONS.get(TOURS));
            }
        } else {
            cells.add(ICONS.get(OPEN));
        }

        if (!cells.isEmpty()) {
            // Set TableLayout width, height, stretchColumns=1
            int column = 0;
            final Iterator<Service> iterator = cells.iterator();
            TableRow row = new TableRow(context);
            tableLayout.addView(row);

            while (iterator.hasNext()) {
                Service service = iterator.next();
                TextView view = new TextView(context);
                view.setText(service.text);
                view.setCompoundDrawables(service.icon, null, null, null);
                row.addView(view);
                ++column;
                if ((column & 1) == 0) {
                    row = new TableRow(context);
                    tableLayout.addView(row);
                }
            }
        }

        return tableLayout;
    }
}