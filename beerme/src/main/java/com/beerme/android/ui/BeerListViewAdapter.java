package com.beerme.android.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.db.BeerListItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BeerListViewAdapter extends RecyclerView.Adapter<BeerListViewAdapter.BeerListViewHolder> {
    // http://antonioleiva.com/recyclerview-listener/
    public interface OnItemClickListener {
        void onItemClick(@NonNull BeerListItem beer);
    }

    private final Activity mActivity;
    private final OnItemClickListener mListener;
    private List<BeerListItem> mBeerList;

    public BeerListViewAdapter(@NonNull Activity activity, @NonNull List<BeerListItem> list) {
        this.mActivity = activity;
        this.mListener = (OnItemClickListener) activity;
        this.mBeerList = list;
    }

    @NonNull
    @Override
    public BeerListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BeerListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.beer_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BeerListViewHolder holder, int position) {
        BeerListItem beer = mBeerList.get(position);

        holder.bind(beer, mListener);

        holder.nameView.setText(beer.name);
        holder.styleView.setText(beer.stylename);
        holder.abvView.setText(beer.abv == null ? "" : beer.abv + "% abv");

        beer.showStars(mActivity, holder.starsView);
    }

    @Override
    public int getItemCount() {
        return mBeerList.size();
    }

    public void addItems(List<BeerListItem> list) {
        mBeerList = list;
        notifyDataSetChanged();
    }

    static class BeerListViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView nameView;
        private final TextView styleView;
        private final TextView abvView;
        private final LinearLayout starsView;

        BeerListViewHolder(@NonNull View view) {
            super(view);

            mView = view;
            nameView = view.findViewById(R.id.nameView);
            styleView = view.findViewById(R.id.styleView);
            abvView = view.findViewById(R.id.abvView);
            starsView = view.findViewById(R.id.starsView);
        }

        void bind(@NonNull final BeerListItem beer, @NonNull final BeerListViewAdapter.OnItemClickListener listener) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(beer);
                }
            });
        }
    }
}