package com.beerme.android;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by rstueven on 2/25/17.
 * <p/>
 * Catches map touch/pan/move.
 * http://dimitar.me/how-to-detect-a-user-pantouchdrag-on-android-map-v2/
 */

public class TouchableWrapper extends FrameLayout {
    private long lastTouched = 0;
    private static final long SCROLL_TIME = 200L; // 200 Milliseconds, but you can adjust that to your liking
    private UpdateMapAfterUserInteraction updateMapAfterUserInteraction;

    public TouchableWrapper(final Context context) {
        super(context);
        // Force the host activity to implement the UpdateMapAfterUserInteraction Interface
        try {
            updateMapAfterUserInteraction = (UpdateMapAfterUserInteraction) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement UpdateMapAfterUserInteraction");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouched = SystemClock.uptimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                final long now = SystemClock.uptimeMillis();
                if (now - lastTouched > SCROLL_TIME) {
                    // Update the map
                    updateMapAfterUserInteraction.onUpdateMapAfterUserInteraction();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // Map Activity must implement this interface
    public interface UpdateMapAfterUserInteraction {
        public void onUpdateMapAfterUserInteraction();
    }
}