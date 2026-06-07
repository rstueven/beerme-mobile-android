package com.beerme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beerme.data.model.Beer
import com.beerme.data.model.BeerWithBrewery
import kotlinx.coroutines.flow.Flow

@Dao
interface BeerDao {
    @Query("SELECT * FROM beers WHERE breweryId = :breweryId ORDER BY name COLLATE NOCASE ASC")
    fun getBeersByBrewery(breweryId: String): Flow<List<Beer>>

    @Query("SELECT * FROM beers WHERE id = :id")
    suspend fun getBeerById(id: String): Beer?

    // Joins in the brewery name (LEFT JOIN: a beer may reference a brewery not
    // yet synced). Ranked by relevance: earliest match position first (so
    // prefix/exact matches beat interior ones), then shorter names (favouring
    // exact matches), then alphabetically.
    @Query(
        "SELECT beers.*, breweries.name AS breweryName " +
            "FROM beers LEFT JOIN breweries ON beers.breweryId = breweries.id " +
            "WHERE beers.name LIKE '%' || :query || '%' " +
            "ORDER BY INSTR(LOWER(beers.name), LOWER(:query)), LENGTH(beers.name), " +
            "beers.name COLLATE NOCASE ASC LIMIT :limit"
    )
    suspend fun searchByName(query: String, limit: Int): List<BeerWithBrewery>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeers(beers: List<Beer>)

    @Query("SELECT MAX(updated) FROM beers")
    suspend fun getLatestUpdateTimestamp(): String?
}
