package com.beerme.data.repository

import com.beerme.data.remote.DirectionsApiService
import kotlinx.coroutines.CancellationException
import org.osmdroid.bonuspack.utils.PolylineEncoder
import org.osmdroid.util.GeoPoint
import retrofit2.HttpException

/** A decoded driving route. */
data class Route(
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val durationSeconds: Double
)

/** Outcome of a routing request, distinguishing the cases the UI surfaces. */
sealed interface RouteResult {
    data class Success(val route: Route) : RouteResult
    /** Routing succeeded but no driving route exists between the points. */
    data object NoRoute : RouteResult
    /** No LocationIQ API key configured — routing is unavailable. */
    data object Unavailable : RouteResult
    data class Error(val message: String?) : RouteResult
}

/**
 * Fetches driving routes from LocationIQ. A no-op ([RouteResult.Unavailable])
 * when no API key is configured, mirroring [GeocodingRepository].
 */
class DirectionsRepository(
    private val apiService: DirectionsApiService?,
    private val apiKey: String
) {
    /**
     * Driving route from [start] to [end], optionally detouring through
     * [waypoints] (in the order given). OSRM keeps via-points in sequence, so the
     * caller is responsible for ordering them sensibly along the trip.
     */
    suspend fun route(
        start: GeoPoint,
        end: GeoPoint,
        waypoints: List<GeoPoint> = emptyList()
    ): RouteResult {
        val service = apiService ?: return RouteResult.Unavailable
        if (apiKey.isEmpty()) return RouteResult.Unavailable
        // OSRM wants lon,lat order, points separated by ';'.
        val coords = buildString {
            append("${start.longitude},${start.latitude}")
            waypoints.forEach { append(";${it.longitude},${it.latitude}") }
            append(";${end.longitude},${end.latitude}")
        }
        return try {
            val response = service.directions(coords, apiKey)
            val route = response.routes?.firstOrNull()
                ?.takeIf { response.code == "Ok" && it.geometry != null }
                ?: return RouteResult.NoRoute
            // Match osmbonuspack's own OSRM decode (precision 10, not 3D).
            val points = PolylineEncoder.decode(route.geometry, 10, false)
            if (points.isEmpty()) return RouteResult.NoRoute
            RouteResult.Success(
                Route(points, route.distance ?: 0.0, route.duration ?: 0.0)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            // 429 = LocationIQ rate limit (free tier ~2 req/s).
            val message = if (e.code() == 429) {
                "Routing rate limit reached — try again in a moment"
            } else {
                "Couldn't fetch the route"
            }
            RouteResult.Error(message)
        } catch (e: Exception) {
            RouteResult.Error("Couldn't fetch the route")
        }
    }
}
