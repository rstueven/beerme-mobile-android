package com.beerme.android;

import android.content.ContentResolver;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.beerme.android.db.DBHelper;

/**
 * Created by rstueven on 7/10/17.
 * Superclass for (mostly) all Activities.
 */

public class BeerMeActivity extends AppCompatActivity {
    protected final DBHelper dbHelper = DBHelper.getInstance(BeerMeActivity.this);
    protected final ContentResolver contentResolver = dbHelper.getContentResolver();

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, AppSettings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}