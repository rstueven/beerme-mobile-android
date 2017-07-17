package com.beerme.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private final ImageView mView;

    public DownloadImageTask(final ImageView bmImage) {
        this.mView = bmImage;
    }

    @Override
    protected Bitmap doInBackground(final String... urls) {
        final String urldisplay = urls[0];
        Bitmap bitmap = null;

        final InputStream in;
        try {
            in = new java.net.URL(urldisplay).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (final IOException e) {
            Log.e("beerme", "DownloadImageTask: " + e.getLocalizedMessage() + urldisplay);
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(final Bitmap result) {
        mView.setImageBitmap(result);
    }
}