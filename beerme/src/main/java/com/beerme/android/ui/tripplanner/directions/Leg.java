package com.beerme.android.ui.tripplanner.directions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class Leg {
	private int distance;
	private String distanceText;
	private int duration;
	private String durationText;
	private String endAddress;
	private LatLng endLocation;
	private String startAddress;
	private LatLng startLocation;
	private List<Step> steps;

	public Leg(JSONObject legsJson) throws JSONException {
		this.distance = legsJson.getJSONObject("distance").getInt("value");
		this.distanceText = legsJson.getJSONObject("distance")
				.getString("text");
		this.duration = legsJson.getJSONObject("duration").getInt("value");
		this.durationText = legsJson.getJSONObject("duration")
				.getString("text");
		this.endAddress = legsJson.getString("end_address");
		this.endLocation = Directions.parseLatLng(legsJson
				.getJSONObject("end_location"));
		this.startAddress = legsJson.getString("start_address");
		this.startLocation = Directions.parseLatLng(legsJson
				.getJSONObject("start_location"));
		this.steps = parseSteps(legsJson.getJSONArray("steps"));
	}

	private List<Step> parseSteps(JSONArray stepsJson) throws JSONException {
		List<Step> steps = new ArrayList<Step>();
		final int n = stepsJson.length();

		if (n == 0) {
			return null;
		}

		for (int i = 0; i < n; i++) {
			steps.add(new Step((JSONObject) stepsJson.get(i)));
		}

		return steps;
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

	public String getEndAddress() {
		return this.endAddress;
	}

	public LatLng getEndLocation() {
		return this.endLocation;
	}

	public String getStartAddress() {
		return this.startAddress;
	}

	public LatLng getStartLocation() {
		return this.startLocation;
	}

	public List<Step> getSteps() {
		return this.steps;
	}
}