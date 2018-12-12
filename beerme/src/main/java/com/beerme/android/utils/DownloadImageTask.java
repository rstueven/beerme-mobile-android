package com.beerme.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> mViewReference;

    public DownloadImageTask(ImageView bmImage) {
        this.mViewReference = new WeakReference<>(bmImage);
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap bitmap = null;

        InputStream in;
        try {
            in = new java.net.URL(urldisplay).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            Log.e(Utils.APPTAG, "DownloadImageTask: " + e.getLocalizedMessage() + urldisplay);
        } catch (IOException e) {
            Log.e(Utils.APPTAG, "DownloadImageTask: " + e.getLocalizedMessage() + urldisplay);
        }

        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        mViewReference.get().setImageBitmap(result);
    }
}