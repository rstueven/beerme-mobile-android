package com.beerme.android_free.utils;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

// http://dimitar.me/how-to-detect-a-user-pantouchdrag-on-android-map-v2/
public class TouchableWrapper extends RelativeLayout {
	private long mTouchStart = 0;
	private static final long SCROLL_TIME = 48L;
	private UpdateMapAfterUserInteraction mCallback;

	// MapFactory Activity must implement this interface
	public interface UpdateMapAfterUserInteraction {
		public void onUpdateMapAfterUserInteraction();
	}

	public TouchableWrapper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchableWrapper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TouchableWrapper(Context context) {
		super(context);
	}

	public void setUpdateMapAfterUserInteraction(
			UpdateMapAfterUserInteraction callback) {
		this.mCallback = callback;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchStart = SystemClock.uptimeMillis();
			break;
		case MotionEvent.ACTION_UP:
			final long now = SystemClock.uptimeMillis();
			if (now - mTouchStart > SCROLL_TIME) {
				if (mCallback != null) {
					mCallback.onUpdateMapAfterUserInteraction();
				}
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
}