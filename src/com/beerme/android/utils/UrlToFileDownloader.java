/**
 * 
 */
package com.beerme.android.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.util.Log;

import com.beerme.android.R;

/**
 * @author rstueven
 * 
 */
public class UrlToFileDownloader implements Runnable {
	public static int DEFAULT_BUFFER_SIZE = 128 * 1024;
	private Context mContext = null;
	private UrlToFileDownloadListener listener = null;
	private URL url = null;
	private String fileName = null;

	public interface UrlToFileDownloadListener {
		public void onUrlToFileDownloaded(String fileName);
	}

	public UrlToFileDownloader(Context context, String urlString,
			String fileName, UrlToFileDownloadListener listener) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}
		if (listener == null) {
			throw new IllegalArgumentException("null listener");
		}

		mContext = context;

		try {
			this.url = new URL(urlString);
		} catch (MalformedURLException e) {
			ErrLog.log(mContext, "UrlToFileDownloader(" + urlString + ","
					+ fileName + ")", e, R.string.Network_problem);
		}

		this.fileName = fileName;
		this.listener = listener;
	}

	public UrlToFileDownloader(Context context, URL url,
			UrlToFileDownloadListener listener) {
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}
		if (listener == null) {
			throw new IllegalArgumentException("null listener");
		}

		this.url = url;
		this.fileName = null;
		this.listener = listener;
	}

	@Override
	public void run() {
		// http://stackoverflow.com/a/5472726/295028
		File file = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;

		if (Utils.isExternalStorageWritable()) {
			try {
				file = File.createTempFile("beerme", null);
				if (file.exists()) {
					fileName = file.getAbsolutePath();

					URLConnection ucon = url.openConnection();

					char[] inBuffer = new char[DEFAULT_BUFFER_SIZE];

					InputStream is = ucon.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					reader = new BufferedReader(isr, DEFAULT_BUFFER_SIZE);

					FileWriter fw = new FileWriter(file);
					writer = new BufferedWriter(fw);

					int n = 0;
					while ((n = reader.read(inBuffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
						writer.write(inBuffer, 0, n);
					}
				} else {
					Log.w(Utils.APPTAG, file.getAbsolutePath()
							+ " does not exist");
				}
			} catch (IOException e) {
				Log.e(Utils.APPTAG,
						"UrlToFileDownloader.run(" + url.toExternalForm() + ","
								+ fileName + "): " + e.getLocalizedMessage());
				file.delete();
				file = null;
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (writer != null) {
						writer.close();
					}
				} catch (IOException e) {
					// Ignore
				}

				listener.onUrlToFileDownloaded((file != null) ? file
						.getAbsolutePath() : null);
			}
		}
	}

	public static void download(Context context, String urlString,
			UrlToFileDownloadListener listener) throws MalformedURLException {
		download(context, new URL(urlString), listener);
	}

	public static void download(Context context, URL url,
			UrlToFileDownloadListener listener) {
		new Thread(new UrlToFileDownloader(context, url, listener), "Download_"
				+ url.getFile()).start();
	}
}