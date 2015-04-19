package com.beerme.android_free.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.beerme.android_free.ui.BeerActivity;
import com.beerme.android_free.ui.BreweryActivity;

public class URIDispatcher extends FragmentActivity {
	private long breweryId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		Uri data = i.getData();

		String auth = data.getAuthority();
		String frag = data.getFragment();
		String path = data.getPath();
		String query = data.getQuery();

		if ("".equals(auth)) {
			// Relative path under beerme.com
			String regex = "^\\d+";

			if (query != null) {
				// There is a query
				if (query.matches(regex)) {
					// Query is a number
					breweryId = Long.parseLong(query);
					if (frag == null) {
						// There's no frag
						// Invoke BreweryDetail(breweryId)
						startBreweryDetail();
					} else if (frag.matches(regex)) {
						// Frag exists and is a number
						// Invoke BeerDetail(frag)
						startBeerDetail(frag);
					} else {
						// There's a frag, but we don't recognize it
						// Invoke BreweryDetail(breweryId)
						startBreweryDetail();
					}
				}
			} else {
				// beerme.com internal link without query
				Uri.Builder beermeLink = new Uri.Builder();
				beermeLink.authority("beerme.com");
				beermeLink.fragment(frag);
				beermeLink.path(path);
				beermeLink.scheme("http");

				sendToBrowser(beermeLink.build());
			}
		} else {
			// Absolute path
			Uri.Builder externalLink = new Uri.Builder();
			externalLink.fragment(frag);
			externalLink.path(path);
			externalLink.query(query);
			externalLink.scheme("http");

			sendToBrowser(externalLink.build());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.trackActivityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Utils.trackActivityStop(this);
	}

	private void startBreweryDetail() {
		Intent intent = new Intent(this, BreweryActivity.class);
		intent.putExtra("id", breweryId);
		startActivity(intent);
		this.finish();
	}

	private void startBeerDetail(String frag) {
		long beerId = Long.parseLong(frag);
		Intent intent = new Intent(this, BeerActivity.class);
		intent.putExtra("id", beerId);
		startActivity(intent);
		this.finish();
	}

	private void sendToBrowser(Uri uri) {
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(i);
		this.finish();
	}

	public static String rewriteUri(String s) {
		String rewritten = s.replaceAll("(<a href=['\"])",
				"$1com.beerme.android.link://");

		return rewritten;
	}
}