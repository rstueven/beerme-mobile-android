package com.beerme.data.remote

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * LocationIQ Directions (OSRM-based) routing. Returns a driving route between
 * two or more points as an encoded polyline the map can draw and the route
 * planner can measure breweries against.
 * See https://docs.locationiq.com/docs/directions-driving
 */
interface DirectionsApiService {
    /**
     * [coordinates] must already be formatted as `lon,lat;lon,lat[;…]` — note
     * OSRM's lon,lat order, the opposite of most APIs. It is passed `encoded`
     * so the `;` and `,` separators are not percent-escaped.
     */
    @GET("v1/directions/driving/{coordinates}")
    suspend fun directions(
        @Path("coordinates", encoded = true) coordinates: String,
        @Query("key") key: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "polyline",
        @Query("steps") steps: Boolean = false,
        @Query("alternatives") alternatives: Boolean = false
    ): DirectionsResponse
}

/** OSRM response envelope. [code] is "Ok" on success. */
data class DirectionsResponse(
    @Json(name = "code") val code: String?,
    @Json(name = "routes") val routes: List<RouteDto>?
)

data class RouteDto(
    /** Encoded polyline (Google algorithm, precision 5). */
    @Json(name = "geometry") val geometry: String?,
    @Json(name = "distance") val distance: Double?, // metres
    @Json(name = "duration") val duration: Double?  // seconds
)
