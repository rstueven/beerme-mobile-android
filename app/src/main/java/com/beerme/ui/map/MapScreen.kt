package com.beerme.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.beerme.data.model.BreweryStatus
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
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

/**
 * Below this zoom level no individual markers are shown: feeding tens of
 * thousands of world-wide markers to the clusterer re-runs its O(n²)
 * algorithm on the UI thread at every zoom change (the cause of the
 * historical OOM/ANR issues). Above it, only breweries within an expanded
 * margin around the visible region are loaded.
 */
private const val MIN_MARKER_ZOOM = 6.0

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

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Visible region (bounding box + zoom), updated when panning/zooming settles.
    var viewport by remember { mutableStateOf<Pair<BoundingBox, Double>?>(null) }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(4.0)
            controller.setCenter(GeoPoint(39.8283, -98.5795))
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

    val clusterer = remember {
        RadiusMarkerClusterer(context).apply {
            // RadiusMarkerClusterer requires a cluster icon before first draw.
            val size = 96
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BeerAmber.toArgb() }
            Canvas(bitmap).drawCircle(size / 2f, size / 2f, size / 2f, paint)
            setIcon(bitmap)
        }
    }

    // Handle startup zoom/center; re-run once breweries arrive so the zoom
    // level can be derived from nearby brewery density.
    val hasBreweries = breweries.isNotEmpty()
    LaunchedEffect(userLocation, hasBreweries) {
        userLocation?.let { point ->
            val zoom = viewModel.calculateZoom(point, breweries)
            mapView.controller.animateTo(point, zoom, 1000L)
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
                            relatedObject = brewery.id
                            infoWindow = sharedInfoWindow
                        }
                    } else null
                }
            }
        }

        sharedInfoWindow.close()
        clusterer.items.clear()
        clusterer.items.addAll(markers)
        clusterer.invalidate()
        if (!mapView.overlays.contains(clusterer)) {
            mapView.overlays.add(clusterer)
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

    // Ask for location permission on first composition; fetch the user's
    // location as soon as it is granted.
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.requestUserLocation(context)
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView }
        )
        Row(
            modifier = Modifier
                .statusBarsPadding()
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
    }
}
