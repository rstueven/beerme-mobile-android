package com.beerme.android.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.beerme.android.BeerMeApplication;
import com.beerme.android.R;
import com.beerme.android.db.DBContract;
import com.beerme.android.db.DBHelper;

/**
 * Created by rstueven on 7/15/17.
 * <p/>
 * Beer data.
 */

public class Beer {
    private int id;
    private int breweryid;
    private String name;
    private int style;
    private double abv;
    private String image;
    private double beermerating;

    public Beer(final Context context, final int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Beer(" + id + "): Invalid id");
        }

        final DBHelper dbHelper = DBHelper.getInstance(context);
        final ContentResolver contentResolver = dbHelper.getContentResolver();
        final Uri uri = DBContract.Beer.buildUri(id);

        final Cursor c = contentResolver.query(uri, DBContract.Beer.COLUMNS, null, null, null);

        if ((c != null) && c.moveToFirst()) {
            c.moveToFirst();
            this.id = id;
            this.breweryid = c.getInt(c.getColumnIndex(DBContract.Beer.COLUMN_BREWERYID));
            this.name = c.getString(c.getColumnIndex(DBContract.Beer.COLUMN_NAME));
            this.style = c.getInt(c.getColumnIndex(DBContract.Beer.COLUMN_STYLE));
            this.abv = c.getDouble(c.getColumnIndex(DBContract.Beer.COLUMN_ABV));
            this.image = c.getString(c.getColumnIndex(DBContract.Beer.COLUMN_IMAGE));
            this.beermerating = c.getDouble(c.getColumnIndex(DBContract.Beer.COLUMN_BEERMERATING));

            c.close();
        }
    }

    @Override
    public String toString() {
        String s = "";
        s += "id: " + this.id + "\n";
        s += "breweryid: " + this.breweryid + "\n";
        s += "name: " + this.name + "\n";
        s += "style: " + this.style + "\n";
        s += "abv: " + this.abv + "\n";
        s += "image: " + this.image + "\n";
        s += "beermerating: " + this.beermerating + "\n";

        return s;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getBreweryid() {
        return breweryid;
    }

    public void setBreweryid(final int breweryid) {
        this.breweryid = breweryid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(final int style) {
        this.style = style;
    }

    public double getAbv() {
        return abv;
    }

    public void setAbv(final double abv) {
        this.abv = abv;
    }

    public String getImage() {
        return image;
    }

    public void setImage(final String image) {
        this.image = image;
    }

    public double getBeermerating() {
        return beermerating;
    }

    public void setBeermerating(final double beermerating) {
        this.beermerating = beermerating;
    }

    public double getStarCount() {
        return (beermerating <= 0) ? 0 : (float) Math.max((Math.floor(beermerating - 10.5) / 2) + 0.5, 0.5);
    }

    public LinearLayout getStars() {
        final Context context = BeerMeApplication.getInstance();
        final double starCount = getStarCount();
        final int wholeStars = (int)starCount;
        final LinearLayout layout = new LinearLayout(context);

        for (int i = 0; i < wholeStars; i++) {
            @SuppressWarnings("ObjectAllocationInLoop")
            final ImageView star = new ImageView(context);
            final Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_star_black_24dp);
            drawable.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
            star.setImageDrawable(drawable);
            layout.addView(star);
        }

        if ((starCount - wholeStars) > 0) {
            final ImageView star = new ImageView(context);
            final Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_star_half_black_24dp);
            drawable.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
            star.setImageDrawable(drawable);
            layout.addView(star);
        }

        return layout;
    }
}