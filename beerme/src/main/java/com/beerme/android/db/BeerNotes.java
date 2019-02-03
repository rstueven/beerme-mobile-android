package com.beerme.android.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "beernotes", indices = {@Index("beerid"), @Index("sampled")})
class BeerNotes {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    private final long id;
    private final long beerid;
    @ColumnInfo(name = "package")
    private final String pkg;
    //    @TypeConverters(DateConverter.class)
    private final String sampled;
    private final String place;
    private final Double appscore;
    private final String appearance;
    private final Double aroscore;
    private final String aroma;
    private final Double mouscore;
    private final String mouthfeel;
    private final Double ovrscore;
    private final String notes;

    public BeerNotes(long id, long beerid, String pkg, String sampled, String place, Double appscore, String appearance, Double aroscore, String aroma, Double mouscore, String mouthfeel, Double ovrscore, String notes) {
        this.id = id;
        this.beerid = beerid;
        this.pkg = pkg;
        this.sampled = sampled;
        this.place = place;
        this.appscore = appscore;
        this.appearance = appearance;
        this.aroscore = aroscore;
        this.aroma = aroma;
        this.mouscore = mouscore;
        this.mouthfeel = mouthfeel;
        this.ovrscore = ovrscore;
        this.notes = notes;
    }
}
