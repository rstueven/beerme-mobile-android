package com.beerme.android.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "brewerynotes", indices = {@Index("breweryid"), @Index("date")})
class BreweryNotes {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    public final long id;
    public final long breweryid;
    @NonNull
//    @TypeConverters(DateConverter.class)
    public final String date;
    public final Double rating;
    public final String notes;

    public BreweryNotes(long id, long breweryid, @NonNull String date, Double rating, String notes) {
        this.id = id;
        this.breweryid = breweryid;
        this.date = date;
        this.rating = rating;
        this.notes = notes;
    }
}