package com.beerme.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class BreweryInfoFragment extends Fragment {
    private Brewery brewery;

    static BreweryInfoFragment getInstance(@NonNull Brewery brewery) {
        BreweryInfoFragment frag = new BreweryInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable("brewery", brewery);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("beerme", "onCreate(Bundle savedInstanceState)");
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args == null) {
            throw new IllegalStateException("BreweryInfoFragment.onCreate(): null args");
        }

        brewery = (Brewery)args.getSerializable("brewery");

        if (brewery == null) {
            throw new IllegalArgumentException("BreweryInfoFragment.onCreate(): null brewery");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("beerme", "onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)");
        View view = inflater.inflate(R.layout.fragment_brewery_info, container, false);

        Activity activity = getActivity();

        if (activity != null) {
            TextView hoursView = view.findViewById(R.id.hours_view);
            LinearLayout servicesLayout = view.findViewById(R.id.services_layout);
            ImageView imageView = view.findViewById(R.id.image_view);

            Log.d("beerme", brewery.toString());
            hoursView.setText(brewery.hours);
            brewery.showServicesByName(activity, servicesLayout);

            if (TextUtils.isEmpty(brewery.image)) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                String imageUrl = "https://beerme.com/graphics/" + brewery.image;
                Picasso.with(activity).load(imageUrl).into(imageView);
            }
        }

        return view;
    }
}