package com.beerme.ui.map

import android.content.Context
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    val scaleBar = ScaleBarOverlay(this).apply {
        setAlignBottom(true)
        unitsOfMeasure = ScaleBarOverlay.UnitsOfMeasure.imperial
    }
    overlays.add(scaleBar)

    val copyright = CopyrightOverlay(context).apply {
        setCopyrightNotice(beerMeTileSource.copyrightNotice)
    }
    overlays.add(copyright)

    // The map draws edge-to-edge (under the system bars), so anchor the scale bar
    // and the required tile attribution above the navigation bar. Otherwise the
    // nav bar covers the copyright notice, which the OSM/LocationIQ attribution
    // terms require to stay visible. Re-applied on every inset change (rotation,
    // gesture vs. 3-button nav).
    val sideOffset = (10 * density).toInt()
    val scaleBarBase = (28 * density).toInt()
    val copyrightBase = (4 * density).toInt()
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        scaleBar.setScaleBarOffset(sideOffset, scaleBarBase + navBottom)
        copyright.setOffset(sideOffset, copyrightBase + navBottom)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
