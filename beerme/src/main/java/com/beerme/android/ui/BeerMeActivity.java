package com.beerme.android.ui;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.beerme.android.R;
import com.beerme.android.util.ToolbarIconTinter;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BeerMeActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.brewerylist_menu, menu);
        ToolbarIconTinter.tintIcons(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_or_list:
                new MapOrListDialog(this).build().show();
                return true;
            case R.id.status_filter:
                new StatusFilterDialog(this).build().show();
                return true;
            case R.id.distance_unit:
                new DistanceUnitDialog(this).build().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}