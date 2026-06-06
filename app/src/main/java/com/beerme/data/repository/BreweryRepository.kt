package com.beerme.data.repository

import com.beerme.data.local.BeerDao
import com.beerme.data.local.BreweryDao
import com.beerme.data.local.TastingNoteDao
import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import com.beerme.data.model.TastingNote
import com.beerme.data.remote.BreweryApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BreweryRepository(
    private val breweryDao: BreweryDao,
    private val beerDao: BeerDao,
    private val tastingNoteDao: TastingNoteDao,
    private val apiService: BreweryApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val breweries: Flow<List<Brewery>> = breweryDao.getAllBreweries()

    suspend fun syncBreweries() {
        try {
            val dbLastUpdate = breweryDao.getLatestUpdateTimestamp()
            val lastUpdate = dbLastUpdate ?: userPreferencesRepository.lastUpdateTimestamp.first()
            val newBreweries = apiService.getBreweries(lastUpdate)
            
            if (newBreweries.isNotEmpty()) {
                breweryDao.insertBreweries(newBreweries)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncBeers() {
        try {
            val dbLastUpdate = beerDao.getLatestUpdateTimestamp()
            val lastUpdate = dbLastUpdate ?: userPreferencesRepository.beerLastUpdateTimestamp.first()
            val newBeers = apiService.getBeers(lastUpdate)
            
            if (newBeers.isNotEmpty()) {
                beerDao.insertBeers(newBeers)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBeersForBrewery(breweryId: String): Flow<List<Beer>> {
        return beerDao.getBeersByBrewery(breweryId)
    }

    suspend fun getBreweryById(id: String): Brewery? = breweryDao.getBreweryById(id)

    suspend fun getBeerById(id: String): Beer? = beerDao.getBeerById(id)

    fun getTastingNotesForBeer(beerId: String): Flow<List<TastingNote>> {
        return tastingNoteDao.getTastingNotesForBeer(beerId)
    }

    suspend fun addTastingNote(tastingNote: TastingNote) {
        tastingNoteDao.insertTastingNote(tastingNote)
    }

    suspend fun insertBeers(beers: List<Beer>) {
        beerDao.insertBeers(beers)
    }
}
