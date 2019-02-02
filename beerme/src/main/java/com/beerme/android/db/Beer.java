package com.beerme.android.db;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index("breweryid"), @Index("name"), @Index("updated")})
public class Beer {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    public long id;
    public long breweryid;
    @NonNull
    public String name;
    public Long style;
    public Double abv;
    public String image;
    @NonNull
//    @TypeConverters(DateConverter.class)
    public String updated;
    public Double beermerating;

    // TODO: Some of these could be e.g. optString()
    public Beer(@NonNull JSONObject obj) throws JSONException {
        try {
            this.id = obj.getInt("id");
            this.breweryid = obj.getInt("breweryid");
            this.name = obj.getString("name");
            this.style = obj.optLong("style");
            this.abv = obj.optDouble("abv");
            this.image = obj.getString("gfx");
            this.updated = obj.getString("updated");
            this.beermerating = obj.optDouble("beermerating");
        } catch (JSONException e) {
            Log.e("beerme", obj.toString());
            throw new JSONException("Beer() failed: " + e.getLocalizedMessage());
        }
    }

    public Beer(long id, long breweryid, @NonNull String name, Long style, Double abv, String image, @NonNull String updated, Double beermerating) {
        this.id = id;
        this.breweryid = breweryid;
        this.name = name;
        this.style = style;
        this.abv = abv;
        this.image = image;
        this.updated = updated;
        this.beermerating = beermerating;
    }

    @Override
    public String toString() {
        return "Beer{" +
                "id=" + id +
                ", breweryid=" + breweryid +
                ", name='" + name + '\'' +
                ", style=" + style +
                ", abv=" + abv +
                ", image='" + image + '\'' +
                ", updated='" + updated + '\'' +
                ", beermerating=" + beermerating +
                '}';
    }
}