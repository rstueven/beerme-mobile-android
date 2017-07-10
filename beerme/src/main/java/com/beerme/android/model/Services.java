package com.beerme.android.model;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.beerme.android.R;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by rstueven on 3/5/17.
 * <p/>
 * Brewery services.
 */

// TODO: Should this be an enum?
public class Services {
    private static final int OPEN = 0x0001;
    private static final int BAR = 0x0002;
    private static final int BEERGARDEN = 0x0004;
    private static final int FOOD = 0x0008;
    private static final int GIFTSHOP = 0x0010;
    private static final int HOTEL = 0x0020;
    private static final int INTERNET = 0x0040;
    private static final int RETAIL = 0x0080;
    private static final int TOURS = 0x0100;

    private static final SparseArray<Service> SERVICES = new SparseArray<>();

    private static class Service {
        int code;
        String text;
        int icon;

        Service(final int code, final String text, final int icon) {
            this.code = code;
            this.text = text;
            this.icon = icon;
        }
    }

    static {
        SERVICES.put(OPEN, new Service(OPEN, "Not open to the public", R.drawable.ic_block_black_24dp));
        SERVICES.put(BAR, new Service(BAR, "Bar/Tasting Room", R.drawable.ic_local_drink_black_24dp));
        SERVICES.put(BEERGARDEN, new Service(BEERGARDEN, "Beer Garden", R.drawable.ic_nature_people_black_24dp));
        SERVICES.put(FOOD, new Service(FOOD, "Food", R.drawable.ic_restaurant_black_24dp));
        SERVICES.put(GIFTSHOP, new Service(GIFTSHOP, "Items for Sale", R.drawable.ic_local_mall_black_24dp));
        SERVICES.put(HOTEL, new Service(HOTEL, "Hotel Rooms", R.drawable.ic_hotel_black_24dp));
        SERVICES.put(INTERNET, new Service(INTERNET, "Internet Access", R.drawable.ic_wifi_black_24dp));
        SERVICES.put(RETAIL, new Service(RETAIL, "Beer to Go", R.drawable.ic_shopping_cart_black_24dp));
        SERVICES.put(TOURS, new Service(TOURS, "Tours", R.drawable.ic_directions_walk_black_24dp));
    }

    private static ArrayList<Service> loadServices(final int svc) {
        final ArrayList<Service> cells = new ArrayList<>();

        if ((svc & OPEN) == OPEN) {
            if ((svc & BAR) == BAR) {
                cells.add(SERVICES.get(BAR));
            }
            if ((svc & BEERGARDEN) == BEERGARDEN) {
                cells.add(SERVICES.get(BEERGARDEN));
            }
            if ((svc & FOOD) == FOOD) {
                cells.add(SERVICES.get(FOOD));
            }
            if ((svc & GIFTSHOP) == GIFTSHOP) {
                cells.add(SERVICES.get(GIFTSHOP));
            }
            if ((svc & HOTEL) == HOTEL) {
                cells.add(SERVICES.get(HOTEL));
            }
            if ((svc & INTERNET) == INTERNET) {
                cells.add(SERVICES.get(INTERNET));
            }
            if ((svc & RETAIL) == RETAIL) {
                cells.add(SERVICES.get(RETAIL));
            }
            if ((svc & TOURS) == TOURS) {
                cells.add(SERVICES.get(TOURS));
            }
        } else {
            cells.add(SERVICES.get(OPEN));
        }

        return cells;
    }

    public static LinearLayout serviceIcons(final Context context, final int svc) {
        final LinearLayout layout = new LinearLayout(context);
        final ArrayList<Service> cells = loadServices(svc);

        if (!cells.isEmpty()) {
            for (final Service service : cells) {
                //noinspection ObjectAllocationInLoop
                final ImageView view = new ImageView(context);
                view.setImageDrawable(ContextCompat.getDrawable(context, service.icon));
                layout.addView(view);
            }
        }

        return layout;
    }

    public static TableLayout serviceView(final Context context, final int svc) {
        final TableLayout tableLayout = new TableLayout(context);
        final ArrayList<Service> cells = loadServices(svc);

        if (!cells.isEmpty()) {
            // Set TableLayout width, height, stretchColumns=1
            int column = 0;
            final Iterator<Service> iterator = cells.iterator();
            TableRow row = new TableRow(context);
            tableLayout.addView(row);

            while (iterator.hasNext()) {
                final Service service = iterator.next();
                //noinspection ObjectAllocationInLoop
                final TextView view = new TextView(context);
                view.setText(service.text);
                view.setCompoundDrawablesWithIntrinsicBounds(service.icon, 0, 0, 0);
                view.setCompoundDrawablePadding(8);
                row.addView(view);
                ++column;
                if ((column & 1) == 0) {
                    //noinspection ObjectAllocationInLoop
                    row = new TableRow(context);
                    tableLayout.addView(row);
                }
            }
        }

        return tableLayout;
    }
}