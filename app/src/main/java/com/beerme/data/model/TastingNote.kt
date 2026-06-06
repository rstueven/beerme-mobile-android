package com.beerme.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Locally-authored tasting notes. No DB foreign key to Beer: beer sync uses
 * OnConflictStrategy.REPLACE (delete + insert), which would cascade-delete
 * the user's notes on every incremental update.
 */
@Entity(
    tableName = "tasting_notes",
    indices = [Index(value = ["beerId"])]
)
data class TastingNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val beerId: String,
    val note: String,
    val rating: Int,
    val user: String
)
