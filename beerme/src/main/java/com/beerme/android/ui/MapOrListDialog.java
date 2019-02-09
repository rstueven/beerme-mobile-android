package com.beerme.android.ui;

import android.app.Activity;
import android.content.DialogInterface;

import com.beerme.android.R;
import com.beerme.android.util.SharedPref;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class MapOrListDialog extends AlertDialog {
    static final String MAP = "Map";
    static final String LIST = "List";
    public static final String DEFAULT = MAP;

    private final Activity mActivity;
    private static final String[] itemList = {MAP, LIST};
    private static final int itemListLength = itemList.length;
    private String mapOrList;

    public interface MapOrListListener {
        void onMapOrListChanged(@NonNull String mapOrList);
    }

    public MapOrListDialog(@NonNull Activity activity) {
        super(activity);
        this.mActivity = activity;
        mapOrList = SharedPref.read(SharedPref.Pref.MAP_OR_LIST, DEFAULT);
    }

    public AlertDialog build() {
        Builder builder = new Builder(mActivity);
        builder.setTitle(R.string.map_or_list);

        int checkedItem = 0;
        for (int i = 0; i < itemListLength; i++) {
            if (mapOrList.equals(itemList[i])) {
                checkedItem = i;
                break;
            }
        }

        builder.setSingleChoiceItems(itemList, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mapOrList = itemList[which];
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                SharedPref.write(SharedPref.Pref.MAP_OR_LIST, mapOrList);

                if (mActivity instanceof MapOrListListener) {
                    ((MapOrListListener) mActivity).onMapOrListChanged(mapOrList);
                }
            }
        });

        return builder.create();
    }
}