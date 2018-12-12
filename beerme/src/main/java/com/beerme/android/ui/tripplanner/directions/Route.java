package com.beerme.android.ui.tripplanner.directions;

/**
 * @author rstueven
 * 
 * @see <a href=
 *      'https://developers.google.com/maps/documentation/directions/#Routes'>The Google Directions API: Routes</a>
 */
public class Route {
//	private LatLngBounds bounds;
//	private String copyrights;
//	private ArrayList<Leg> legs;
//	private Segment overviewPolyline;
//	private String summary;
//	private ArrayList<Integer> waypointOrder;
//	private ArrayList<String> warnings;
//
//	public Route(JSONObject routeJson) throws JSONException {
//		this.bounds = parseBounds(routeJson.getJSONObject("bounds"));
//		this.legs = parseLegs(routeJson.getJSONArray("legs"));
//		this.overviewPolyline = Directions.parsePolyline(routeJson
//				.getJSONObject("overview_polyline").getString("points"));
//		this.summary = routeJson.getString("summary");
//		this.waypointOrder = parseWaypointOrder(routeJson
//				.getJSONArray("waypoint_order"));
//		this.warnings = parseWarnings(routeJson.getJSONArray("warnings"));
//	}
//
//	private LatLngBounds parseBounds(JSONObject b) throws JSONException {
//		JSONObject ne = b.getJSONObject("northeast");
//		LatLng boundNE = new LatLng(ne.getDouble("lat"), ne.getDouble("lng"));
//		JSONObject sw = b.getJSONObject("southwest");
//		LatLng boundSW = new LatLng(sw.getDouble("lat"), sw.getDouble("lng"));
//		return new LatLngBounds(boundSW, boundNE);
//	}
//
//	private ArrayList<Leg> parseLegs(JSONArray legsJson) throws JSONException {
//		ArrayList<Leg> legs = new ArrayList<Leg>();
//		final int n = legsJson.length();
//
//		if (n == 0) {
//			return null;
//		}
//
//		for (int i = 0; i < n; i++) {
//			legs.add(new Leg((JSONObject) legsJson.get(i)));
//		}
//
//		return legs;
//	}
//
//	private ArrayList<Integer> parseWaypointOrder(JSONArray waypointJson)
//			throws JSONException {
//		final int n = waypointJson.length();
//		ArrayList<Integer> order = new ArrayList<Integer>();
//
//		if (n == 0) {
//			return null;
//		}
//
//		for (int i = 0; i < n; i++) {
//			order.add(waypointJson.getInt(i));
//		}
//
//		return order;
//	}
//
//	private ArrayList<String> parseWarnings(JSONArray warningsJson)
//			throws JSONException {
//		final int n = warningsJson.length();
//		ArrayList<String> warnings = new ArrayList<String>();
//
//		if (n == 0) {
//			return null;
//		}
//
//		for (int i = 0; i < n; i++) {
//			warnings.add(warningsJson.getString(i));
//		}
//
//		return warnings;
//	}
//
//	public LatLngBounds getBounds() {
//		return this.bounds;
//	}
//
//	public String getCopyrights() {
//		return this.copyrights;
//	}
//
//	public ArrayList<Leg> getLegs() {
//		return this.legs;
//	}
//
//	public Segment getOverviewPolyline() {
//		return this.overviewPolyline;
//	}
//
//	public String getSummary() {
//		return this.summary;
//	}
//
//	public ArrayList<Integer> getWaypointOrder() {
//		return this.waypointOrder;
//	}
//
//	public ArrayList<String> getWarnings() {
//		return this.warnings;
//	}
}