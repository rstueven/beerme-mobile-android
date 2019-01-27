package com.beerme.android.ui;

import android.location.Location;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.beerme.android.util.LocationActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BreweryListViewAdapter extends RecyclerView.Adapter<BreweryListViewAdapter.BreweryListViewHolder> {
    private final LocationActivity mActivity;
    private List<Brewery> mBreweryList;

    public BreweryListViewAdapter(@NonNull LocationActivity activity, @NonNull List<Brewery> breweryList) {
        this.mActivity = activity;
        this.mBreweryList = breweryList;
    }

    @Override
    @NonNull
    public BreweryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BreweryListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.brewerylist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final BreweryListViewHolder holder, int position) {
        Brewery brewery = mBreweryList.get(position);
        holder.nameView.setText(brewery.name);
        holder.addressView.setText(brewery.address);
        holder.distanceView.setText(brewery.getDistanceText(mActivity.getLocation()));

        if (TextUtils.isEmpty(brewery.phone)) {
            holder.phoneView.setVisibility(View.GONE);
        } else {
            holder.phoneView.setText(brewery.phone);
            holder.phoneView.setVisibility(View.VISIBLE);
        }

        String statusText = brewery.getStatusText();
        if (TextUtils.isEmpty(statusText)) {
            holder.statusView.setVisibility(View.GONE);
            holder.servicesView.setVisibility(View.VISIBLE);
            brewery.showServiceIcons(mActivity, holder.servicesView);
        } else {
            holder.statusView.setVisibility(View.VISIBLE);
            holder.servicesView.setVisibility(View.GONE);
            holder.statusView.setText(statusText);
        }
    }

    @Override
    public int getItemCount() {
        return mBreweryList.size();
    }

    public void addItems(List<Brewery> list) {
        this.mBreweryList = orderListByDistance(list, mActivity.getLocation());
        notifyDataSetChanged();
    }

    static class BreweryListViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView addressView;
        private final TextView phoneView;
        private final TextView distanceView;
        private final LinearLayout servicesView;
        private final TextView statusView;

        BreweryListViewHolder(View view) {
            super(view);

            nameView = view.findViewById(R.id.nameView);
            addressView = view.findViewById(R.id.addressView);
            phoneView = view.findViewById(R.id.phoneView);
            distanceView = view.findViewById(R.id.distanceView);
            servicesView = view.findViewById(R.id.servicesView);
            statusView = view.findViewById(R.id.statusView);
        }
    }

    private List<Brewery> orderListByDistance(@NonNull final List<Brewery> list, Location location) {
        List<Brewery> sortedList = new ArrayList<>();
        TreeMap<Float, Brewery> tm = new TreeMap<>();
        Location breweryLocation;
        float distance;

        for (Brewery brewery : list) {
            distance = 0;

            if (location != null) {
                breweryLocation = new Location("");
                breweryLocation.setLatitude(brewery.latitude);
                breweryLocation.setLongitude(brewery.longitude);
                distance = breweryLocation.distanceTo(location);
            }

            tm.put(distance, brewery);
        }

        Set<Map.Entry<Float, Brewery>> set = tm.entrySet();
        for (Map.Entry<Float, Brewery> entry : set) {
            sortedList.add(entry.getValue());
        }

        return sortedList;
    }
}