package com.beerme.android.db;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class BeerListViewModel extends AndroidViewModel {
    private LiveData<List<Beer>> beerList;

    public BeerListViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Beer>> getBeerList() {
        BeerMeDatabase db = BeerMeDatabase.getInstance(this.getApplication());
        beerList = db.beerDao().getAllBeers();
        return beerList;
    }

    public LiveData<List<Beer>> getBeerListByBreweryId(final long breweryId) {
        BeerMeDatabase db = BeerMeDatabase.getInstance(this.getApplication());
        beerList = db.beerDao().getBeersByBreweryId(breweryId);
        return beerList;
    }
}