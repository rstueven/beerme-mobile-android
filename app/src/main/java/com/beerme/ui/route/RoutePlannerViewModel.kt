package com.beerme.ui.route

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beerme.data.model.Brewery
import com.beerme.data.repository.BreweryRepository
import com.beerme.data.repository.DirectionsRepository
import com.beerme.data.repository.GeocodingRepository
import com.beerme.data.repository.PlaceResult
import com.beerme.data.repository.RouteResult
import com.beerme.util.METERS_PER_MILE
import com.beerme.util.RouteBrewery
import com.beerme.util.breweriesNearRoute
import com.beerme.util.decimate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.osmdroid.util.GeoPoint

/** A trip endpoint, regardless of whether it came from a place, GPS, or a brewery. */
data class Endpoint(val lat: Double, val lon: Double, val label: String) {
    val geoPoint: GeoPoint get() = GeoPoint(lat, lon)
}

/** Which endpoint a picker is choosing for. */
enum class EndpointField { START, END }

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RoutePlannerViewModel(
    private val breweryRepository: BreweryRepository,
    private val geocodingRepository: GeocodingRepository,
    private val directionsRepository: DirectionsRepository
) : ViewModel() {

    private val _start = MutableStateFlow<Endpoint?>(null)
    val start: StateFlow<Endpoint?> = _start.asStateFlow()

    private val _end = MutableStateFlow<Endpoint?>(null)
    val end: StateFlow<Endpoint?> = _end.asStateFlow()

    private val _radiusMiles = MutableStateFlow(DEFAULT_RADIUS_MILES)
    val radiusMiles: StateFlow<Int> = _radiusMiles.asStateFlow()

    private val _route = MutableStateFlow<RouteResult?>(null)
    val route: StateFlow<RouteResult?> = _route.asStateFlow()

    private val _candidates = MutableStateFlow<List<RouteBrewery>>(emptyList())
    val candidates: StateFlow<List<RouteBrewery>> = _candidates.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    private val _computing = MutableStateFlow(false)
    val computing: StateFlow<Boolean> = _computing.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    // ---- Endpoint picker -------------------------------------------------

    private val _pickerQuery = MutableStateFlow("")
    val pickerQuery: StateFlow<String> = _pickerQuery.asStateFlow()

    /** Geographic places matching the picker query (LocationIQ). */
    val placeResults: StateFlow<List<PlaceResult>> = _pickerQuery
        .debounce(PLACE_DEBOUNCE_MS)
        .mapLatest { query ->
            val trimmed = query.trim()
            if (trimmed.length < MIN_QUERY_LENGTH) emptyList()
            else geocodingRepository.search(trimmed)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Breweries matching the picker query (local, with coordinates). */
    val breweryResults: StateFlow<List<Brewery>> = _pickerQuery
        .debounce(SEARCH_DEBOUNCE_MS)
        .mapLatest { query ->
            val trimmed = query.trim()
            if (trimmed.length < MIN_QUERY_LENGTH) emptyList()
            else breweryRepository.searchBreweries(trimmed)
                .filter { it.latitude != null && it.longitude != null }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPickerQueryChange(query: String) {
        _pickerQuery.value = query
    }

    fun clearPicker() {
        _pickerQuery.value = ""
    }

    // ---- Endpoints & routing --------------------------------------------

    fun setEndpoint(field: EndpointField, endpoint: Endpoint) {
        when (field) {
            EndpointField.START -> _start.value = endpoint
            EndpointField.END -> _end.value = endpoint
        }
        clearPicker()
        if (_start.value != null && _end.value != null) computeRoute()
    }

    fun setRadiusMiles(miles: Int) {
        _radiusMiles.value = miles.coerceIn(MIN_RADIUS_MILES, MAX_RADIUS_MILES)
        // Re-filter against the cached polyline; no new routing call.
        cachedDecimatedRoute?.let { filterCandidates(it) }
    }

    private var cachedDecimatedRoute: List<GeoPoint>? = null
    private var routeJob: Job? = null

    fun computeRoute() {
        val start = _start.value ?: return
        val end = _end.value ?: return
        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            _computing.value = true
            val result = directionsRepository.route(start.geoPoint, end.geoPoint)
            _route.value = result
            if (result is RouteResult.Success) {
                val decimated = withContext(Dispatchers.Default) {
                    decimate(result.route.points)
                }
                cachedDecimatedRoute = decimated
                filterCandidates(decimated)
            } else {
                cachedDecimatedRoute = null
                _candidates.value = emptyList()
                _selectedIds.value = emptySet()
            }
            _computing.value = false
        }
    }

    private var filterJob: Job? = null

    private fun filterCandidates(decimatedRoute: List<GeoPoint>) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            val radiusMeters = _radiusMiles.value * METERS_PER_MILE
            val near = withContext(Dispatchers.Default) {
                breweriesNearRoute(decimatedRoute, breweryRepository.breweriesSnapshot(), radiusMeters)
            }
            _candidates.value = near
            // Drop selections that are no longer near the route.
            val stillValid = near.mapTo(HashSet()) { it.brewery.id }
            _selectedIds.value = _selectedIds.value.intersect(stillValid)
        }
    }

    fun toggleSelection(breweryId: String) {
        val current = _selectedIds.value
        if (breweryId in current) {
            _selectedIds.value = current - breweryId
        } else {
            if (current.size >= MAX_STOPS) {
                _userMessage.value = "You can add up to $MAX_STOPS stops"
                return
            }
            _selectedIds.value = current + breweryId
        }
    }

    /** Selected breweries, ordered by their position along the route. */
    fun orderedSelectedStops(): List<Brewery> {
        val selected = _selectedIds.value
        return _candidates.value
            .filter { it.brewery.id in selected }
            .map { it.brewery }
    }

    fun consumeUserMessage() {
        _userMessage.value = null
    }

    // ---- Current location ------------------------------------------------

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                _userLocation.value = GeoPoint(it.latitude, it.longitude)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        if (fusedLocationClient != null) return
        val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        fusedLocationClient = client
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

    private companion object {
        const val DEFAULT_RADIUS_MILES = 10
        const val MIN_RADIUS_MILES = 1
        const val MAX_RADIUS_MILES = 50
        const val MAX_STOPS = 9
        const val MIN_QUERY_LENGTH = 2
        const val SEARCH_DEBOUNCE_MS = 200L
        const val PLACE_DEBOUNCE_MS = 350L
    }
}
