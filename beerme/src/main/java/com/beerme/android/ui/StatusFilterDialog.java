package com.beerme.android.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.beerme.android.util.SharedPref;

import java.util.Arrays;

public class StatusFilterDialog extends AlertDialog {
    protected StatusFilterDialog(Context context) {
        super(context);
    }

    public AlertDialog build() {
        final int itemCount = Brewery.Status.size;
        String[] itemList = Brewery.Status.names();
        boolean[] checkedItems = getCheckedItems(itemCount);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(itemList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                Log.d("beerme", "ONCLICK(" + which + ", " + isChecked + ")");
            }
        });
        builder.setCancelable(false);
        builder.setTitle("Status Filter");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("beerme", "OK");
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("beerme", "CANCEL");
            }
        });
        return builder.create();
    }

    private boolean[] getCheckedItems(final int count) {
        boolean[] checkedItems = new boolean[count];
        Arrays.fill(checkedItems, false);
        int[] statusFilter = SharedPref.read(SharedPref.Pref.STATUS_FILTER);

        for (int status : statusFilter) {
            int s = status;
            for (int i = 0; i < count; i++) {
                if (s == 1) {
                    checkedItems[i] = true;
                    break;
                } else {
                    s = s >> 1;
                }
            }
        }

        return checkedItems;
    }
}