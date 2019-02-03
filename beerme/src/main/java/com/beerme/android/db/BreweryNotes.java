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
    private final long id;
    private final long breweryid;
    @NonNull
//    @TypeConverters(DateConverter.class)
    private final String date;
    private final Double rating;
    private final String notes;

    public BreweryNotes(long id, long breweryid, @NonNull String date, Double rating, String notes) {
        this.id = id;
        this.breweryid = breweryid;
        this.date = date;
        this.rating = rating;
        this.notes = notes;
    }
}