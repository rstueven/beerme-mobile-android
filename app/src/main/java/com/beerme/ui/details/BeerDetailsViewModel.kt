package com.beerme.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beerme.data.model.Beer
import com.beerme.data.model.TastingNote
import com.beerme.data.repository.BreweryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
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

    val tastingNotes: StateFlow<List<TastingNote>> = repository.getTastingNotesForBeer(beerId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
