package com.beerme.ui.map

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beerme.data.model.Brewery
import com.beerme.data.repository.BreweryRepository
import com.beerme.data.repository.UserPreferencesRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel(
    private val breweryRepository: BreweryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val statusFilters: StateFlow<Set<String>> = userPreferencesRepository.statusFilters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferencesRepository.DEFAULT_STATUS_FILTERS
        )

    val breweries: StateFlow<List<Brewery>> = combine(
        breweryRepository.breweries,
        userPreferencesRepository.statusFilters
    ) { breweries, filters ->
        breweries.filter { it.status in filters }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation

    init {
        viewModelScope.launch {
            breweryRepository.syncBreweries()
            breweryRepository.syncBeers()
        }
    }

    fun toggleStatusFilter(statusCode: String) {
        viewModelScope.launch {
            val current = userPreferencesRepository.statusFilters.first()
            val updated = if (statusCode in current) current - statusCode else current + statusCode
            userPreferencesRepository.saveStatusFilters(updated)
        }
    }

    @SuppressLint("MissingPermission")
    fun requestUserLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    _userLocation.value = GeoPoint(location.latitude, location.longitude)
                } else {
                    // Fall back to the last known location (e.g. GPS not yet warm).
                    fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                        if (last != null) {
                            _userLocation.value = GeoPoint(last.latitude, last.longitude)
                        }
                    }
                }
            }
    }

    /**
     * Calculates an initial zoom level that displays between 5 and 20 breweries.
     */
    fun calculateZoom(userPoint: GeoPoint, breweries: List<Brewery>): Double {
        if (breweries.isEmpty()) return 10.0

        val distances = breweries.mapNotNull { b ->
            if (b.latitude != null && b.longitude != null) {
                userPoint.distanceToAsDouble(GeoPoint(b.latitude, b.longitude))
            } else null
        }.sorted()

        if (distances.isEmpty()) return 4.0

        // We want to show at least 5 and up to 20.
        // Let's pick the distance to the 15th closest brewery as a target for the view radius.
        val targetIndex = if (distances.size >= 15) 14 else distances.size - 1
        val radiusMeters = distances[targetIndex]

        return when {
            radiusMeters < 500 -> 17.0
            radiusMeters < 1500 -> 15.0
            radiusMeters < 5000 -> 13.0
            radiusMeters < 15000 -> 11.0
            radiusMeters < 50000 -> 9.0
            radiusMeters < 150000 -> 7.0
            else -> 4.0
        }
    }
}
