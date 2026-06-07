package com.beerme.data.repository

import com.beerme.data.local.BeerDao
import com.beerme.data.local.BreweryDao
import com.beerme.data.local.TastingNoteDao
import com.beerme.data.model.Beer
import com.beerme.data.model.BeerWithBrewery
import com.beerme.data.model.Brewery
import com.beerme.data.model.TastingNote
import com.beerme.data.remote.BreweryApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

/** Which dataset a running sync is currently downloading, or how it failed. */
sealed interface SyncPhase {
    data object Idle : SyncPhase
    data object Breweries : SyncPhase
    data object Beers : SyncPhase
    data object TastingNotes : SyncPhase

    /**
     * A sync stopped because [dataset] (a human-readable name) failed.
     * [message] is the underlying error text, if any.
     */
    data class Error(val dataset: String, val message: String?) : SyncPhase
}

class BreweryRepository(
    private val breweryDao: BreweryDao,
    private val beerDao: BeerDao,
    private val tastingNoteDao: TastingNoteDao,
    private val apiService: BreweryApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val breweries: Flow<List<Brewery>> = breweryDao.getAllBreweries()

    private val _syncPhase = MutableStateFlow<SyncPhase>(SyncPhase.Idle)
    val syncPhase: StateFlow<SyncPhase> = _syncPhase

    /**
     * Refreshes all three datasets in order, reporting progress via [syncPhase].
     * If a dataset fails, the sync stops and [syncPhase] settles on
     * [SyncPhase.Error] naming the dataset that failed (rather than silently
     * falling back to [SyncPhase.Idle], which is indistinguishable from success).
     */
    suspend fun syncAll() {
        try {
            _syncPhase.value = SyncPhase.Breweries
            syncBreweries()
            _syncPhase.value = SyncPhase.Beers
            syncBeers()
            _syncPhase.value = SyncPhase.TastingNotes
            syncTastingNotes()
            _syncPhase.value = SyncPhase.Idle
        } catch (e: CancellationException) {
            // Scope cancelled (e.g. screen left mid-sync): not a failure, and
            // rethrowing keeps structured concurrency intact.
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            val dataset = when (_syncPhase.value) {
                SyncPhase.Breweries -> "breweries"
                SyncPhase.Beers -> "beers"
                SyncPhase.TastingNotes -> "tasting notes"
                else -> "data"
            }
            _syncPhase.value = SyncPhase.Error(dataset, e.message)
        }
    }

    suspend fun syncBreweries() {
        val dbLastUpdate = breweryDao.getLatestUpdateTimestamp()
        val lastUpdate = dbLastUpdate ?: userPreferencesRepository.lastUpdateTimestamp.first()
        val newBreweries = apiService.getBreweries(lastUpdate)

        if (newBreweries.isNotEmpty()) {
            breweryDao.insertBreweries(newBreweries)
        }
    }

    suspend fun syncBeers() {
        val dbLastUpdate = beerDao.getLatestUpdateTimestamp()
        val lastUpdate = dbLastUpdate ?: userPreferencesRepository.beerLastUpdateTimestamp.first()
        val newBeers = apiService.getBeers(lastUpdate)

        if (newBeers.isNotEmpty()) {
            beerDao.insertBeers(newBeers)
        }
    }

    fun getBeersForBrewery(breweryId: String): Flow<List<Beer>> {
        return beerDao.getBeersByBrewery(breweryId)
    }

    suspend fun getBreweryById(id: String): Brewery? = breweryDao.getBreweryById(id)

    suspend fun getBeerById(id: String): Beer? = beerDao.getBeerById(id)

    /** Breweries whose name contains [query] (case-insensitive substring). */
    suspend fun searchBreweries(query: String, limit: Int = SEARCH_LIMIT): List<Brewery> =
        breweryDao.searchByName(query, limit)

    /**
     * Beers whose name contains [query] (case-insensitive substring), each
     * paired with its brewery's name.
     */
    suspend fun searchBeers(query: String, limit: Int = SEARCH_LIMIT): List<BeerWithBrewery> =
        beerDao.searchByName(query, limit)

    suspend fun syncTastingNotes() {
        val lastSampled = tastingNoteDao.getLatestSampledTimestamp()
        val newNotes = apiService.getBeerNotes(lastSampled)

        if (newNotes.isNotEmpty()) {
            tastingNoteDao.insertTastingNotes(newNotes)
        }
    }

    fun getTastingNotesForBeer(beerId: String): Flow<List<TastingNote>> {
        return tastingNoteDao.getTastingNotesForBeer(beerId)
    }

    suspend fun insertBeers(beers: List<Beer>) {
        beerDao.insertBeers(beers)
    }

    private companion object {
        /** Cap on rows returned per search type, to keep the result list tidy. */
        const val SEARCH_LIMIT = 8
    }
}
