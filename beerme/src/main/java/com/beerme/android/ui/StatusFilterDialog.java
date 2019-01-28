package com.beerme.android.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.beerme.android.util.SharedPref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusFilterDialog extends AlertDialog {
    private final int itemCount = Brewery.Status.size;
    private final String[] itemList = Brewery.Status.names();
    private final boolean[] checkedItems = getCheckedItems(itemCount);
    private final Activity mActivity;

    public interface StatusFilterListener {
        void onStatusFilterChanged(List<Integer> statusFilter);
    }

    protected StatusFilterDialog(Activity activity) {
        super(activity);
        this.mActivity = activity;
    }

    public AlertDialog build() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setCancelable(false);

        builder.setTitle("Status Filter");

        builder.setMultiChoiceItems(itemList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                Log.d("beerme", "ONCLICK(" + which + ", " + isChecked + ")");
                checkedItems[which] = isChecked;
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("beerme", "OK");
                List<Integer> statusFilterList = new ArrayList<>();

                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        statusFilterList.add(1 << i);
                    }
                }

                SharedPref.writeIntList(SharedPref.Pref.STATUS_FILTER, statusFilterList);

                if (mActivity instanceof StatusFilterListener) {
                    ((StatusFilterListener) mActivity).onStatusFilterChanged(statusFilterList);
                }
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

    // TODO: This "power-of-two" thing doesn't make sense anymore, and should be replaced with an ordinary index.
    private boolean[] getCheckedItems(final int count) {
        boolean[] checkedItems = new boolean[count];
        Arrays.fill(checkedItems, false);
        // TODO: Easier with readIntList?
        int[] statusFilter = SharedPref.readIntArray(SharedPref.Pref.STATUS_FILTER);

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