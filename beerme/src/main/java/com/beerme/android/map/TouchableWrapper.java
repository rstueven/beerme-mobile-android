package com.beerme.android.map;

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
    private final Context context;
    private long lastTouched;
    private static final long SCROLL_TIME = 200L; // 200 Milliseconds, but you can adjust that to your liking

    // Map Activity must implement this interface
    public interface UpdateMapAfterUserInteraction {
        void onUpdateMapAfterUserInteraction();
    }

    public TouchableWrapper(final Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouched = SystemClock.uptimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                final long now = SystemClock.uptimeMillis();
                if ((now - lastTouched) > SCROLL_TIME) {
                    ((UpdateMapAfterUserInteraction) context).onUpdateMapAfterUserInteraction();
                }
                break;
            default:
//                Log.e("beerme", "dispatchTouchEvent(" + ev.getAction() + "): Unknown action");
        }

        return super.dispatchTouchEvent(ev);
    }
}