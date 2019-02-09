package com.beerme.android.ui;

import android.content.Intent;
import android.os.Bundle;

import com.beerme.android.util.SharedPref;

/**
 * Just a switch to Map or BreweryList based on the SharedPref setting.
 */
public class MainActivity extends BeerMeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String mapOrList = SharedPref.read(SharedPref.Pref.MAP_OR_LIST, MapOrListDialog.DEFAULT);

        switch (mapOrList) {
            case MapOrListDialog.MAP:
                startActivity(new Intent(this, MapActivity.class));
                break;
            case MapOrListDialog.LIST:
                startActivity(new Intent(this, BreweryListActivity.class));
                break;
            default:
                throw new IllegalArgumentException("MainActivity.onCreate(): unknown mapOrList <" + mapOrList + ">");
        }

        this.finish();
    }
}