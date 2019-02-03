package com.beerme.android.db;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class BeerListViewModel extends AndroidViewModel {
    private LiveData<List<BeerListItem>> beerList;

    public BeerListViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<BeerListItem>> getBeerListByBreweryId(final long breweryId) {
        BeerMeDatabase db = BeerMeDatabase.getInstance(this.getApplication());
        return db.beerDao().getBeersByBreweryId(breweryId);
    }
}