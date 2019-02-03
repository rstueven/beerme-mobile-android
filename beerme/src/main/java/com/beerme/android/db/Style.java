package com.beerme.android.db;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index("updated")})
public class Style {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    public long id;
    @NonNull
    public String name;
    @NonNull
//    @TypeConverters(DateConverter.class)
    public String updated;

    // TODO: Some of these could be e.g. optString()
    Style(@NonNull JSONObject obj) throws JSONException {
        try {
            this.id = obj.getInt("id");
            this.name = obj.getString("name");
            this.updated = obj.getString("updated");
        } catch (JSONException e) {
            Log.e("beerme", obj.toString());
            throw new JSONException("Style(): " + e.getLocalizedMessage());
        }
    }


    Style(long id, @NonNull String name, @NonNull String updated) {
        this.id = id;
        this.name = name;
        this.updated = updated;
    }
}