package com.beerme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beerme.data.model.Beer
import kotlinx.coroutines.flow.Flow

@Dao
interface BeerDao {
    @Query("SELECT * FROM beers WHERE breweryId = :breweryId ORDER BY name COLLATE NOCASE ASC")
    fun getBeersByBrewery(breweryId: String): Flow<List<Beer>>

    @Query("SELECT * FROM beers WHERE id = :id")
    suspend fun getBeerById(id: String): Beer?

    @Query(
        "SELECT * FROM beers WHERE name LIKE '%' || :query || '%' " +
            "ORDER BY name COLLATE NOCASE ASC LIMIT :limit"
    )
    suspend fun searchByName(query: String, limit: Int): List<Beer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeers(beers: List<Beer>)

    @Query("SELECT MAX(updated) FROM beers")
    suspend fun getLatestUpdateTimestamp(): String?
}
