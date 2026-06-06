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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreweries(breweries: List<Brewery>)

    @Query("SELECT MAX(updated) FROM breweries")
    suspend fun getLatestUpdateTimestamp(): String?

    @Query("DELETE FROM breweries")
    suspend fun deleteAll()
}
