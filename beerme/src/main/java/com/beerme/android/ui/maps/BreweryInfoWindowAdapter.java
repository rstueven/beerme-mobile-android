package com.beerme.android.ui.maps;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidmapsextensions.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.Marker;
import com.beerme.android.R;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BreweryInfoWindowAdapter implements InfoWindowAdapter {
	private LayoutInflater inflater;
	private TextView tv;
	private Collator collator = Collator.getInstance();
	private Comparator<Marker> comparator = new Comparator<Marker>() {
		public int compare(Marker lhs, Marker rhs) {
			String leftTitle = lhs.getTitle();
			String rightTitle = rhs.getTitle();
			if (leftTitle == null && rightTitle == null) {
				return 0;
			}
			if (leftTitle == null) {
				return 1;
			}
			if (rightTitle == null) {
				return -1;
			}
			return collator.compare(leftTitle, rightTitle);
		}
	};

	public BreweryInfoWindowAdapter(Context context, LayoutInflater inflater) {
		this.inflater = inflater;

		tv = new TextView(context);
		tv.setTextColor(Color.BLACK);
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return (null);
	}

	@Override
	public View getInfoContents(Marker marker) {
		if (marker.isCluster()) {
			List<Marker> markers = marker.getMarkers();
			int i = 0;
			String text = "";
			while (i < 5 && markers.size() > 0) {
				Marker m = Collections.min(markers, comparator);
				String title = m.getTitle();
				if (title == null) {
					break;
				}
				text += title + "\n";
				markers.remove(m);
				i++;
			}
			if (text.length() == 0) {
				text = "Markers with mutable data";
			} else if (markers.size() > 0) {
				text += "and " + markers.size() + " more...";
			} else {
				text = text.substring(0, text.length() - 1);
			}
			tv.setText(text);
			return tv;
		} else {
			View popup = inflater.inflate(R.layout.popup, (ViewGroup)null);

			TextView titleView = (TextView) popup.findViewById(R.id.info_title);

			titleView.setText(marker.getTitle());
			titleView = (TextView) popup.findViewById(R.id.info_snippet);
			titleView.setText(marker.getSnippet());

			return (popup);
		}
	}
}