package com.beerme.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.beerme.R
import com.beerme.data.model.BeerWithBrewery
import com.beerme.data.model.Brewery
import com.beerme.data.model.BreweryStatus
import com.beerme.data.repository.PlaceResult
import com.beerme.data.repository.SyncPhase
import com.beerme.ui.launchDirections
import com.beerme.ui.theme.BeerAmber
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.clustering.StaticCluster
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

/**
 * Below this zoom level no individual markers are shown: feeding tens of
 * thousands of world-wide markers to the clusterer re-runs its O(n²)
 * algorithm on the UI thread at every zoom change (the cause of the
 * historical OOM/ANR issues). Above it, only breweries within an expanded
 * margin around the visible region are loaded.
 */
/**
 * RadiusMarkerClusterer whose cluster markers report taps via [onClusterTap]
 * instead of opening an (empty) cluster info window. [onClusterTap] is wired up
 * after the overlay that handles the tap exists.
 */
private class BreweryClusterer(context: Context) : RadiusMarkerClusterer(context) {
    var onClusterTap: ((StaticCluster, MapView) -> Unit)? = null

    // The base class closes item popups on every re-cluster (its invalidate()
    // forces one on the next draw pass), which would dismiss the popup right
    // after the rebuild effect restores it. The rebuild effect owns the shared
    // info window's lifecycle.
    override fun hideInfoWindows() {}

    override fun buildClusterMarker(cluster: StaticCluster, mapView: MapView): Marker {
        val marker = super.buildClusterMarker(cluster, mapView)
        marker.setOnMarkerClickListener { _, mv ->
            onClusterTap?.invoke(cluster, mv)
            true
        }
        return marker
    }
}

private const val MIN_MARKER_ZOOM = 6.0

/**
 * Zoom a searched-to brewery animates to before any de-clustering. Closer
 * zooms are used only when needed to pull the brewery out of a cluster.
 */
private const val BREWERY_FOCUS_ZOOM = 15.0

/**
 * RadiusMarkerClusterer stops clustering above its mMaxClusteringZoomLevel (17,
 * left at the library default), so at this zoom every brewery renders as its own
 * pin — even ones sharing identical coordinates — while street tiles are still
 * available. Used as the upper bound for [zoomToIsolateBrewery]: there is never
 * any need to zoom in past the level where clustering is already off.
 */
private const val CLUSTER_OFF_ZOOM = 18.0

/**
 * A tapped cluster is fanned out (spiderfied) rather than zoomed into when its
 * members would still overlap within this many pixels at [CLUSTER_OFF_ZOOM] —
 * i.e. when zooming can't separate them (breweries at identical coordinates).
 * Roughly a marker-icon width.
 */
private const val CLUSTER_SPIDERFY_MAX_SPREAD_PX = 60.0

/**
 * Returns the zoom level — clamped to [BREWERY_FOCUS_ZOOM, CLUSTER_OFF_ZOOM] —
 * at which [target]'s own placemark escapes the marker clusterer, so its bubble
 * shows on the brewery itself rather than on a cluster.
 *
 * RadiusMarkerClusterer groups any markers within [CLUSTER_RADIUS_PX] screen
 * pixels of each other, so the target stands alone once its nearest neighbour is
 * farther than that. Web-mercator resolution is 156543.03·cos(lat) m/px at zoom
 * 0 and halves per level, so the threshold is
 *   100·156543·cos(lat) / 2^z < nearestMeters,
 * and we add a level of headroom so it sits comfortably clear of the radius.
 * Breweries that can't be separated this way (identical coordinates) just go to
 * [CLUSTER_OFF_ZOOM], where clustering is disabled regardless.
 */
private fun zoomToIsolateBrewery(
    target: Brewery,
    breweries: List<Brewery>,
    maxZoom: Double
): Double {
    val cap = minOf(CLUSTER_OFF_ZOOM, maxZoom)
    val lat = target.latitude ?: return BREWERY_FOCUS_ZOOM
    val lon = target.longitude ?: return BREWERY_FOCUS_ZOOM
    val latRad = Math.toRadians(lat)
    // Equirectangular metres-per-degree is accurate enough at neighbour scale.
    val mPerDegLat = 111_320.0
    val mPerDegLon = 111_320.0 * cos(latRad)
    var nearestSq = Double.MAX_VALUE
    for (b in breweries) {
        if (b.id == target.id) continue
        val bLat = b.latitude ?: continue
        val bLon = b.longitude ?: continue
        val dx = (bLon - lon) * mPerDegLon
        val dy = (bLat - lat) * mPerDegLat
        val sq = dx * dx + dy * dy
        if (sq < nearestSq) nearestSq = sq
    }
    if (nearestSq == Double.MAX_VALUE) return BREWERY_FOCUS_ZOOM   // no neighbours
    val nearestMeters = sqrt(nearestSq)
    if (nearestMeters <= 0.0) return cap                          // identical coords
    val CLUSTER_RADIUS_PX = 100.0
    val threshold = ln(CLUSTER_RADIUS_PX * 156_543.03392 * cos(latRad) / nearestMeters) / ln(2.0)
    return (threshold + 1.0).coerceIn(BREWERY_FOCUS_ZOOM, cap)
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onBreweryClick: (String) -> Unit,
    onBeerClick: (String) -> Unit,
    onPlanRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val breweries by viewModel.breweries.collectAsState()
    val statusFilters by viewModel.statusFilters.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val syncPhase by viewModel.syncPhase.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val breweryResults by viewModel.breweryResults.collectAsState()
    val beerResults by viewModel.beerResults.collectAsState()
    val placeResults by viewModel.placeResults.collectAsState()

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Visible region (bounding box + zoom), updated when panning/zooming settles.
    var viewport by remember { mutableStateOf<Pair<BoundingBox, Double>?>(null) }

    // Camera (center + zoom) persisted across navigation, so returning from a
    // detail screen restores the map exactly as it was left rather than
    // resetting to the startup view. NaN means "not set yet" — the first launch
    // falls back to the default center/zoom below.
    var savedLat by rememberSaveable { mutableStateOf(Double.NaN) }
    var savedLon by rememberSaveable { mutableStateOf(Double.NaN) }
    var savedZoom by rememberSaveable { mutableStateOf(Double.NaN) }

    // While true, the map tracks the device location. Touching the map
    // disengages it; the my-location button re-engages it. Saved across
    // navigation so a map left in the disengaged state stays put on return.
    var isFollowing by rememberSaveable { mutableStateOf(true) }

    // Set when a brewery is picked from search results: the map animates to it
    // and its bubble should open once the marker rebuild produces its marker
    // (the marker doesn't exist until the new region is in view). Cleared once
    // the bubble is shown.
    var pendingBubbleBreweryId by rememberSaveable { mutableStateOf<String?>(null) }

    @SuppressLint("ClickableViewAccessibility")
    val mapView = remember {
        createBaseMapView(context).apply {
            if (!savedLat.isNaN() && !savedLon.isNaN() && !savedZoom.isNaN()) {
                // Restore the camera from a previous visit to this screen.
                controller.setZoom(savedZoom)
                controller.setCenter(GeoPoint(savedLat, savedLon))
            } else {
                controller.setZoom(4.0)
                controller.setCenter(GeoPoint(39.8283, -98.5795))
            }
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    isFollowing = false
                }
                false // never consume; the map handles the gesture
            }
            addOnFirstLayoutListener { _, _, _, _, _ ->
                viewport = boundingBox to zoomLevelDouble
            }
            addMapListener(DelayedMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    viewport = boundingBox to zoomLevelDouble
                    return true
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    viewport = boundingBox to zoomLevelDouble
                    return true
                }
            }, 300L))
        }
    }

    // Track the camera whenever the visible region settles, so the saved
    // values are current the moment we navigate away and the screen is torn
    // down (its plain remembered MapView is rebuilt on return).
    LaunchedEffect(viewport) {
        viewport?.let { (box, zoom) ->
            savedLat = box.centerLatitude
            savedLon = box.centerLongitude
            savedZoom = zoom
        }
    }

    // One shared info window for all markers. Inflating a MarkerInfoWindow per
    // marker (35k+ breweries) was the source of the OOM/ANR issues.
    val currentOnBreweryClick by rememberUpdatedState(onBreweryClick)
    // The directions button on a bubble looks the brewery up by id at tap time,
    // so it needs the latest list (the bubble is built once and reused).
    val currentBreweries by rememberUpdatedState(breweries)
    val sharedInfoWindow = remember {
        // Custom layout (brewery_info_window) keeps the standard bonuspack view
        // IDs — so MarkerInfoWindow still auto-fills title/address/subdescription
        // — and adds a directions button handled separately below.
        object : MarkerInfoWindow(R.layout.brewery_info_window, mapView) {
            @SuppressLint("ClickableViewAccessibility")
            override fun onOpen(item: Any?) {
                super.onOpen(item)
                val marker = item as? Marker
                val breweryId = marker?.relatedObject as? String
                // Replace BasicInfoWindow's default touch listener (which only
                // closes the bubble) with navigation to the brewery details.
                // The directions button is clickable and so consumes its own
                // taps before they reach this root listener.
                mView.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        breweryId?.let(currentOnBreweryClick)
                    }
                    true
                }
                mView.findViewById<View>(R.id.bubble_directions).setOnClickListener {
                    currentBreweries.firstOrNull { it.id == breweryId }
                        ?.let { launchDirections(context, it) }
                }
            }
        }
    }

    // While a brewery bubble is open, a single tap anywhere on the map dismisses
    // it. Taps on the bubble itself are consumed by its View (see onOpen above),
    // so any tap reaching this overlay is necessarily outside the bubble. It
    // returns false, letting the same tap fall through to the clusterer below —
    // so tapping another marker closes this bubble and opens that one in one
    // gesture, while tapping empty map just closes it.
    val dismissBubbleOverlay = remember(sharedInfoWindow) {
        object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                if (sharedInfoWindow.isOpen) {
                    sharedInfoWindow.close()
                    mapView.invalidate()
                }
                return false
            }
        }
    }

    // Blue dot marking the device's current location, drawn above the
    // brewery markers and not tappable.
    val locationMarker = remember {
        val size = 48
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        canvas.drawCircle(center, center, center,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
        canvas.drawCircle(center, center, center - 6f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF4285F4.toInt() })
        Marker(mapView).apply {
            icon = BitmapDrawable(context.resources, bitmap)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setInfoWindow(null)
            setOnMarkerClickListener { _, _ -> true }
        }
    }

    // Keep the dot on the latest GPS fix; add it lazily on the first one.
    LaunchedEffect(userLocation) {
        val point = userLocation ?: return@LaunchedEffect
        locationMarker.position = point
        if (!mapView.overlays.contains(locationMarker)) {
            mapView.overlays.add(locationMarker)
        }
        mapView.invalidate()
    }

    val clusterer = remember {
        BreweryClusterer(context).apply {
            // RadiusMarkerClusterer requires a cluster icon before first draw.
            val size = 96
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BeerAmber.toArgb() }
            Canvas(bitmap).drawCircle(size / 2f, size / 2f, size / 2f, paint)
            setIcon(bitmap)
        }
    }

    // Spiderfy stacked pins: above the clustering-off zoom, breweries sharing a
    // location draw on top of each other and a tap can only reach the topmost.
    // This overlay fans them out so each is individually selectable.
    val spiderfyOverlay = remember(mapView, clusterer) {
        SpiderfyOverlay(
            density = context.resources.displayMetrics.density,
            findStack = { p ->
                // Only meaningful once clustering is off; below that a tap on a
                // cluster belongs to the clusterer (which zooms in).
                if (mapView.zoomLevelDouble < CLUSTER_OFF_ZOOM) {
                    emptyList()
                } else {
                    val proj = mapView.projection
                    val tip = Point()
                    clusterer.items.filter { m ->
                        proj.toPixels(m.position, tip)
                        val w = m.icon?.intrinsicWidth ?: 0
                        val h = m.icon?.intrinsicHeight ?: 0
                        p.x >= tip.x - w / 2 && p.x <= tip.x + w / 2 &&
                            p.y >= tip.y - h && p.y <= tip.y
                    }
                }
            },
            onLeafChosen = { marker -> marker.showInfoWindow() }
        )
    }

    DisposableEffect(mapView, spiderfyOverlay) {
        // Topmost overlay so it intercepts taps before the clusterer.
        mapView.overlays.add(spiderfyOverlay)
        // Tapping a cluster: fan it out if its members would still overlap when
        // fully zoomed in (identical coordinates), otherwise zoom in to expand
        // it. The default does nothing useful (an empty cluster info window).
        clusterer.onClusterTap = onClusterTap@{ cluster, mv ->
            val box = cluster.boundingBox
            val mPerPx = 156_543.03392 *
                cos(Math.toRadians(cluster.position.latitude)) /
                (1 shl CLUSTER_OFF_ZOOM.toInt())
            val spreadPx = box.diagonalLengthInMeters / mPerPx
            if (spreadPx < CLUSTER_SPIDERFY_MAX_SPREAD_PX) {
                val members = (0 until cluster.size).map { cluster.getItem(it) }
                spiderfyOverlay.spiderfy(members, mv)
            } else {
                mv.zoomToBoundingBox(box, true, 100, CLUSTER_OFF_ZOOM, 600L)
            }
        }
        // Any pan/zoom dismisses the fan immediately (the 300ms-delayed viewport
        // listener would let it linger through a drag).
        val collapseOnMove = object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                if (spiderfyOverlay.isActive) {
                    spiderfyOverlay.collapse()
                    mapView.invalidate()
                }
                return false
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                if (spiderfyOverlay.isActive) {
                    spiderfyOverlay.collapse()
                    mapView.invalidate()
                }
                return false
            }
        }
        mapView.addMapListener(collapseOnMove)
        onDispose {
            clusterer.onClusterTap = null
            mapView.removeMapListener(collapseOnMove)
            mapView.overlays.remove(spiderfyOverlay)
        }
    }

    DisposableEffect(mapView, dismissBubbleOverlay) {
        mapView.overlays.add(dismissBubbleOverlay)
        onDispose { mapView.overlays.remove(dismissBubbleOverlay) }
    }

    // On the first fix, fit the view to the breweries nearest the user
    // (re-run once breweries arrive). On subsequent fixes the map follows
    // the location, keeping whatever zoom level is selected, for as long as
    // follow mode is engaged.
    // Saved across navigation so the one-time fit-to-nearest-breweries zoom
    // isn't re-run when returning to an already-positioned map.
    var initialZoomDone by rememberSaveable { mutableStateOf(false) }
    val hasBreweries = breweries.isNotEmpty()
    val isLaidOut = viewport != null
    LaunchedEffect(userLocation, hasBreweries, isFollowing, isLaidOut) {
        if (!isFollowing) return@LaunchedEffect
        val point = userLocation ?: return@LaunchedEffect
        if (!initialZoomDone) {
            // zoomToBoundingBox needs a measured view; wait for first layout.
            if (!isLaidOut) return@LaunchedEffect
            val box = viewModel.calculateTargetBox(point, breweries)
            if (box != null) {
                // Kill any in-flight animation, then jump (not animate): an
                // animated fit races against the next GPS fix, whose
                // follow-pan would freeze the zoom mid-flight.
                mapView.controller.stopAnimation(false)
                mapView.zoomToBoundingBox(box, false, 64)
                initialZoomDone = true
                // The non-animated jump doesn't reliably emit scroll/zoom
                // events, so refresh the viewport by hand or the markers
                // never load on a stationary device.
                viewport = mapView.boundingBox to mapView.zoomLevelDouble
            } else {
                // Breweries not yet synced: center on the user meanwhile.
                mapView.controller.animateTo(point, 10.0, 1000L)
            }
        } else {
            mapView.controller.animateTo(point, mapView.zoomLevelDouble, 600L)
        }
    }

    // Build markers for the breweries in (a margin around) the visible region,
    // off the main thread.
    LaunchedEffect(breweries, viewport) {
        val currentViewport = viewport ?: return@LaunchedEffect
        // The rebuild replaces the marker instances the fan holds references to.
        if (spiderfyOverlay.isActive) spiderfyOverlay.collapse()
        val markers = withContext(Dispatchers.Default) {
            val (box, zoom) = currentViewport
            if (zoom < MIN_MARKER_ZOOM) {
                emptyList()
            } else {
                val expanded = box.increaseByScale(1.5f)
                breweries.mapNotNull { brewery ->
                    val lat = brewery.latitude
                    val lon = brewery.longitude
                    if (lat != null && lon != null && expanded.contains(lat, lon)) {
                        Marker(mapView).apply {
                            position = GeoPoint(lat, lon)
                            title = brewery.name
                            snippet = brewery.address
                            // Show the status for any non-open brewery; for open
                            // ones show the hours instead (hours are meaningless
                            // for a planned/defunct/closed brewery).
                            val statusLabel = BreweryStatus.entries
                                .firstOrNull { it.code == brewery.status }
                                ?.takeUnless { it == BreweryStatus.OPEN }
                                ?.label
                            subDescription = statusLabel ?: brewery.hours
                            relatedObject = brewery.id
                            infoWindow = sharedInfoWindow
                        }
                    } else null
                }
            }
        }

        // The rebuild discards the marker an open popup is anchored to;
        // remember which brewery it was showing so it can be re-opened on
        // that brewery's replacement marker.
        val openBreweryId = if (sharedInfoWindow.isOpen) {
            (sharedInfoWindow.relatedObject as? Marker)?.relatedObject as? String
        } else null
        sharedInfoWindow.close()
        clusterer.items.clear()
        clusterer.items.addAll(markers)
        clusterer.invalidate()
        if (!mapView.overlays.contains(clusterer)) {
            // Insert below the location dot so the dot stays visible.
            mapView.overlays.add(0, clusterer)
        }
        openBreweryId?.let { id ->
            markers.firstOrNull { it.relatedObject == id }?.showInfoWindow()
        }
        // A brewery just picked from search: open its bubble as soon as its
        // marker exists, then stop waiting.
        pendingBubbleBreweryId?.let { id ->
            markers.firstOrNull { it.relatedObject == id }?.let { marker ->
                marker.showInfoWindow()
                pendingBubbleBreweryId = null
            }
        }
        mapView.invalidate()
    }

    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Ask for location permission on first composition; start following the
    // device location as soon as it is granted.
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.startLocationUpdates(context)
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var searchExpanded by rememberSaveable { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Disable the edge-swipe gesture so horizontal map panning reaches the
        // MapView instead of opening the drawer. The drawer still opens via the
        // hamburger button in the search bar.
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawerContent(
                statusFilters = statusFilters,
                onToggleStatus = { viewModel.toggleStatusFilter(it) },
                onPlanRoute = {
                    scope.launch { drawerState.close() }
                    onPlanRoute()
                }
            )
        }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapView }
            )

            // Search bar pinned to the top; its leading icon doubles as the
            // hamburger-menu entry point when collapsed.
            SearchBar(
                modifier = Modifier.align(Alignment.TopCenter),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onSearch = {},
                        expanded = searchExpanded,
                        onExpandedChange = { searchExpanded = it },
                        placeholder = { Text("Search breweries, beers, places") },
                        leadingIcon = {
                            if (searchExpanded) {
                                IconButton(onClick = {
                                    searchExpanded = false
                                    viewModel.clearSearch()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Close search"
                                    )
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Open menu"
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            when {
                                searchQuery.isNotEmpty() -> IconButton(
                                    onClick = { viewModel.clearSearch() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear search"
                                    )
                                }

                                !searchExpanded -> Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
                expanded = searchExpanded,
                onExpandedChange = { searchExpanded = it }
            ) {
                SearchResults(
                    query = searchQuery,
                    breweries = breweryResults,
                    beers = beerResults,
                    places = placeResults,
                    onBrewerySelected = { brewery ->
                        searchExpanded = false
                        viewModel.clearSearch()
                        if (brewery.latitude != null && brewery.longitude != null) {
                            // Stop following so the next GPS fix doesn't yank the
                            // map away from the brewery the user just chose.
                            isFollowing = false
                            // Zoom close enough that the brewery isn't swallowed
                            // by a cluster, so its own bubble can show.
                            val targetZoom = zoomToIsolateBrewery(
                                brewery, breweries, mapView.maxZoomLevel
                            )
                            mapView.controller.animateTo(
                                GeoPoint(brewery.latitude, brewery.longitude),
                                targetZoom, 1000L
                            )
                            // Open its bubble once the marker rebuild reaches it.
                            pendingBubbleBreweryId = brewery.id
                        }
                    },
                    onBeerSelected = { id ->
                        searchExpanded = false
                        viewModel.clearSearch()
                        onBeerClick(id)
                    },
                    onPlaceSelected = { place ->
                        searchExpanded = false
                        viewModel.clearSearch()
                        // Stop following so the next GPS fix doesn't yank the
                        // map away from the place the user just chose.
                        isFollowing = false
                        mapView.controller.animateTo(
                            GeoPoint(place.latitude, place.longitude), 13.0, 1000L
                        )
                    }
                )
            }

            // Sync status banner, tucked just below the collapsed search bar.
            if (!searchExpanded && syncPhase != SyncPhase.Idle) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 72.dp)
                ) {
                    SyncStatusBanner(
                        phase = syncPhase,
                        isInitialLoad = breweries.isEmpty(),
                        onRetry = { viewModel.retrySync() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            FloatingActionButton(
                onClick = { isFollowing = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp),
                containerColor = if (isFollowing) {
                    // Full primary (brown on cream theme) so the engaged state
                    // is unmistakable against the surface-colored idle state.
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ) {
                Icon(
                    imageVector = if (isFollowing) {
                        Icons.Filled.MyLocation
                    } else {
                        Icons.Filled.LocationSearching
                    },
                    contentDescription = "Return to my location"
                )
            }
        }
    }
}

/**
 * Hamburger-menu drawer. The "Settings" item expands into a status-filter
 * submenu that toggles which brewery statuses are shown on the map.
 */
@Composable
private fun AppDrawerContent(
    statusFilters: Set<String>,
    onToggleStatus: (String) -> Unit,
    onPlanRoute: () -> Unit
) {
    var settingsExpanded by rememberSaveable { mutableStateOf(false) }

    ModalDrawerSheet {
        Text(
            text = "BeerMe",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
        )
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        NavigationDrawerItem(
            label = { Text("Plan a Route") },
            icon = { Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null) },
            selected = false,
            onClick = onPlanRoute,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("Settings") },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            badge = {
                Icon(
                    imageVector = if (settingsExpanded) {
                        Icons.Filled.ExpandLess
                    } else {
                        Icons.Filled.ExpandMore
                    },
                    contentDescription = null
                )
            },
            selected = false,
            onClick = { settingsExpanded = !settingsExpanded },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        if (settingsExpanded) {
            Text(
                text = "Status Filter",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 4.dp)
            )
            BreweryStatus.entries.forEach { status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleStatus(status.code) }
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = status.code in statusFilters,
                        onCheckedChange = { onToggleStatus(status.code) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = status.label)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Results shown while the search bar is expanded, split across Breweries,
 * Beers, and Locations tabs so each list is browsable without scrolling past
 * the others. Tab labels carry their result counts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResults(
    query: String,
    breweries: List<Brewery>,
    beers: List<BeerWithBrewery>,
    places: List<PlaceResult>,
    onBrewerySelected: (Brewery) -> Unit,
    onBeerSelected: (String) -> Unit,
    onPlaceSelected: (PlaceResult) -> Unit
) {
    val trimmed = query.trim()
    if (trimmed.length < 2) {
        SearchHint("Search for breweries, beers, or places")
        return
    }

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var userPickedTab by rememberSaveable { mutableStateOf(false) }
    // A new query reopens auto-selection of the first populated tab.
    LaunchedEffect(trimmed) { userPickedTab = false }
    LaunchedEffect(breweries.size, beers.size, places.size, userPickedTab) {
        if (!userPickedTab) {
            val firstPopulated = listOf(breweries.size, beers.size, places.size)
                .indexOfFirst { it > 0 }
            if (firstPopulated >= 0) selectedTab = firstPopulated
        }
    }

    val titles = listOf("Breweries", "Beers", "Locations")
    val counts = listOf(breweries.size, beers.size, places.size)

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        userPickedTab = true
                    },
                    text = {
                        val count = counts[index]
                        Text(if (count > 0) "$title ($count)" else title)
                    }
                )
            }
        }
        when (selectedTab) {
            0 -> ResultList(
                items = breweries,
                emptyText = "No breweries match “$trimmed”",
                itemKey = { it.id },
                icon = Icons.Filled.Storefront,
                title = { it.name },
                subtitle = { it.address },
                onClick = { onBrewerySelected(it) },
                trailingContent = { brewery -> BreweryStatusLabel(brewery.status) }
            )
            1 -> ResultList(
                items = beers,
                emptyText = "No beers match “$trimmed”",
                itemKey = { it.beer.id },
                icon = Icons.Filled.SportsBar,
                title = { it.beer.name },
                // Brewery name disambiguates same-named beers (e.g. "IPA").
                subtitle = {
                    listOfNotNull(it.breweryName, it.beer.style)
                        .joinToString(" · ")
                        .ifEmpty { null }
                },
                onClick = { onBeerSelected(it.beer.id) }
            )
            else -> ResultList(
                items = places,
                emptyText = "No locations match “$trimmed”",
                itemKey = null,
                icon = Icons.Filled.Place,
                title = { it.name },
                subtitle = { it.address },
                onClick = { onPlaceSelected(it) }
            )
        }
    }
}

/** A single search tab's scrollable result list (or an empty-state hint). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ResultList(
    items: List<T>,
    emptyText: String,
    itemKey: ((T) -> Any)?,
    icon: ImageVector,
    title: (T) -> String,
    subtitle: (T) -> String?,
    onClick: (T) -> Unit,
    trailingContent: (@Composable (T) -> Unit)? = null
) {
    if (items.isEmpty()) {
        SearchHint(emptyText)
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items, key = itemKey) { item ->
            ListItem(
                headlineContent = { Text(title(item)) },
                supportingContent = subtitle(item)?.let {
                    { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                },
                leadingContent = { Icon(icon, contentDescription = null) },
                trailingContent = trailingContent?.let { { it(item) } },
                modifier = Modifier.clickable { onClick(item) }
            )
        }
    }
}

/**
 * The brewery's status as a trailing label, shown only when it is not "Open"
 * (the common case needs no annotation). Closed breweries are flagged in the
 * error colour.
 */
@Composable
private fun BreweryStatusLabel(statusCode: String?) {
    val status = BreweryStatus.entries.firstOrNull { it.code == statusCode }
    if (status == null || status == BreweryStatus.OPEN) return
    Text(
        text = status.label,
        style = MaterialTheme.typography.labelMedium,
        color = if (status == BreweryStatus.CLOSED) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
}

@Composable
private fun SearchHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SyncStatusBanner(
    phase: SyncPhase,
    isInitialLoad: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val error = phase as? SyncPhase.Error
    val containerColor = if (error != null) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (error != null) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (error != null) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (phase) {
                        SyncPhase.Breweries -> "Updating breweries…"
                        SyncPhase.Beers -> "Updating beers…"
                        SyncPhase.TastingNotes -> "Updating tasting notes…"
                        is SyncPhase.Error -> "Couldn't update ${phase.dataset}"
                        SyncPhase.Idle -> ""
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor
                )
                if (error?.message != null) {
                    Text(
                        text = error.message,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor
                    )
                } else if (isInitialLoad && error == null) {
                    Text(
                        text = "First download may take a minute",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor
                    )
                }
            }
            if (error != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}
