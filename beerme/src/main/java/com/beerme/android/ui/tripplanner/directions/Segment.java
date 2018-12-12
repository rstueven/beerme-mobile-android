package com.beerme.android.ui.tripplanner.directions;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * List of {@list LatLng} objects that describe a segment of a {@link Route} or
 * a {@link Leg}
 * <p>
 * Ideally, <code>Segment</code> would simply extend
 * {@link com.google.android.gms.maps.model.Polyline}, but that class is
 * <code>final</code>.
 * 
 * @author rstueven
 * 
 */
public class Segment extends ArrayList<LatLng> {
//	private static final long serialVersionUID = -6692983509180553884L;
//
//	public Segment() {
//		super();
//	}
//
//	/**
//	 * Calculates a bounding box that includes all points in this Segment
//	 *
//	 * @return {@link LatLngBounds}, or <code>null</code> on error
//	 */
//	public LatLngBounds getBounds() {
//		LatLngBounds.Builder builder = new LatLngBounds.Builder();
//
//		for (LatLng p : this) {
//			builder.include(p);
//		}
//
//		try {
//			return builder.build();
//		} catch (IllegalStateException e) {
//			Log.i(Utils.APPTAG, e.getLocalizedMessage());
//			return null;
//		}
//	}
//
//	/**
//	 * Calculates the shortest distance between this <code>Segment</code> and a
//	 * given point
//	 *
//	 * @param point
//	 *            {@link LatLng} representation of the given point
//	 * @return distance in meters
//	 * @see <a href=
//	 *      'http://forums.codeguru.com/showthread.php?194400-Distance-between-point-and-line-segment'>Distan
//	 *      c e between point and line segment</a> at Codeguru
//	 */
//	public double distanceToPoint(LatLng point) {
//		LatLng startPoint = this.get(0);
//		LatLng endPoint = this.get(this.size() - 1);
//
//		double ax = startPoint.latitude;
//		double ay = startPoint.longitude;
//		double bx = endPoint.latitude;
//		double by = endPoint.longitude;
//		double cx = point.latitude;
//		double cy = point.longitude;
//
//		double baDiffX = bx - ax;
//		double baDiffY = by - ay;
//		double caDiffX = cx - ax;
//		double caDiffY = cy - ay;
//
//		double l = Math.sqrt((baDiffX * baDiffX) + (baDiffY * baDiffY));
//
//		double r = ((caDiffX * baDiffX) + (caDiffY * baDiffY)) / (l * l);
//
//		if (r <= 0)
//			return Utils.distance(point, startPoint);
//		if (r >= 1)
//			return Utils.distance(point, endPoint);
//
//		double px = ax + (r * baDiffX);
//		double py = ay + (r * baDiffY);
//		LatLng pXY = new LatLng(px, py);
//
//		return Utils.distance(point, pXY);
//	}
}