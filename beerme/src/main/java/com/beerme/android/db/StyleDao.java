package com.beerme.android.db;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public abstract class StyleDao {
    @Query("SELECT * FROM style WHERE _id = :id")
    abstract LiveData<Style> getStyle(int id);

    @Query("SELECT * FROM style WHERE name LIKE :name")
    abstract LiveData<Style> getStyleByName(String name);

    @Query("SELECT * FROM style")
    abstract LiveData<List<Style>> getAllStyles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void insert(Style style);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void insertAll(Style... styles);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract void update(Style style);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract void updateAll(Style... styles);

    // upsert: https://stackoverflow.com/a/48641762/295028
//    public void upsert(Style style) {
//        try {
//            insert(style);
//        } catch (SQLiteConstraintException e) {
//            update(style);
//        }
//    }
//
//    public void upsertAll(Style... styles) {
//        for (Style style : styles) {
//            try {
//                insert(style);
//            } catch (SQLiteConstraintException e) {
//                update(style);
//            }
//        }
//    }

//    @Delete
//    void delete(Style style);
//
//    @Delete
//    void deleteAll(Style... styles);
}