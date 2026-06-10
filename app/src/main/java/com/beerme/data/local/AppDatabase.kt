package com.beerme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import com.beerme.data.model.TastingNote

@Database(entities = [Brewery::class, Beer::class, TastingNote::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun breweryDao(): BreweryDao
    abstract fun beerDao(): BeerDao
    abstract fun tastingNoteDao(): TastingNoteDao
}
