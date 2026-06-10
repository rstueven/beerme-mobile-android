package com.beerme.util

import com.beerme.data.model.Brewery
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import kotlin.math.cos
import kotlin.math.sqrt

/** Metres in one mile. */
const val METERS_PER_MILE = 1609.344

private const val METERS_PER_DEGREE_LAT = 111_320.0

/** A brewery that lies near a route, with where along/from the route it sits. */
data class RouteBrewery(
    val brewery: Brewery,
    val distanceAlongRouteMeters: Double,
    val distanceFromRouteMeters: Double
)

/**
 * Drops polyline vertices closer than [minSpacingMeters] to the previously kept
 * one. A cross-country route's ~10k vertices collapse to a few hundred segments
 * with negligible error at mile-scale radii, keeping the per-brewery distance
 * pass cheap. The first and last points are always kept.
 */
fun decimate(points: List<GeoPoint>, minSpacingMeters: Double = 200.0): List<GeoPoint> {
    if (points.size <= 2) return points
    val kept = ArrayList<GeoPoint>(points.size)
    kept.add(points.first())
    var last = points.first()
    for (i in 1 until points.size - 1) {
        val p = points[i]
        if (last.distanceToAsDouble(p) >= minSpacingMeters) {
            kept.add(p)
            last = p
        }
    }
    kept.add(points.last())
    return kept
}

/** Bounding box enclosing [points], padded outward by [padMeters] on every side. */
fun routeBoundingBox(points: List<GeoPoint>, padMeters: Double): BoundingBox {
    var north = -90.0
    var south = 90.0
    var east = -180.0
    var west = 180.0
    for (p in points) {
        if (p.latitude > north) north = p.latitude
        if (p.latitude < south) south = p.latitude
        if (p.longitude > east) east = p.longitude
        if (p.longitude < west) west = p.longitude
    }
    val dLat = padMeters / METERS_PER_DEGREE_LAT
    // Use the box's widest latitude for the most generous longitude padding.
    val widestLat = maxOf(kotlin.math.abs(north), kotlin.math.abs(south))
    val dLon = padMeters /
        (METERS_PER_DEGREE_LAT * cos(Math.toRadians(widestLat))).coerceAtLeast(1.0)
    return BoundingBox(
        (north + dLat).coerceAtMost(85.0),
        east + dLon,
        (south - dLat).coerceAtLeast(-85.0),
        west - dLon
    )
}

/** Nearest distance from a point to the route, and how far along the route that is. */
data class RouteProjection(
    val distFromRouteMeters: Double,
    val distAlongRouteMeters: Double
)

/**
 * Projects (lat, lon) onto the polyline [points], returning the minimum
 * perpendicular distance to any segment and the cumulative along-route distance
 * of that nearest projection. Uses a local equirectangular projection (metres
 * relative to each segment's start), accurate at radius scale and far cheaper
 * than per-segment Haversine. [cumulative] holds the running along-route
 * distance at each vertex (see [cumulativeDistances]).
 */
fun projectOntoRoute(
    lat: Double,
    lon: Double,
    points: List<GeoPoint>,
    cumulative: DoubleArray
): RouteProjection {
    val latRad = Math.toRadians(lat)
    val mPerDegLon = METERS_PER_DEGREE_LAT * cos(latRad)
    var bestSq = Double.MAX_VALUE
    var bestAlong = 0.0
    for (i in 0 until points.size - 1) {
        val a = points[i]
        val b = points[i + 1]
        // Project all three points to local metres relative to a.
        val ax = 0.0
        val ay = 0.0
        val bx = (b.longitude - a.longitude) * mPerDegLon
        val by = (b.latitude - a.latitude) * METERS_PER_DEGREE_LAT
        val px = (lon - a.longitude) * mPerDegLon
        val py = (lat - a.latitude) * METERS_PER_DEGREE_LAT
        val segLenSq = (bx - ax) * (bx - ax) + (by - ay) * (by - ay)
        val t = if (segLenSq <= 0.0) 0.0
        else (((px - ax) * (bx - ax) + (py - ay) * (by - ay)) / segLenSq).coerceIn(0.0, 1.0)
        val projX = ax + t * (bx - ax)
        val projY = ay + t * (by - ay)
        val dx = px - projX
        val dy = py - projY
        val distSq = dx * dx + dy * dy
        if (distSq < bestSq) {
            bestSq = distSq
            val segLen = sqrt(segLenSq)
            bestAlong = cumulative[i] + t * segLen
        }
    }
    return RouteProjection(sqrt(bestSq), bestAlong)
}

/** Running along-route distance (metres) at each vertex; element 0 is 0. */
fun cumulativeDistances(points: List<GeoPoint>): DoubleArray {
    val cumulative = DoubleArray(points.size)
    for (i in 1 until points.size) {
        cumulative[i] = cumulative[i - 1] + points[i - 1].distanceToAsDouble(points[i])
    }
    return cumulative
}

/**
 * Returns every brewery within [radiusMeters] of the route [points], each tagged
 * with its along-route and from-route distances, sorted by along-route position.
 *
 * Pipeline: bounding-box prefilter (rejecting null-coordinate and far breweries
 * in O(1)) then a point-to-segment minimum-distance test on the survivors. Run
 * this off the main thread for large brewery lists.
 */
fun breweriesNearRoute(
    points: List<GeoPoint>,
    breweries: List<Brewery>,
    radiusMeters: Double
): List<RouteBrewery> {
    if (points.size < 2) return emptyList()
    val cumulative = cumulativeDistances(points)
    val box = routeBoundingBox(points, radiusMeters)
    val result = ArrayList<RouteBrewery>()
    for (brewery in breweries) {
        val lat = brewery.latitude ?: continue
        val lon = brewery.longitude ?: continue
        if (!box.contains(lat, lon)) continue
        val projection = projectOntoRoute(lat, lon, points, cumulative)
        if (projection.distFromRouteMeters <= radiusMeters) {
            result.add(
                RouteBrewery(
                    brewery = brewery,
                    distanceAlongRouteMeters = projection.distAlongRouteMeters,
                    distanceFromRouteMeters = projection.distFromRouteMeters
                )
            )
        }
    }
    result.sortBy { it.distanceAlongRouteMeters }
    return result
}
