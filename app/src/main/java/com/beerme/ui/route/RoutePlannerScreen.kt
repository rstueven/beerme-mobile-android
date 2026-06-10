package com.beerme.ui.route

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.beerme.data.model.Brewery
import com.beerme.data.repository.RouteResult
import com.beerme.ui.launchRouteDirections
import com.beerme.ui.map.createBaseMapView
import com.beerme.ui.theme.BeerAmber
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private const val ROUTE_PADDING_PX = 120

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RoutePlannerScreen(
    viewModel: RoutePlannerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val start by viewModel.start.collectAsState()
    val end by viewModel.end.collectAsState()
    val radiusMiles by viewModel.radiusMiles.collectAsState()
    val route by viewModel.route.collectAsState()
    val candidates by viewModel.candidates.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val computing by viewModel.computing.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.startLocationUpdates(context)
        }
    }

    // Which field's picker is open (null = closed).
    var pickerField by remember { mutableStateOf<EndpointField?>(null) }

    val density = context.resources.displayMetrics.density
    val primaryArgb = MaterialTheme.colorScheme.primary.toArgb()
    val amberArgb = BeerAmber.toArgb()

    // Marker icons: amber = unselected candidate, primary = selected, green
    // start, red end. Built once.
    val unselectedIcon = remember { BitmapDrawable(context.resources, dotBitmap(amberArgb, density)) }
    val selectedIcon = remember(primaryArgb) {
        BitmapDrawable(context.resources, dotBitmap(primaryArgb, density, ringPx = 5f))
    }
    val startIcon = remember { BitmapDrawable(context.resources, dotBitmap(0xFF2E7D32.toInt(), density)) }
    val endIcon = remember { BitmapDrawable(context.resources, dotBitmap(0xFFC62828.toInt(), density)) }

    @SuppressLint("ClickableViewAccessibility")
    val mapView = remember {
        createBaseMapView(context).apply {
            controller.setZoom(4.0)
            controller.setCenter(GeoPoint(39.8283, -98.5795))
        }
    }

    val routePolyline = remember {
        Polyline(mapView).apply {
            outlinePaint.color = primaryArgb
            outlinePaint.strokeWidth = 10f
        }
    }
    val startMarker = remember {
        Marker(mapView).apply {
            icon = startIcon
            title = "Start"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setInfoWindow(null)
            setOnMarkerClickListener { _, _ -> true }
        }
    }
    val endMarker = remember {
        Marker(mapView).apply {
            icon = endIcon
            title = "End"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setInfoWindow(null)
            setOnMarkerClickListener { _, _ -> true }
        }
    }

    // Candidate markers, rebuilt when the candidate set changes.
    val candidateMarkers = remember { mutableListOf<Marker>() }

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

    // Draw / update the route polyline and start+end markers, and fit the camera.
    LaunchedEffect(route, start, end) {
        val success = route as? RouteResult.Success
        if (success != null) {
            routePolyline.setPoints(success.route.points)
            if (!mapView.overlays.contains(routePolyline)) {
                mapView.overlays.add(routePolyline)
            }
        } else {
            mapView.overlays.remove(routePolyline)
        }
        start?.let { s ->
            startMarker.position = s.geoPoint
            if (!mapView.overlays.contains(startMarker)) mapView.overlays.add(startMarker)
        }
        end?.let { e ->
            endMarker.position = e.geoPoint
            if (!mapView.overlays.contains(endMarker)) mapView.overlays.add(endMarker)
        }
        if (success != null && success.route.points.isNotEmpty()) {
            // Fit the camera to the whole route.
            val box = BoundingBox.fromGeoPointsSafe(success.route.points)
            runCatching { mapView.zoomToBoundingBox(box, true, ROUTE_PADDING_PX) }
        }
        mapView.invalidate()
    }

    // Rebuild candidate markers when the candidate set changes.
    LaunchedEffect(candidates) {
        candidateMarkers.forEach { mapView.overlays.remove(it) }
        candidateMarkers.clear()
        candidates.forEach { rb ->
            val lat = rb.brewery.latitude ?: return@forEach
            val lon = rb.brewery.longitude ?: return@forEach
            val marker = Marker(mapView).apply {
                position = GeoPoint(lat, lon)
                title = rb.brewery.name
                icon = if (rb.brewery.id in selectedIds) selectedIcon else unselectedIcon
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                setInfoWindow(null)
                relatedObject = rb.brewery.id
                setOnMarkerClickListener { _, _ ->
                    viewModel.toggleSelection(rb.brewery.id)
                    true
                }
            }
            candidateMarkers.add(marker)
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    // Swap candidate marker icons when the selection changes.
    LaunchedEffect(selectedIds) {
        candidateMarkers.forEach { marker ->
            val id = marker.relatedObject as? String ?: return@forEach
            marker.icon = if (id in selectedIds) selectedIcon else unselectedIcon
        }
        mapView.invalidate()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Plan a Route") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { mapView })

            // Top control panel: start/end fields + radius slider.
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 3.dp,
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    EndpointRow(
                        label = "Start",
                        value = start?.label,
                        onClick = { pickerField = EndpointField.START }
                    )
                    Spacer(Modifier.size(8.dp))
                    EndpointRow(
                        label = "End",
                        value = end?.label,
                        onClick = { pickerField = EndpointField.END }
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Within $radiusMiles miles of the route",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = radiusMiles.toFloat(),
                        onValueChange = { viewModel.setRadiusMiles(it.toInt()) },
                        valueRange = 1f..50f,
                        steps = 48
                    )
                }
            }

            if (computing) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Status banner for the various non-success route states.
            val bannerText = when (val r = route) {
                RouteResult.Unavailable -> "Route planning needs a LocationIQ key"
                RouteResult.NoRoute -> "No driving route found between these points"
                is RouteResult.Error -> r.message
                is RouteResult.Success ->
                    if (candidates.isEmpty() && !computing)
                        "No breweries within $radiusMiles miles — try a larger radius"
                    else null
                null -> if (start == null || end == null)
                    "Choose a start and end to find breweries along the way" else null
            }
            if (bannerText != null && !computing) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = bannerText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Bottom bar: selected count + Get directions.
            if (start != null && end != null && route is RouteResult.Success) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    tonalElevation = 3.dp,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${selectedIds.size} of 9 stops",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = {
                                start?.let { s ->
                                    end?.let { e ->
                                        launchRouteDirections(
                                            context, s, e, viewModel.orderedSelectedStops()
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("Get directions")
                        }
                    }
                }
            }
        }
    }

    // Endpoint picker bottom sheet.
    pickerField?.let { field ->
        EndpointPickerSheet(
            field = field,
            viewModel = viewModel,
            userLocation = userLocation,
            onRequestLocationPermission = { permissionState.launchMultiplePermissionRequest() },
            locationGranted = permissionState.allPermissionsGranted,
            onDismiss = {
                pickerField = null
                viewModel.clearPicker()
            }
        )
    }

    // One-shot messages (e.g. stop cap reached): a transient toast-like overlay.
    userMessage?.let { message ->
        DismissableMessage(message) { viewModel.consumeUserMessage() }
    }
}

@Composable
private fun EndpointRow(label: String, value: String?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = value ?: "Tap to choose",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndpointPickerSheet(
    field: EndpointField,
    viewModel: RoutePlannerViewModel,
    userLocation: GeoPoint?,
    onRequestLocationPermission: () -> Unit,
    locationGranted: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val query by viewModel.pickerQuery.collectAsState()
    val places by viewModel.placeResults.collectAsState()
    val breweries by viewModel.breweryResults.collectAsState()
    var tab by remember { mutableStateOf(0) } // 0 = Places, 1 = Breweries

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).navigationBarsPadding()) {
            Text(
                text = if (field == EndpointField.START) "Choose start" else "Choose end",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.size(8.dp))

            // Current location shortcut.
            ListItem(
                headlineContent = { Text("Use current location") },
                leadingContent = { Icon(Icons.Filled.MyLocation, contentDescription = null) },
                modifier = Modifier.clickable {
                    if (userLocation != null) {
                        viewModel.setEndpoint(
                            field,
                            Endpoint(userLocation.latitude, userLocation.longitude, "Current location")
                        )
                        onDismiss()
                    } else {
                        onRequestLocationPermission()
                    }
                }
            )
            if (userLocation == null && !locationGranted) {
                Text(
                    text = "Grant location permission to use this",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(Modifier.size(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onPickerQueryChange,
                placeholder = { Text("Search places or breweries") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.size(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("Places") }
                SegmentedButton(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("Breweries") }
            }
            Spacer(Modifier.size(8.dp))

            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                if (tab == 0) {
                    items(places, key = { it.latitude.toString() + it.longitude }) { place ->
                        ListItem(
                            headlineContent = { Text(place.name) },
                            supportingContent = place.address?.let {
                                { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            },
                            leadingContent = { Icon(Icons.Filled.Place, contentDescription = null) },
                            modifier = Modifier.clickable {
                                viewModel.setEndpoint(
                                    field,
                                    Endpoint(place.latitude, place.longitude, place.name)
                                )
                                onDismiss()
                            }
                        )
                    }
                } else {
                    items(breweries, key = { it.id }) { brewery ->
                        ListItem(
                            headlineContent = { Text(brewery.name) },
                            supportingContent = brewery.address?.let {
                                { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            },
                            leadingContent = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                            modifier = Modifier.clickable {
                                val lat = brewery.latitude ?: return@clickable
                                val lon = brewery.longitude ?: return@clickable
                                viewModel.setEndpoint(field, Endpoint(lat, lon, brewery.name))
                                onDismiss()
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.size(8.dp))
        }
    }
}

/** Brief inline message surfaced over the map, dismissed after a short delay. */
@Composable
private fun DismissableMessage(message: String, onConsumed: () -> Unit) {
    LaunchedEffect(message) {
        kotlinx.coroutines.delay(2500)
        onConsumed()
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 96.dp, start = 24.dp, end = 24.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.inverseSurface,
            tonalElevation = 4.dp
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

/** A filled circular marker dot with a white border. */
private fun dotBitmap(colorArgb: Int, density: Float, ringPx: Float = 4f): Bitmap {
    val size = (28 * density).toInt().coerceAtLeast(24)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = size / 2f
    canvas.drawCircle(center, center, center,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE })
    canvas.drawCircle(center, center, center - ringPx,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorArgb })
    return bitmap
}
