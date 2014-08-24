package com.beerme.android_free.ui.tripplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SaveTripData implements Serializable {
	private static final long serialVersionUID = 7222086394735751286L;
	private String mDirections = null;
	private ArrayList<Long> mBreweries = new ArrayList<Long>();
	private HashMap<Long, String> mStops = null;

	public SaveTripData(String directions, Set<Long> breweries,
			HashMap<Long, String> stops) {
		this.mDirections = directions;
		this.mBreweries.addAll(breweries);
		this.mStops = stops;
	}

	public String getDirections() {
		return this.mDirections;
	}

	public ArrayList<Long> getBreweries() {
		return this.mBreweries;
	}

	public HashMap<Long, String> getStops() {
		return this.mStops;
	}
}