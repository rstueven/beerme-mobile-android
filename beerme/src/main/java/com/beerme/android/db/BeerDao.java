package com.beerme.android.db;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public abstract class BeerDao {
    @Query("SELECT * FROM beer WHERE _id = :id")
    abstract LiveData<Beer> getBeer(int id);

    @Query("SELECT * FROM beer WHERE name LIKE :name")
    abstract LiveData<Beer> getBeerByName(String name);

    @Query("SELECT * FROM beer WHERE breweryid = :breweryId ORDER BY name")
    abstract LiveData<List<Beer>> getBeersByBreweryId(long breweryId);

    @Query("SELECT * FROM beer")
    abstract LiveData<List<Beer>> getAllBeers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void insert(Beer beer);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void insertAll(Beer... beers);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract void update(Beer beer);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract void updateAll(Beer... beers);

    // upsert: https://stackoverflow.com/a/48641762/295028
//    public void upsert(Beer beer) {
//        try {
//            insert(beer);
//        } catch (SQLiteConstraintException e) {
//            update(beer);
//        }
//    }
//
//    public void upsertAll(Beer... beers) {
//        for (Beer beer : beers) {
//            try {
//                insert(beer);
//            } catch (SQLiteConstraintException e) {
//                update(beer);
//            }
//        }
//    }

//    @Delete
//    void delete(Beer beer);
//
//    @Delete
//    void deleteAll(Beer... beers);
}