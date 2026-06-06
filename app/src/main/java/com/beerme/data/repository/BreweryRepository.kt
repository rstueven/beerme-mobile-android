package com.beerme.data.repository

import com.beerme.data.local.BeerDao
import com.beerme.data.local.BreweryDao
import com.beerme.data.local.TastingNoteDao
import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import com.beerme.data.model.TastingNote
import com.beerme.data.remote.BreweryApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

/** Which dataset a running sync is currently downloading. */
enum class SyncPhase { IDLE, BREWERIES, BEERS, TASTING_NOTES }

class BreweryRepository(
    private val breweryDao: BreweryDao,
    private val beerDao: BeerDao,
    private val tastingNoteDao: TastingNoteDao,
    private val apiService: BreweryApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val breweries: Flow<List<Brewery>> = breweryDao.getAllBreweries()

    private val _syncPhase = MutableStateFlow(SyncPhase.IDLE)
    val syncPhase: StateFlow<SyncPhase> = _syncPhase

    /** Refreshes all three datasets, reporting progress via [syncPhase]. */
    suspend fun syncAll() {
        try {
            _syncPhase.value = SyncPhase.BREWERIES
            syncBreweries()
            _syncPhase.value = SyncPhase.BEERS
            syncBeers()
            _syncPhase.value = SyncPhase.TASTING_NOTES
            syncTastingNotes()
        } finally {
            _syncPhase.value = SyncPhase.IDLE
        }
    }

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

    suspend fun syncTastingNotes() {
        try {
            val lastSampled = tastingNoteDao.getLatestSampledTimestamp()
            val newNotes = apiService.getBeerNotes(lastSampled)

            if (newNotes.isNotEmpty()) {
                tastingNoteDao.insertTastingNotes(newNotes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTastingNotesForBeer(beerId: String): Flow<List<TastingNote>> {
        return tastingNoteDao.getTastingNotesForBeer(beerId)
    }

    suspend fun insertBeers(beers: List<Beer>) {
        beerDao.insertBeers(beers)
    }
}
