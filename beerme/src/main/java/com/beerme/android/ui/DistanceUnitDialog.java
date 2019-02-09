package com.beerme.android.ui;

import android.app.Activity;
import android.content.DialogInterface;

import com.beerme.android.util.Measurer;
import com.beerme.android.util.SharedPref;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class DistanceUnitDialog extends AlertDialog {
    private final Activity mActivity;
    private final int itemCount = Measurer.DistanceUnit.size;
    private final String[] itemList = Measurer.DistanceUnit.names();

    public interface DistanceUnitListener {
        void onDistanceUnitChanged(int distanceUnit);
    }

    public DistanceUnitDialog(@NonNull Activity activity) {
        super(activity);
        this.mActivity = activity;
    }

    public AlertDialog build() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setCancelable(false);

        builder.setTitle("Distance Unit");

        builder.setItems(itemList, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPref.write(SharedPref.Pref.DISTANCE_UNIT, which);

                if (mActivity instanceof DistanceUnitListener) {
                    ((DistanceUnitListener) mActivity).onDistanceUnitChanged(which);
                }
            }
        });


        return builder.create();
    }
}