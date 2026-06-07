package com.beerme.data.repository

import com.beerme.data.remote.GeocodeResult
import com.beerme.data.remote.GeocodingApiService
import kotlinx.coroutines.CancellationException

/** A geographic place the map can recenter on. */
data class PlaceResult(
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double
)

/**
 * Resolves free-text place queries via LocationIQ. A no-op (always returns no
 * results) when no LocationIQ API key is configured, so geographic search
 * simply disappears rather than erroring on builds without a key.
 */
class GeocodingRepository(
    private val apiService: GeocodingApiService?,
    private val apiKey: String
) {
    suspend fun search(query: String, limit: Int = 5): List<PlaceResult> {
        val service = apiService ?: return emptyList()
        if (apiKey.isEmpty()) return emptyList()
        return try {
            service.autocomplete(apiKey, query, limit).mapNotNull { it.toPlaceResult() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Geocoding is best-effort: a network or quota error yields no place
            // suggestions rather than failing the whole search.
            emptyList()
        }
    }

    private fun GeocodeResult.toPlaceResult(): PlaceResult? {
        val latitude = lat?.toDoubleOrNull() ?: return null
        val longitude = lon?.toDoubleOrNull() ?: return null
        val placeName = displayPlace?.takeIf { it.isNotBlank() }
            ?: displayName?.substringBefore(",")?.trim()?.takeIf { it.isNotBlank() }
            ?: return null
        val placeAddress = displayAddress?.takeIf { it.isNotBlank() } ?: displayName
        return PlaceResult(placeName, placeAddress, latitude, longitude)
    }
}
