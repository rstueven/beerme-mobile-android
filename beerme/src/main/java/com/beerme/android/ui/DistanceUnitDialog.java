package com.beerme.android.ui;

import android.app.Activity;
import android.content.DialogInterface;

import com.beerme.android.R;
import com.beerme.android.util.Measurer;
import com.beerme.android.util.SharedPref;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class DistanceUnitDialog extends AlertDialog {
    private final Activity mActivity;
    private final int itemCount = Measurer.DistanceUnit.size;
    private final String[] itemList = Measurer.DistanceUnit.names();
    private int distanceUnit;

    public interface DistanceUnitListener {
        void onDistanceUnitChanged(int distanceUnit);
    }

    public DistanceUnitDialog(@NonNull Activity activity) {
        super(activity);
        this.mActivity = activity;
        distanceUnit = SharedPref.read(SharedPref.Pref.DISTANCE_UNIT, Measurer.DistanceUnit.DEFAULT);
    }

    public AlertDialog build() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        builder.setTitle(R.string.distance_unit);

        builder.setSingleChoiceItems(itemList, distanceUnit, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                distanceUnit = which;
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                SharedPref.write(SharedPref.Pref.DISTANCE_UNIT, distanceUnit);

                if (mActivity instanceof DistanceUnitListener) {
                    ((DistanceUnitListener) mActivity).onDistanceUnitChanged(distanceUnit);
                }
            }
        });

        return builder.create();
    }
}