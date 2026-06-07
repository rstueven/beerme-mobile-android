package com.beerme.data.remote

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * LocationIQ forward-geocoding autocomplete. Resolves free-text place queries
 * (cities, addresses, landmarks) to coordinates the map can recenter on.
 * See https://docs.locationiq.com/docs/autocomplete
 */
interface GeocodingApiService {
    @GET("v1/autocomplete")
    suspend fun autocomplete(
        @Query("key") key: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("dedupe") dedupe: Int = 1
    ): List<GeocodeResult>
}

/** One LocationIQ autocomplete hit. Coordinates arrive as strings. */
data class GeocodeResult(
    @Json(name = "lat") val lat: String?,
    @Json(name = "lon") val lon: String?,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "display_place") val displayPlace: String?,
    @Json(name = "display_address") val displayAddress: String?
)
