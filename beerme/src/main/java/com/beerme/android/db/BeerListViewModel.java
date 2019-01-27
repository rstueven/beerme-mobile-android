package com.beerme.android.db;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

class BeerListViewModel extends AndroidViewModel {
    private final LiveData<List<Beer>> beerList;

    public BeerListViewModel(@NonNull Application application) {
        super(application);

        BeerMeDatabase db = BeerMeDatabase.getInstance(this.getApplication());
        beerList = db.beerDao().getAllBeers();
    }

    public LiveData<List<Beer>> getBeerList() {
        return beerList;
    }
}