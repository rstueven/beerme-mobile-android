package com.beerme.android.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	ImageView mView;

	public DownloadImageTask(ImageView bmImage) {
		this.mView = bmImage;
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
		mView.setImageBitmap(result);
	}
}