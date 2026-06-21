package com.beerme.ui.route

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beerme.data.model.Brewery
import com.beerme.data.model.BreweryService
import com.beerme.data.repository.BreweryRepository
import com.beerme.data.repository.DirectionsRepository
import com.beerme.data.repository.GeocodingRepository
import com.beerme.data.repository.PlaceResult
import com.beerme.data.repository.RouteResult
import com.beerme.data.repository.UserPreferencesRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
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
    private val directionsRepository: DirectionsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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

    // Bumped only when the camera should re-fit to the whole route (i.e. the
    // endpoints changed). Adding/removing a stop redraws the route in place
    // without yanking the camera around on every tap.
    private val _fitRequest = MutableStateFlow(0)
    val fitRequest: StateFlow<Int> = _fitRequest.asStateFlow()

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
        // The label updates instantly; debounce the re-filter so rapid +/- taps
        // only recompute candidates once the radius settles. No new routing call.
        val route = cachedDecimatedRoute ?: return
        radiusDebounceJob?.cancel()
        radiusDebounceJob = viewModelScope.launch {
            delay(RADIUS_DEBOUNCE_MS)
            // Re-fit so candidates at the new radius all stay in frame.
            filterCandidates(route, fitCamera = true)
        }
    }

    private var cachedDecimatedRoute: List<GeoPoint>? = null
    private var routeJob: Job? = null
    private var radiusDebounceJob: Job? = null

    /** Recompute from the endpoints (camera re-fits to the resulting route). */
    fun computeRoute() = fetchRoute(fitCamera = true)

    /**
     * Fetches the driving route from start through the currently-selected stops
     * (ordered along the trip) to end, then re-filters candidate breweries
     * against the new geometry. Selected stops are via-points on the route, so
     * they always survive the re-filter; detours can pull in breweries the
     * straight-line route missed and drop ones it used to pass.
     */
    private fun fetchRoute(fitCamera: Boolean) {
        val start = _start.value ?: return
        val end = _end.value ?: return
        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            _computing.value = true
            val waypoints = orderedSelectedStops().mapNotNull { b ->
                val lat = b.latitude
                val lon = b.longitude
                if (lat != null && lon != null) GeoPoint(lat, lon) else null
            }
            val result = directionsRepository.route(start.geoPoint, end.geoPoint, waypoints)
            _route.value = result
            if (result is RouteResult.Success) {
                val decimated = withContext(Dispatchers.Default) {
                    decimate(result.route.points)
                }
                cachedDecimatedRoute = decimated
                // Fit after candidates resolve so the camera can include them.
                filterCandidates(decimated, fitCamera)
            } else {
                cachedDecimatedRoute = null
                _candidates.value = emptyList()
                _selectedIds.value = emptySet()
            }
            _computing.value = false
        }
    }

    private var filterJob: Job? = null

    private fun filterCandidates(decimatedRoute: List<GeoPoint>, fitCamera: Boolean = false) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            val radiusMeters = _radiusMiles.value * METERS_PER_MILE
            // Honor the user's map status filter (Open/Planned/Closed/…). Read
            // the persisted value directly, not a WhileSubscribed StateFlow.
            val statusFilters = userPreferencesRepository.statusFilters.first()
            val near = withContext(Dispatchers.Default) {
                val visible = breweryRepository.breweriesSnapshot()
                    .filter { it.status in statusFilters }
                    // Only route to breweries the public can actually visit.
                    .filter { (it.services and BreweryService.OPEN.mask) != 0 }
                breweriesNearRoute(decimatedRoute, visible, radiusMeters)
            }
            _candidates.value = near
            // Drop selections that are no longer near the route.
            val stillValid = near.mapTo(HashSet()) { it.brewery.id }
            _selectedIds.value = _selectedIds.value.intersect(stillValid)
            // Fit now that candidates are known, so they're all in frame.
            if (fitCamera) _fitRequest.value += 1
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
        // Re-route through the new set of stops and re-filter candidates.
        fetchRoute(fitCamera = false)
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
        const val RADIUS_DEBOUNCE_MS = 400L
    }
}
