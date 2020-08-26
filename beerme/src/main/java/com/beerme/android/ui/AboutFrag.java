package com.beerme.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about, container);
        this.getDialog().setTitle(R.string.About);

        TextView appversion = view.findViewById(R.id.about_appversion);
        appversion.setText(getString(R.string.Version, Utils.getAppVersion()));

        TextView platformversion = view.findViewById(R.id.about_platformversion);
        platformversion.setText(getString(R.string.Platform, Utils.getPlatformVersion()));

        Button contact = view.findViewById(R.id.aboutContactButton);
        contact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = getString(R.string.supportEmail);
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(email));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.emailSubject));
                intent.putExtra(Intent.EXTRA_EMAIL, email);

                String buf = getString(R.string.Notes_for_beerMe_support) + "\n-------------------------" +
                        "\nAPP_VERSION: " + Utils.getAppVersion() +
                        "\nAPP_PLATFORM: " + Utils.getPlatformVersion() +
                        "\nMODEL: " + Build.MODEL +
                        "\nMANUFACTURER: " + Build.MANUFACTURER +
                        "\nBRAND: " + Build.BRAND +
                        "\nPRODUCT: " + Build.PRODUCT;
                intent.putExtra(Intent.EXTRA_TEXT, buf);

                startActivity(Intent.createChooser(intent, getString(R.string.contact)));
            }
        });

        return view;
    }
}