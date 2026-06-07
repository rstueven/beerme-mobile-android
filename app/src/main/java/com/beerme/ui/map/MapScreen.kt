package com.beerme.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.beerme.BuildConfig
import com.beerme.data.model.BreweryStatus
import com.beerme.data.repository.SyncPhase
import com.beerme.ui.theme.BeerAmber
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

/**
 * Below this zoom level no individual markers are shown: feeding tens of
 * thousands of world-wide markers to the clusterer re-runs its O(n²)
 * algorithm on the UI thread at every zoom change (the cause of the
 * historical OOM/ANR issues). Above it, only breweries within an expanded
 * margin around the visible region are loaded.
 */
private const val MIN_MARKER_ZOOM = 6.0

/**
 * LocationIQ street tiles when an API key is configured (locationiq.apiKey in
 * local.properties), otherwise the default OpenStreetMap tile server.
 */
private val tileSource = if (BuildConfig.LOCATIONIQ_API_KEY.isNotEmpty()) {
    XYTileSource(
        "LocationIQStreets",
        0, 19, 256,
        ".png?key=${BuildConfig.LOCATIONIQ_API_KEY}",
        arrayOf(
            "https://a-tiles.locationiq.com/v3/streets/r/",
            "https://b-tiles.locationiq.com/v3/streets/r/",
            "https://c-tiles.locationiq.com/v3/streets/r/"
        ),
        "© LocationIQ © OpenStreetMap contributors"
    )
} else {
    TileSourceFactory.MAPNIK
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onBreweryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val breweries by viewModel.breweries.collectAsState()
    val statusFilters by viewModel.statusFilters.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val syncPhase by viewModel.syncPhase.collectAsState()

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Visible region (bounding box + zoom), updated when panning/zooming settles.
    var viewport by remember { mutableStateOf<Pair<BoundingBox, Double>?>(null) }

    // While true, the map tracks the device location. Touching the map
    // disengages it; the my-location button re-engages it.
    var isFollowing by remember { mutableStateOf(true) }

    @SuppressLint("ClickableViewAccessibility")
    val mapView = remember {
        MapView(context).apply {
            setTileSource(tileSource)
            setMultiTouchControls(true)
            controller.setZoom(4.0)
            controller.setCenter(GeoPoint(39.8283, -98.5795))
            val density = resources.displayMetrics.density
            // Lift the zoom buttons (36dp bitmaps) half their height so the
            // system navigation bar doesn't cover them.
            zoomController.display.setAdditionalPixelMargins(0f, 0f, 0f, 18f * density)
            overlays.add(ScaleBarOverlay(this).apply {
                setAlignBottom(true)
                unitsOfMeasure = ScaleBarOverlay.UnitsOfMeasure.imperial
                setScaleBarOffset((10 * density).toInt(), (48 * density).toInt())
            })
            // Tile-source attribution (required by LocationIQ's terms).
            overlays.add(CopyrightOverlay(context).apply {
                setCopyrightNotice(tileSource.copyrightNotice)
                setOffset((10 * density).toInt(), (28 * density).toInt())
            })
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

    // One shared info window for all markers. Inflating a MarkerInfoWindow per
    // marker (35k+ breweries) was the source of the OOM/ANR issues.
    val currentOnBreweryClick by rememberUpdatedState(onBreweryClick)
    val sharedInfoWindow = remember {
        object : MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView) {
            @SuppressLint("ClickableViewAccessibility")
            override fun onOpen(item: Any?) {
                super.onOpen(item)
                val breweryId = (item as? Marker)?.relatedObject as? String
                // Replace BasicInfoWindow's default touch listener (which only
                // closes the bubble) with navigation to the brewery details.
                mView.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        breweryId?.let(currentOnBreweryClick)
                    }
                    true
                }
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
        object : RadiusMarkerClusterer(context) {
            // The base class closes item popups on every re-cluster (its
            // invalidate() forces one on the next draw pass), which would
            // dismiss the popup right after the rebuild effect restores it.
            // The rebuild effect owns the shared info window's lifecycle.
            override fun hideInfoWindows() {}
        }.apply {
            // RadiusMarkerClusterer requires a cluster icon before first draw.
            val size = 96
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BeerAmber.toArgb() }
            Canvas(bitmap).drawCircle(size / 2f, size / 2f, size / 2f, paint)
            setIcon(bitmap)
        }
    }

    // On the first fix, fit the view to the breweries nearest the user
    // (re-run once breweries arrive). On subsequent fixes the map follows
    // the location, keeping whatever zoom level is selected, for as long as
    // follow mode is engaged.
    var initialZoomDone by remember { mutableStateOf(false) }
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
                            // Hours are meaningless for a closed brewery.
                            subDescription = brewery.hours
                                .takeUnless { brewery.status == BreweryStatus.CLOSED.code }
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

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView }
        )
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                BreweryStatus.entries.forEach { status ->
                    FilterChip(
                        selected = status.code in statusFilters,
                        onClick = { viewModel.toggleStatusFilter(status.code) },
                        label = { Text(status.label) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        elevation = FilterChipDefaults.elevatedFilterChipElevation()
                    )
                }
            }
            if (syncPhase != SyncPhase.Idle) {
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
