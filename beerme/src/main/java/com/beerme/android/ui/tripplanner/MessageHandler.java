package com.beerme.android.ui.tripplanner;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.utils.ErrLog;

public class MessageHandler extends Handler {
	public static final int DIRECTIONS_START = 1;
	public static final int DIRECTIONS_END = 2;
	public static final int SEGMENTS_START = 3;
	public static final int SEGMENTS_END = 4;
	public static final int SEGMENTS_INCREMENT = 5;
	public static final int BREWERIES_START = 6;
	public static final int BREWERIES_END = 7;
	public static final int BREWERIES_INCREMENT = 8;
	public static final int DISPLAY_EXCEPTION = 9;

	private WeakReference<TripPlannerFrag> mRef;

	public interface MessageListener {
		public void postEmptyMessage(int what);

		public void postMessage(int what, int arg1, int arg2);

		public void postException(Exception e);
	}

	MessageHandler(TripPlannerFrag frag) {
		mRef = new WeakReference<TripPlannerFrag>(frag);
	}

	@Override
	public void handleMessage(Message msg) {
		TripPlannerFrag frag = mRef.get();
		ProgressBar bar = frag.getProgressBar();
		TextView text = frag.getProgressText();

		switch (msg.what) {
		case DIRECTIONS_START:
			bar.setIndeterminate(true);
			text.setText(R.string.Calculating_directions);
			break;
		case DIRECTIONS_END:
			bar.setIndeterminate(false);
			text.setText("");
			break;
		case SEGMENTS_START:
			bar.setIndeterminate(false);
			int nSegments = msg.arg1;
			bar.setMax(nSegments);
			text.setText(R.string.Analyzing_route);
			break;
		case SEGMENTS_END:
			bar.setIndeterminate(false);
			bar.setProgress(0);
			text.setText("");
			break;
		case SEGMENTS_INCREMENT:
			bar.setProgress(msg.arg1);
			break;
		case BREWERIES_START:
			bar.setIndeterminate(false);
			int nBreweries = msg.arg1;
			bar.setMax(nBreweries);
			text.setText(frag.getString(R.string.Loading_N_breweries, nBreweries));
			break;
		case BREWERIES_END:
			bar.setIndeterminate(false);
			bar.setProgress(0);
			text.setText("");
			break;
		case BREWERIES_INCREMENT:
			bar.incrementProgressBy(1);
			break;
		case DISPLAY_EXCEPTION:
			Exception e = (Exception) (msg.obj);
			ErrLog.log(frag.getActivity(), "MessageHandler.handleMessage("
					+ msg.what + ")", e, "Bad message: " + e.getLocalizedMessage());
			break;
		default:
			super.handleMessage(msg);
		}
	}
}