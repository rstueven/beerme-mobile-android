package com.beerme.android_free.ui.tripplanner.directions;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class Step {
	private int distance;
	private String distanceText;
	private int duration;
	private String durationText;
	private LatLng endLocation;
	private String htmlInstructions;
	private String maneuver;
	private Segment polyline;
	private LatLng startLocation;
	private String travelMode;

	public Step(JSONObject stepsJson) throws JSONException {
		this.distance = stepsJson.getJSONObject("distance").getInt("value");
		this.duration = stepsJson.getJSONObject("duration").getInt("value");
		this.distanceText = stepsJson.getJSONObject("distance").getString(
				"text");
		this.durationText = stepsJson.getJSONObject("duration").getString(
				"text");
		this.endLocation = Directions.parseLatLng(stepsJson
				.getJSONObject("end_location"));
		this.htmlInstructions = stepsJson.getString("html_instructions");
		this.maneuver = stepsJson.optString("maneuver", null);
		this.polyline = Directions.parsePolyline(stepsJson.getJSONObject(
				"polyline").getString("points"));
		this.startLocation = Directions.parseLatLng(stepsJson
				.getJSONObject("start_location"));
		this.travelMode = stepsJson.getString("travel_mode");
	}

	public int getDistance() {
		return this.distance;
	}

	public int getDuration() {
		return this.duration;
	}

	public String getDistanceText() {
		return this.distanceText;
	}

	public String getDurationText() {
		return this.durationText;
	}

	public LatLng getEndLocation() {
		return this.endLocation;
	}

	public String getHtmlInstructions() {
		return this.htmlInstructions;
	}

	public String getManeuver() {
		return this.maneuver;
	}

	public Segment getPolyline() {
		return this.polyline;
	}

	public LatLng getStartLocation() {
		return this.startLocation;
	}

	public String getTravelMode() {
		return this.travelMode;
	}
}