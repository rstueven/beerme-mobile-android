package com.beerme.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beerme.android.R;
import com.beerme.android.db.Beer;
import com.beerme.android.db.BeerListViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BeerListFragment extends Fragment {
    private long breweryId;
    private BeerListViewAdapter beerListViewAdapter;
    private List<Beer> beerList;

    static BeerListFragment getInstance(long breweryId) {
        BeerListFragment frag = new BeerListFragment();
        Bundle args = new Bundle();
        args.putLong("breweryId", breweryId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args == null) {
            throw new IllegalStateException("BeerListFragment.onCreate(): null args");
        }

        breweryId = args.getLong("breweryId");

        if (breweryId <= 0L) {
            throw new IllegalArgumentException("BeerListFragment.onCreate(): null breweryId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beer_list, container, false);

        final Activity activity = getActivity();

        if (activity != null) {
            RecyclerView recyclerView = view.findViewById(R.id.beer_list_view);
            beerListViewAdapter = new BeerListViewAdapter(activity, new ArrayList<Beer>());
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));

            recyclerView.setAdapter(beerListViewAdapter);

            BeerListViewModel beerListViewModel = ViewModelProviders.of(this).get(BeerListViewModel.class);

            beerListViewModel.getBeerListByBreweryId(breweryId).observe(this, new Observer<List<Beer>>() {
                @Override
                public void onChanged(List<Beer> beers) {
                    beerList = beers;
                    beerListViewAdapter.addItems(beerList);
                }
            });
        }
        return view;
    }
}