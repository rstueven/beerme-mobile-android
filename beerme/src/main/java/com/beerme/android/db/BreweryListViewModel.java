package com.beerme.android.db;

import android.app.Application;

import com.beerme.android.util.SharedPref;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class BreweryListViewModel extends AndroidViewModel {
    private final LiveData<List<Brewery>> breweryList;

    public BreweryListViewModel(@NonNull Application application) {
        super(application);

        BeerMeDatabase db = BeerMeDatabase.getInstance(this.getApplication());

        breweryList = db.breweryDao().getAllBreweriesByStatus(SharedPref.read(SharedPref.Pref.STATUS_FILTER));
    }

    public LiveData<List<Brewery>> getBreweryList() {
        return breweryList;
    }
}