package com.beerme.android.ui.tripplanner.directions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONUtils;

import com.google.android.gms.maps.model.LatLng;

/**
 * <code>Directions</code> is a representation of the {@link JSONObject}
 * obtained via the Google Directions API. The source <code>JSONObject</code>
 * may be specified as a JSON string, or may be downloaded from a {@link URL}.
 * 
 * @author rstueven
 * @see <a href=
 *      'https://developers.google.com/maps/documentation/directions/#JSON'>The
 *      Google Directions API: Directions Responses</a>
 * 
 */
public class Directions {
	protected static final String APPTAG = "Directions";
	private String jsonString = null;
	private JSONObject jsonObject = null;
	private ArrayList<Route> routes = null;
	private String status = null;

	/**
	 * Class constructor. The JSON string is downloaded from the specified
	 * {@link URL}, then passed to {@link #Directions(String jsonString)} for
	 * parsing.
	 * 
	 * @param url
	 *            Google Directions URL
	 * @throws JSONException
	 * @throws IOException
	 * @see <a href=
	 *      'https://developers.google.com/maps/documentation/directions/#DirectionsRequests'
	 *      > The Google Directions API: Directions Requests</a>
	 */
	public Directions(final URL url) throws JSONException, IOException {
		this(JSONUtils.fetchJsonString(url));
	}

	/**
	 * Class constructor that parses a JSON string.
	 * 
	 * @param jsonString
	 *            JSON string to be parsed
	 * @throws JSONException
	 */
	public Directions(final String jsonString) throws JSONException {
		if (jsonString != null) {
			this.jsonString = jsonString;
			this.jsonObject = new JSONObject(this.jsonString);
			this.status = jsonObject.getString("status");
			if (this.status.equals("OK")) {
				this.routes = parseRoutes(jsonObject.getJSONArray("routes"));
			}
		} else {
			throw new JSONException("Null directions");
		}
	}

	/**
	 * Parses the {@link JSONArray} of routes into an <code>ArrayList</code>.
	 * 
	 * @param routesJson
	 *            the <code>JSONArray</code> to be parsed
	 * @return an <code>ArrayList</code> of {@link Route} objects, or
	 *         <code>null</code> if there are none
	 * @throws JSONException
	 */
	private ArrayList<Route> parseRoutes(final JSONArray routesJson)
			throws JSONException {
		ArrayList<Route> routes = new ArrayList<Route>();
		final int n = routesJson.length();

		if (n == 0) {
			return null;
		}

		for (int i = 0; i < n; i++) {
			routes.add(new Route((JSONObject) routesJson.get(i)));
		}

		return routes;
	}

	/**
	 * Gets this object's {@link Route}s.
	 * 
	 * @return an <code>ArrayList</code> of <code>Route</code> objects
	 */
	public ArrayList<Route> getRoutes() {
		return this.routes;
	}

	/**
	 * Gets the status of the directions request
	 * 
	 * @return status as a String
	 * @see <a href=
	 *      'https://developers.google.com/maps/documentation/directions/#StatusCodes'
	 *      > The Google Directions API: Status Codes</a>
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * Converts a {@link JSONObject} to a {@link LatLng}
	 * 
	 * @param latLngJson
	 *            the <code>JSONObject</code> to be converted
	 * @return the <code>LatLng</code> object
	 * @throws JSONException
	 */
	public static LatLng parseLatLng(JSONObject latLngJson)
			throws JSONException {
		double lat = latLngJson.getDouble("lat");
		double lng = latLngJson.getDouble("lng");

		return new LatLng(lat, lng);
	}

	// http://stackoverflow.com/questions/2964982/android-get-and-parse-google-directions
	/**
	 * Decodes an encoded {@link com.google.android.gms.maps.model.Polyline}
	 * 
	 * @param encodedPolyline
	 *            the encoded <code>Polyline</code> string
	 * @return {@link Segment} containing all of the points from the
	 *         <code>Polyline</code>
	 * @see <a href=
	 *      'http://stackoverflow.com/questions/2964982/android-get-and-parse-google-directions'>androi
	 *      d get and parse Google Directions</a> at Stack Overflow
	 */
	public static Segment parsePolyline(String encodedPolyline) {
		Segment poly = new Segment();
		int index = 0, len = encodedPolyline.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encodedPolyline.charAt(index++) - 0x3f;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encodedPolyline.charAt(index++) - 0x3f;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((lat / 1E5), (lng / 1E5));
			poly.add(p);
		}

		return poly;
	}

	@Override
	public String toString() {
		return this.jsonString;
	}
}