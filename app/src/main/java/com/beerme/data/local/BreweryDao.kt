package com.beerme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beerme.data.model.Brewery
import kotlinx.coroutines.flow.Flow

@Dao
interface BreweryDao {
    @Query("SELECT * FROM breweries")
    fun getAllBreweries(): Flow<List<Brewery>>

    @Query("SELECT * FROM breweries WHERE id = :id")
    suspend fun getBreweryById(id: String): Brewery?

    // Ranked by relevance: earliest match position first (so prefix/exact
    // matches beat interior ones), then shorter names (favouring exact
    // matches), then alphabetically.
    @Query(
        "SELECT * FROM breweries WHERE name LIKE '%' || :query || '%' " +
            "ORDER BY INSTR(LOWER(name), LOWER(:query)), LENGTH(name), " +
            "name COLLATE NOCASE ASC LIMIT :limit"
    )
    suspend fun searchByName(query: String, limit: Int): List<Brewery>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreweries(breweries: List<Brewery>)

    @Query("SELECT MAX(updated) FROM breweries")
    suspend fun getLatestUpdateTimestamp(): String?

    @Query("DELETE FROM breweries")
    suspend fun deleteAll()
}
