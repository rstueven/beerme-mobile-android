package com.beerme.android.db;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.beerme.android.R;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity
public class BeerListItem {
    @ColumnInfo(name = "_id")
    public long id;
    @NonNull
    public String name;
    public String stylename;
    public Double abv;
    public Double beermerating;

    public BeerListItem(long id, @NonNull String name, String stylename, Double abv, Double beermerating) {
        this.id = id;
        this.name = name;
        this.stylename = stylename;
        this.abv = abv;
        this.beermerating = beermerating;
    }

    public void showStars(@NonNull Activity activity, @NonNull LinearLayout layout) {
        if (beermerating == null) {
            return;
        }

        layout.removeAllViews();

        float stars = (beermerating <= 0) ? 0 : (float) Math.max((Math.floor(beermerating - 10.5) / 2) + 0.5, 0.5);
        int intStars = (int) stars;

        for (int i = 0; i < intStars; i++) {
            ImageView star = new ImageView(activity);
            star.setImageResource(R.drawable.star);
            layout.addView(star);
        }

        if (stars > intStars) {
            ImageView star = new ImageView(activity);
            star.setImageResource(R.drawable.star_half);
            layout.addView(star);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "BeerListItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stylename='" + stylename + '\'' +
                ", abv=" + abv +
                ", beermerating=" + beermerating +
                '}';
    }
}