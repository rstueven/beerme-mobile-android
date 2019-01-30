package com.beerme.android.db;

import com.beerme.android.util.DateConverter;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

@Dao
@TypeConverters(DateConverter.class)
public abstract class BreweryDao {
    @Query("SELECT * FROM brewery WHERE _id = :id")
    abstract LiveData<Brewery> getBrewery(int id);

    @Query("SELECT * FROM brewery WHERE name LIKE :name")
    abstract LiveData<Brewery> getBreweryByName(String name);

    @Query("SELECT * FROM brewery ORDER BY name")
    abstract LiveData<List<Brewery>> getAllBreweries();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void insert(Brewery brewery);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void insertAll(Brewery... breweries);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract void update(Brewery brewery);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract void updateAll(Brewery... breweries);

    // upsert: https://stackoverflow.com/a/48641762/295028
//    public void upsert(Brewery brewery) {
//        try {
//            insert(brewery);
//        } catch (SQLiteConstraintException e) {
//            update(brewery);
//        }
//    }
//
//    public void upsertAll(Brewery... breweries) {
//        for (Brewery brewery : breweries) {
//            try {
//                insert(brewery);
//            } catch (SQLiteConstraintException e) {
//                update(brewery);
//            }
//        }
//    }

//    @Delete
//    void delete(Brewery brewery);
//
//    @Delete
//    void deleteAll(Brewery... breweries);
}