/***
  Copyright (c) 2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */

package com.beerme.android.ui.tripplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidmapsextensions.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.Marker;
import com.beerme.android.R;

// https://github.com/commonsguy/cw-omnibus/blob/master/MapsV2/Popups/src/com/commonsware/android/mapsv2/popups/PopupAdapter.java

public class PopupAdapter implements InfoWindowAdapter {
	LayoutInflater inflater = null;

	public PopupAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return (null);
	}

	@Override
	public View getInfoContents(Marker marker) {
		View popup = inflater.inflate(R.layout.popup, (ViewGroup) null);

		TextView tv = (TextView) popup.findViewById(R.id.info_title);

		tv.setText(marker.getTitle());
		tv = (TextView) popup.findViewById(R.id.info_snippet);
		tv.setText(marker.getSnippet());

		return (popup);
	}
}