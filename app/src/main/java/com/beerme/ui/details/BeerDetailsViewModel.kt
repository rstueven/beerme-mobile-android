package com.beerme.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beerme.data.model.Beer
import com.beerme.data.model.TastingNote
import com.beerme.data.repository.BreweryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BeerDetailsViewModel(
    private val beerId: String,
    private val repository: BreweryRepository
) : ViewModel() {

    val beer: StateFlow<Beer?> = flow {
        emit(repository.getBeerById(beerId))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Name of the brewery that makes this beer (Beer only carries the id), shown
    // beneath the beer name. Null if the brewery isn't in the local database.
    val breweryName: StateFlow<String?> = beer.map { b ->
        b?.breweryId?.let { repository.getBreweryById(it)?.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val tastingNotes: StateFlow<List<TastingNote>> = repository.getTastingNotesForBeer(beerId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
