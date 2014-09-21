package com.beerme.android_free.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.beerme.android_free.utils.Utils;

public class BeerNote_BeerMe extends BeerNote {
	private static final long serialVersionUID = -2476005905314439866L;
	private static final String noteUrl = Utils.BEERME_URL + "beerNote.php";

	public BeerNote_BeerMe(Context context, long id) {
		super(context, id);
		this.mSource = Source.BEERME;
	}

	@Override
	public void load(Context context) {
		URL url;
		try {
			String noteUrlString = noteUrl + "?i=" + id;
			url = new URL(noteUrlString);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String noteString = in.readLine();
			in.close();

			if (noteString != null) {
				String[] fields = noteString.split("\\|", -1);

				try {
					id = Long.parseLong(fields[0]);
				} catch (NumberFormatException e) {
					// Log.i(Utils.APPTAG, e.getLocalizedMessage());
					id = -1;
				}
				try {
					beerid = Integer.parseInt(fields[1]);
				} catch (NumberFormatException e) {
					// Log.i(Utils.APPTAG, e.getLocalizedMessage());
					beerid = -1;
				}
				pkg = fields[2];
				sampled = fields[3];
				place = fields[4];
				try {
					appscore = Float.parseFloat(fields[5]);
				} catch (NumberFormatException e) {
					// Log.i(Utils.APPTAG, e.getLocalizedMessage());
					appscore = 0;
				}
				appearance = fields[6];
				try {
					aroscore = Float.parseFloat(fields[7]);
				} catch (NumberFormatException e) {
					// Log.i(Utils.APPTAG, e.getLocalizedMessage());
					aroscore = 0;
				}
				aroma = fields[8];
				try {
					mouscore = Float.parseFloat(fields[9]);
				} catch (NumberFormatException e) {
					// Log.i(Utils.APPTAG, e.getLocalizedMessage());
					mouscore = 0;
				}
				mouthfeel = fields[10];
				try {
					ovrscore = Float.parseFloat(fields[11]);
				} catch (NumberFormatException e) {
					// Log.i(Utils.APPTAG, e.getLocalizedMessage());
					ovrscore = 0;
				}
				notes = fields[12];
			} else {
				Toast.makeText(context, "null noteString", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (MalformedURLException e) {
			Log.e(Utils.APPTAG,
					"noteUrl:" + noteUrl + ":" + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e(Utils.APPTAG,
					"noteUrl:" + noteUrl + ":" + e.getLocalizedMessage());
		}
	}

	@Override
	public void save(Context context) {
		Log.e(Utils.APPTAG, "BeerNote_BeerMe.save()");
	}

	@Override
	public void delete(Context context) {
		Log.e(Utils.APPTAG, "BeerNote_BeerMe.delete()");
	}
}