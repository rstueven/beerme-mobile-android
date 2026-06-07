package com.beerme.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import com.beerme.data.repository.BreweryRepository
import com.beerme.data.repository.GeocodingRepository
import com.beerme.data.repository.PlaceResult
import com.beerme.data.repository.SyncPhase
import com.beerme.data.repository.UserPreferencesRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import kotlin.math.cos

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MapViewModel(
    private val breweryRepository: BreweryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val geocodingRepository: GeocodingRepository
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

    val syncPhase: StateFlow<SyncPhase> = breweryRepository.syncPhase

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    /** Breweries matching the live search query (local, near-instant). */
    val breweryResults: StateFlow<List<Brewery>> = _searchQuery
        .debounce(SEARCH_DEBOUNCE_MS)
        .mapLatest { query ->
            val trimmed = query.trim()
            if (trimmed.length < MIN_QUERY_LENGTH) emptyList()
            else breweryRepository.searchBreweries(trimmed)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Beers matching the live search query (local, near-instant). */
    val beerResults: StateFlow<List<Beer>> = _searchQuery
        .debounce(SEARCH_DEBOUNCE_MS)
        .mapLatest { query ->
            val trimmed = query.trim()
            if (trimmed.length < MIN_QUERY_LENGTH) emptyList()
            else breweryRepository.searchBeers(trimmed)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Geographic places matching the query (LocationIQ; debounced longer). */
    val placeResults: StateFlow<List<PlaceResult>> = _searchQuery
        .debounce(PLACE_DEBOUNCE_MS)
        .mapLatest { query ->
            val trimmed = query.trim()
            if (trimmed.length < MIN_QUERY_LENGTH) emptyList()
            else geocodingRepository.search(trimmed)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            breweryRepository.syncAll()
        }
    }

    /** Re-runs the full sync, e.g. after a failure surfaced via [syncPhase]. */
    fun retrySync() {
        viewModelScope.launch {
            breweryRepository.syncAll()
        }
    }

    fun toggleStatusFilter(statusCode: String) {
        viewModelScope.launch {
            val current = userPreferencesRepository.statusFilters.first()
            val updated = if (statusCode in current) current - statusCode else current + statusCode
            userPreferencesRepository.saveStatusFilters(updated)
        }
    }

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                _userLocation.value = GeoPoint(it.latitude, it.longitude)
            }
        }
    }

    /** Continuously tracks the device location so the map can follow it. */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        if (fusedLocationClient != null) return
        val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        fusedLocationClient = client

        // Seed quickly from the last known location while GPS warms up.
        client.lastLocation.addOnSuccessListener { last ->
            if (last != null && _userLocation.value == null) {
                _userLocation.value = GeoPoint(last.latitude, last.longitude)
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateDistanceMeters(10f)
            .build()
        client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    override fun onCleared() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
        fusedLocationClient = null
    }

    /**
     * Computes a bounding box centered on the user that the map can fit
     * exactly, sized so at least a handful of brewery markers are visible.
     *
     * The radius reaches the 5th-nearest brewery: the fitted viewport is a
     * tall rectangle around that circle, so the on-screen count lands a bit
     * above 5 for roughly uniform brewery density.
     */
    fun calculateTargetBox(userPoint: GeoPoint, breweries: List<Brewery>): BoundingBox? {
        val distances = breweries.mapNotNull { b ->
            if (b.latitude != null && b.longitude != null) {
                userPoint.distanceToAsDouble(GeoPoint(b.latitude, b.longitude))
            } else null
        }.sorted()
        if (distances.isEmpty()) return null

        val target = distances[minOf(4, distances.size - 1)]
        // 10% breathing room; never tighter than a 500m radius.
        val radius = maxOf(target * 1.1, 500.0)
        val dLat = radius / METERS_PER_DEGREE
        val dLon = radius /
                (METERS_PER_DEGREE * cos(Math.toRadians(userPoint.latitude))).coerceAtLeast(1.0)
        return BoundingBox(
            (userPoint.latitude + dLat).coerceAtMost(85.0),
            userPoint.longitude + dLon,
            (userPoint.latitude - dLat).coerceAtLeast(-85.0),
            userPoint.longitude - dLon
        )
    }

    private companion object {
        const val METERS_PER_DEGREE = 111_320.0
        const val MIN_QUERY_LENGTH = 2
        const val SEARCH_DEBOUNCE_MS = 200L
        const val PLACE_DEBOUNCE_MS = 350L
    }
}
