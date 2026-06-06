package com.beerme.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import com.beerme.data.repository.BreweryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class BreweryDetailsViewModel(
    private val breweryId: String,
    private val repository: BreweryRepository
) : ViewModel() {

    val brewery: StateFlow<Brewery?> = flow {
        emit(repository.getBreweryById(breweryId))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val beers: StateFlow<List<Beer>> = repository.getBeersForBrewery(breweryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
