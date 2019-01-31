package com.beerme.android.ui;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.beerme.android.util.LocationActivity;
import com.beerme.android.util.SharedPref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BreweryListViewAdapter extends RecyclerView.Adapter<BreweryListViewAdapter.BreweryListViewHolder> {
    // http://antonioleiva.com/recyclerview-listener/
    public interface OnItemClickListener {
        void onItemClick(@NonNull Brewery brewery);
    }

    private final LocationActivity mActivity;
    private OnItemClickListener mListener;
    private List<Brewery> mBreweryList;

    public BreweryListViewAdapter(@NonNull LocationActivity activity, @NonNull List<Brewery> breweryList) {
        this.mActivity = activity;
        this.mListener = (OnItemClickListener) activity;
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

        holder.bind(brewery, mListener);

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
        private final View mView;
        private final TextView nameView;
        private final TextView addressView;
        private final TextView phoneView;
        private final TextView distanceView;
        private final LinearLayout servicesView;
        private final TextView statusView;

        BreweryListViewHolder(View view) {
            super(view);

            mView = view;
            nameView = view.findViewById(R.id.nameView);
            addressView = view.findViewById(R.id.addressView);
            phoneView = view.findViewById(R.id.phoneView);
            distanceView = view.findViewById(R.id.distanceView);
            servicesView = view.findViewById(R.id.servicesView);
            statusView = view.findViewById(R.id.statusView);
        }

        public void bind(@NonNull final Brewery brewery, @NonNull final OnItemClickListener listener) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(brewery);
                }
            });
        }
    }

    private List<Brewery> orderListByDistance(@NonNull final List<Brewery> breweryList, Location location) {
        Log.d("beerme", "BreweryListViewAdapter.orderListByDistance()");
        int statusFilter = SharedPref.read(SharedPref.Pref.STATUS_FILTER, 0);
        // TODO: Alert if statusFilter == 0

        if (location == null || breweryList.size() == 0) {
            return breweryList;
        }

        SortedMap<Double, Brewery> sortedMap = Collections.synchronizedSortedMap(new TreeMap<Double, Brewery>());
        double distance;
        Location breweryLocation;

        for (Brewery brewery : breweryList) {
            if ((brewery.status & statusFilter) != 0) {
                breweryLocation = new Location("");
                breweryLocation.setLatitude(brewery.latitude);
                breweryLocation.setLongitude(brewery.longitude);
                distance = breweryLocation.distanceTo(location);
                sortedMap.put(distance, brewery);
            }
        }

        return new ArrayList<>(sortedMap.values());
    }
}