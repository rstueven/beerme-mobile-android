package com.beerme.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.beerme.data.model.Brewery

/**
 * Hands a brewery to an external navigation app via an ACTION_VIEW intent. The
 * Google Maps "dir" URL resolves to the Maps app when installed (and a browser
 * otherwise), where the user picks the travel mode (driving/transit/biking/
 * walking). No location permission is needed: the maps app fills in the current
 * location as the origin.
 *
 * The destination is the brewery's street [address][Brewery.address] when one is
 * on file — more legible and robust to slightly-off coordinates than raw
 * lat/lon — falling back to its coordinates otherwise. When the brewery's
 * [geoprecision][Brewery.geoprecision] is below 8 its location is only
 * approximate, so the user is warned and must confirm before the maps app opens.
 */
fun launchDirections(context: Context, brewery: Brewery) {
    val destination = brewery.address?.takeIf { it.isNotBlank() }
        ?: brewery.latitude?.let { lat -> brewery.longitude?.let { lon -> "$lat,$lon" } }
        ?: return
    val uri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(destination)
    )
    val launch = {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }
    val geoprecision = brewery.geoprecision
    if (geoprecision != null && geoprecision < 8) {
        AlertDialog.Builder(context)
            .setTitle("Approximate location")
            .setMessage(
                "The location on file for this brewery is approximate and may " +
                    "not be accurate."
            )
            .setPositiveButton("Get directions") { _, _ -> launch() }
            .setNegativeButton("Cancel", null)
            .show()
    } else {
        launch()
    }
}
