package com.beerme.ui.map

import android.content.Context
import com.beerme.BuildConfig
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay

/**
 * LocationIQ street tiles when an API key is configured (locationiq.apiKey in
 * local.properties), otherwise the default OpenStreetMap tile server. Shared by
 * every screen that hosts a map.
 */
val beerMeTileSource: ITileSource = if (BuildConfig.LOCATIONIQ_API_KEY.isNotEmpty()) {
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

/**
 * A MapView with the common BeerMe chrome applied: the shared tile source,
 * multi-touch controls, lifted zoom buttons (clear of the nav bar), an imperial
 * scale bar, and the required tile attribution. Screen-specific camera setup,
 * overlays, and listeners are added by the caller.
 */
fun createBaseMapView(context: Context): MapView = MapView(context).apply {
    setTileSource(beerMeTileSource)
    setMultiTouchControls(true)
    val density = resources.displayMetrics.density
    zoomController.display.setAdditionalPixelMargins(0f, 0f, 0f, 18f * density)
    overlays.add(ScaleBarOverlay(this).apply {
        setAlignBottom(true)
        unitsOfMeasure = ScaleBarOverlay.UnitsOfMeasure.imperial
        setScaleBarOffset((10 * density).toInt(), (48 * density).toInt())
    })
    overlays.add(CopyrightOverlay(context).apply {
        setCopyrightNotice(beerMeTileSource.copyrightNotice)
        setOffset((10 * density).toInt(), (28 * density).toInt())
    })
}
