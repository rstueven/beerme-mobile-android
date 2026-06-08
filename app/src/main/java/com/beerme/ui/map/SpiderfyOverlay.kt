package com.beerme.ui.map

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.view.MotionEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Fans a stack of markers that share (nearly) the same screen position out into
 * a ring of individually tappable legs, each joined to the shared point by a
 * leader line — the standard "spiderfy" disambiguation.
 *
 * osmbonuspack's clusterer has no such behaviour, and above its max clustering
 * zoom (17) co-located breweries draw directly on top of one another, so a plain
 * tap can only ever reach the topmost. This overlay sits above the clusterer and
 * intercepts taps:
 *  - inactive + tap lands on ≥2 stacked markers (only once clustering is off) →
 *    fan them out and consume the tap;
 *  - active + tap on a leg → choose that marker and collapse;
 *  - active + tap elsewhere → just collapse.
 *
 * It draws the legs itself, reusing each marker's own icon, and never adds
 * markers to the map — so it doesn't perturb the clusterer or the marker-rebuild
 * effect. [findStack] reports the co-located markers under a tapped pixel (empty
 * if fewer than two); [onLeafChosen] is invoked with the marker the user picks.
 */
class SpiderfyOverlay(
    private val density: Float,
    private val findStack: (Point) -> List<Marker>,
    private val onLeafChosen: (Marker) -> Unit
) : Overlay() {

    private data class Leg(val marker: Marker, val dx: Float, val dy: Float)

    private var anchor: GeoPoint? = null
    private var legs: List<Leg> = emptyList()
    private val reuse = Point()

    val isActive: Boolean get() = anchor != null

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xAA5D4037.toInt() // semi-opaque brown, matching the app accent
        strokeWidth = 2.5f * density
        style = Paint.Style.STROKE
    }

    fun collapse() {
        anchor = null
        legs = emptyList()
    }

    private fun iconSize(m: Marker): Pair<Int, Int> {
        val ic = m.icon
        val w = ic?.intrinsicWidth?.takeIf { it > 0 } ?: (24 * density).toInt()
        val h = ic?.intrinsicHeight?.takeIf { it > 0 } ?: (39 * density).toInt()
        return w to h
    }

    /**
     * Fan [group] out immediately (e.g. from a tap on a cluster of co-located
     * breweries). No-op for fewer than two markers.
     */
    fun spiderfy(group: List<Marker>, mapView: MapView) {
        if (group.size < 2) return
        InfoWindow.closeAllInfoWindowsOn(mapView)
        open(group)
        mapView.invalidate()
    }

    private fun open(group: List<Marker>) {
        val n = group.size
        val (iconW, iconH) = iconSize(group.first())
        // Ring radius: large enough that adjacent icons along the arc don't
        // overlap (arc spacing ≥ 1.2·iconW) and that the legs clear the centre.
        val radius = max(iconH.toFloat(), 1.2f * iconW * n / (2f * Math.PI.toFloat()))
        legs = group.mapIndexed { i, m ->
            // Start straight up, go clockwise, so a 2-stack splits top/bottom.
            val angle = (2.0 * Math.PI * i / n) - Math.PI / 2.0
            Leg(m, (radius * cos(angle)).toFloat(), (radius * sin(angle)).toFloat())
        }
        anchor = group.first().position
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val a = anchor ?: return
        val ap = mapView.projection.toPixels(a, reuse)
        val ax = ap.x.toFloat()
        val ay = ap.y.toFloat()
        for (leg in legs) {
            val lx = ax + leg.dx
            val ly = ay + leg.dy
            canvas.drawLine(ax, ay, lx, ly, linePaint)
            val icon = leg.marker.icon ?: continue
            val w = icon.intrinsicWidth
            val h = icon.intrinsicHeight
            // Default marker anchor is bottom-centre: the tip sits at (lx, ly).
            icon.setBounds(
                (lx - w / 2f).toInt(), (ly - h).toInt(),
                (lx + w / 2f).toInt(), ly.toInt()
            )
            icon.draw(canvas)
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
        if (isActive) {
            val a = anchor!!
            val ap = mapView.projection.toPixels(a, reuse)
            for (leg in legs) {
                val lx = ap.x + leg.dx
                val ly = ap.y + leg.dy
                val (w, h) = iconSize(leg.marker)
                val onLeg = e.x >= lx - w / 2f && e.x <= lx + w / 2f &&
                    e.y >= ly - h && e.y <= ly
                if (onLeg) {
                    val chosen = leg.marker
                    collapse()
                    mapView.invalidate()
                    onLeafChosen(chosen)
                    return true
                }
            }
            // Tapped off the legs: dismiss the fan.
            collapse()
            mapView.invalidate()
            return true
        }

        val stack = findStack(Point(e.x.toInt(), e.y.toInt()))
        if (stack.size < 2) return false // let the clusterer handle a lone marker
        // spiderfy() closes any open bubble first; left open it overlaps the legs
        // and, being a real View, would otherwise swallow the leg taps.
        spiderfy(stack, mapView)
        return true
    }
}
