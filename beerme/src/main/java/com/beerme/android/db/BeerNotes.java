package com.beerme.android.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "beernotes", indices = {@Index("beerid"), @Index("sampled")})
class BeerNotes {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    public final long id;
    public final long beerid;
    @ColumnInfo(name = "package")
    public final String pkg;
    //    @TypeConverters(DateConverter.class)
    public final String sampled;
    public final String place;
    public final Double appscore;
    public final String appearance;
    public final Double aroscore;
    public final String aroma;
    public final Double mouscore;
    public final String mouthfeel;
    public final Double ovrscore;
    public final String notes;

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
