package com.beerme.android.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Created by rstueven on 9/3/17.
 * <p>Zooms an image from thumbnail to fullscreen and back.</p>
 */

public class ImageZoomer {

    public static void zoomImageFromThumb(final Activity activity, final ImageView thumb, final ImageView zoomed) {
        final int animationDuration = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);

//        final Animator animator;
//        if (animator != null) {
//            animator.cancel();
//        }

        final Bitmap bm = ((BitmapDrawable)thumb.getDrawable()).getBitmap();

        zoomed.setImageBitmap(bm);

        // https://developer.android.com/training/animation/zoom.html

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumb.getGlobalVisibleRect(startBounds);
        activity.getWindow().getDecorView().getRootView().getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        final float startScale;
        if (((float) finalBounds.width() / finalBounds.height()) > ((float) startBounds.width() / startBounds.height())) {
            startScale = (float) startBounds.height() / finalBounds.height();
            final float startWidth = startScale * finalBounds.width();
            final float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            startScale = (float) startBounds.width() / finalBounds.width();
            final float startHeight = startScale * finalBounds.height();
            final float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumb.setAlpha(0f);
        zoomed.setVisibility(View.VISIBLE);

        zoomed.setPivotX(0f);
        zoomed.setPivotY(0f);

        final AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(zoomed, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(zoomed, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(zoomed, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(zoomed, View.SCALE_Y, startScale, 1f));
        set.setDuration(animationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
//                animator = null;
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
//                animator = null;
            }
        });

        set.start();
//        animator = set;

        zoomed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
//                if (animator != null) {
//                    animator.cancel();
//                }

                final AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofFloat(zoomed, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(zoomed, View.Y, startBounds.top))
                        .with(ObjectAnimator.ofFloat(zoomed, View.SCALE_X, startScale))
                        .with(ObjectAnimator.ofFloat(zoomed, View.SCALE_Y, startScale));
                set.setDuration(animationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        thumb.setAlpha(1f);
                        zoomed.setVisibility(View.GONE);
//                        animator = null;
                    }

                    @Override
                    public void onAnimationCancel(final Animator animation) {
                        thumb.setAlpha(1f);
                        zoomed.setVisibility(View.GONE);
//                        animator = null;
                    }
                });

                set.start();
//                animator = set;
            }
        });
    }
}