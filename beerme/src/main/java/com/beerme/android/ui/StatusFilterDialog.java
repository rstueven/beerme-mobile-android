package com.beerme.android.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.beerme.android.util.SharedPref;

public class StatusFilterDialog extends AlertDialog {
    private final int itemCount = Brewery.Status.size;
    private final String[] itemList = Brewery.Status.names();
    private final boolean[] checkedItems = getCheckedItems(itemCount);
    private final Activity mActivity;

    public interface StatusFilterListener {
        void onStatusFilterChanged(int statusFilter);
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
                checkedItems[which] = isChecked;
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int statusFilter = 0;

                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        statusFilter += (1 << i);
                    }
                }

                // TODO: Disallow empty status filter.

                SharedPref.write(SharedPref.Pref.STATUS_FILTER, statusFilter);
                if (mActivity instanceof StatusFilterListener) {
                    ((StatusFilterListener) mActivity).onStatusFilterChanged(statusFilter);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        return builder.create();
    }

    private boolean[] getCheckedItems(final int count) {
        boolean[] checkedItems = new boolean[count];

        int statusFilter = SharedPref.read(SharedPref.Pref.STATUS_FILTER, 0);

        for (int i = 0; i < count; i++) {
            checkedItems[i] = (((statusFilter >> i) & 1) == 1);
        }

        return checkedItems;
    }
}