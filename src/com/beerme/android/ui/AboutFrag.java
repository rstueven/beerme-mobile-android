package com.beerme.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.utils.Utils;

public class AboutFrag extends DialogFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.about, container);
		this.getDialog().setTitle(R.string.About);

		TextView appversion = (TextView) view.findViewById(R.id.about_appversion);
		appversion.setText("Version: " + Utils.getAppVersion());

		TextView platformversion = (TextView) view.findViewById(R.id.about_platformversion);
		platformversion.setText("Platform: " + Utils.getPlatformVersion());

		Button contact = (Button) view.findViewById(R.id.aboutContactButton);
		contact.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String email = getString(R.string.supportEmail);
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(email));
				intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.emailSubject));
				intent.putExtra(Intent.EXTRA_EMAIL, email);

				StringBuffer buf = new StringBuffer(getString(R.string.Notes_for_beerMe_support));
				buf.append("\n-------------------------");
				buf.append("\nAPP_VERSION: " + Utils.getAppVersion());
				buf.append("\nAPP_PLATFORM: " + Utils.getPlatformVersion());
				buf.append("\nMODEL: " + Build.MODEL);
				buf.append("\nMANUFACTURER: " + Build.MANUFACTURER);
				buf.append("\nBRAND: " + Build.BRAND);
				buf.append("\nPRODUCT: " + Build.PRODUCT);
				intent.putExtra(Intent.EXTRA_TEXT, buf.toString());

				startActivity(Intent.createChooser(intent, getString(R.string.contact)));
			}
		});

		return view;
	}
}