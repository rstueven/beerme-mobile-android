package com.beerme.android.util;

import android.content.Context;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.drawable.DrawableCompat;

public class ToolbarIconTinter {
    // There might be a way to do this using styles/themes.
    // https://www.codeandkits.com/Blog/2018/03/26/an-android-toolbar-and-action-bar-color-guide/
    public static void tintIcons(@NonNull final Context context, @NonNull final Menu menu) {
        MenuItem item;
        final int color = Color.parseColor("#ffffff");

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        for (int i = 0; i < menu.size(); i++) {
            item = menu.getItem(i);
            DrawableCompat.setTint(item.getIcon(), color);
        }
    }
}