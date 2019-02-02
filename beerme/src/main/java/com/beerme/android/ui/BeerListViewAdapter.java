package com.beerme.android.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.db.Beer;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BeerListViewAdapter extends RecyclerView.Adapter<BeerListViewAdapter.BeerListViewHolder> {
    // http://antonioleiva.com/recyclerview-listener/
    public interface OnItemClickListener {
        void onItemClick(@NonNull Beer beer);
    }

    private OnItemClickListener mListener;
    private List<Beer> mBeerList;

    public BeerListViewAdapter(@NonNull Activity activity, @NonNull List<Beer> list) {
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
        Beer beer = mBeerList.get(position);

        holder.bind(beer, mListener);

        holder.nameView.setText(beer.name);
        holder.styleView.setText(beer.style + " ");
        holder.abvView.setText(beer.abv + " ");
//        holder.stars
    }

    @Override
    public int getItemCount() {
        return mBeerList.size();
    }

    public void addItems(List<Beer> list) {
        mBeerList = list;
        notifyDataSetChanged();
    }

    static class BeerListViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView nameView;
        private final TextView styleView;
        private final TextView abvView;
        private final ImageView starsView;

        public BeerListViewHolder(@NonNull View view) {
            super(view);

            mView = view;
            nameView = view.findViewById(R.id.nameView);
            styleView = view.findViewById(R.id.styleView);
            abvView = view.findViewById(R.id.abvView);
            starsView = view.findViewById(R.id.starsView);
        }

        public void bind(@NonNull final Beer beer, @NonNull final BeerListViewAdapter.OnItemClickListener listener) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(beer);
                }
            });
        }
    }
}