package com.beerme.android_free.ui.maps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidmapsextensions.SupportMapFragment;
import com.beerme.android_free.utils.TouchableWrapper;
import com.beerme.android_free.utils.Utils;

// http://dimitar.me/how-to-detect-a-user-pantouchdrag-on-android-map-v2/
public class BeerMeSupportMapFragment extends SupportMapFragment {
	public View mOriginalContentView;
	public TouchableWrapper mTouchView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		mOriginalContentView = super.onCreateView(inflater, parent,
				savedInstanceState);
		mTouchView = new TouchableWrapper(getActivity());
		mTouchView.addView(mOriginalContentView);
		return mTouchView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public View getView() {
		return mOriginalContentView;
	}
}