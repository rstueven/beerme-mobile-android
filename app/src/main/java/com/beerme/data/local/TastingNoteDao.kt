package com.beerme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beerme.data.model.TastingNote
import kotlinx.coroutines.flow.Flow

@Dao
interface TastingNoteDao {
    @Query("SELECT * FROM tasting_notes WHERE beerId = :beerId")
    fun getTastingNotesForBeer(beerId: String): Flow<List<TastingNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTastingNote(tastingNote: TastingNote)
}
