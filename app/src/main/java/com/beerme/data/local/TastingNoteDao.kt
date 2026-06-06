package com.beerme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beerme.data.model.TastingNote
import kotlinx.coroutines.flow.Flow

@Dao
interface TastingNoteDao {
    @Query("SELECT * FROM tasting_notes WHERE beerId = :beerId ORDER BY sampled DESC")
    fun getTastingNotesForBeer(beerId: String): Flow<List<TastingNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTastingNotes(tastingNotes: List<TastingNote>)

    // The note feed carries no `updated` field, so the most recent `sampled`
    // date serves as the incremental watermark. Slightly conservative
    // (edited notes re-download), but inserts are idempotent.
    @Query("SELECT MAX(sampled) FROM tasting_notes")
    suspend fun getLatestSampledTimestamp(): String?
}
