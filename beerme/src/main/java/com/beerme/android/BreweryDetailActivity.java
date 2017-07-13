package com.beerme.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beerme.android.model.Brewery;
import com.beerme.android.model.Services;
import com.beerme.android.util.DownloadImageTask;

public class BreweryDetailActivity extends BeerMeActivity {
    int id = -1;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewery_detail);

        final Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);
        if (id <= 0) {
            throw new IllegalArgumentException("invalid brewery ID: " + id);
        }

        try {
            final Brewery brewery = new Brewery(this, id);

            final TextView nameView = (TextView) findViewById(R.id.name);
            nameView.setText(brewery.getName());

            final TextView addressView = (TextView) findViewById(R.id.address);
            final String address = brewery.getAddress();
            addressView.setText(address);
            if ((address == null) || address.isEmpty()) {
                addressView.setVisibility(View.GONE);
            }

            final TextView hoursView = (TextView) findViewById(R.id.hours);
            final String hours = brewery.getHours();
            hoursView.setText(hours);
            if ((hours == null) || hours.isEmpty()) {
                hoursView.setVisibility(View.GONE);
            }

            final TableLayout svcView = Services.serviceView(this, brewery.getServices());
            ((FrameLayout) findViewById(R.id.services)).addView(svcView);

            final TextView phoneView = (TextView) findViewById(R.id.phone);
            final String phone = brewery.getPhone();
            if ((phone == null) || phone.isEmpty()) {
                phoneView.setVisibility(View.GONE);
            } else {
                phoneView.setText(phone);
                phoneView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phone));
                        startActivity(intent);
                    }
                });
            }

            final TextView webView = (TextView) findViewById(R.id.web);
            final String web = brewery.getWebForDisplay();
            if ((web == null) || web.isEmpty()) {
                webView.setVisibility(View.GONE);
            } else {
                webView.setText(web);
                webView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(brewery.getWeb()));
                        startActivity(intent);
                    }
                });
            }

            final TextView beerListView = (TextView) findViewById(R.id.beerlist);
            beerListView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Intent intent = new Intent(BreweryDetailActivity.this, BeerListActivity.class);
                    intent.putExtra("id", brewery.getId());
                    intent.putExtra("name", brewery.getName());
                    startActivity(intent);
                }
            });

            final ImageView imageView = (ImageView) findViewById(R.id.image);
            final String breweryImage = brewery.getImage();
            if ((breweryImage == null) || breweryImage.isEmpty()) {
                imageView.setVisibility(View.GONE);
            } else {
                final StringBuilder urlBuilder = new StringBuilder("http://beerme.com/graphics/brewery/" + (brewery.getId() / 1000) + "/" + brewery.getId() + "/");
                switch (breweryImage.charAt(0)) {
                    case 'P':
                        // Premises
                        urlBuilder.append("premises.png");
                        break;
                    case 'G':
                        // Generic
                        urlBuilder.append("generic.png");
                        break;
                    default:
                        urlBuilder.delete(0, urlBuilder.length());
                        imageView.setVisibility(View.GONE);
                }

                if (urlBuilder.length() > 0) {
                    final String imageUrl = urlBuilder.toString();
                    new DownloadImageTask(imageView).execute(imageUrl);
                    mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            zoomImageFromThumb(imageView, imageUrl);
                        }
                    });
                }
            }
        } catch (final IllegalArgumentException e) {
            Toast.makeText(this, "Database error: Illegal brewery ID " + id, Toast.LENGTH_LONG).show();
            Log.e("beerme", e.getLocalizedMessage());
            this.finish();
        }
    }

    private void zoomImageFromThumb(final View thumbView, final String imageUrl) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
        // TODO: Cache the image to save network access
        new DownloadImageTask(expandedImageView).execute(imageUrl);
//        expandedImageView.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.brewery_detail_layout).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        final float startScale;
        if (((float) finalBounds.width() / finalBounds.height())
                > ((float) startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            final float startWidth = startScale * finalBounds.width();
            final float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            final float startHeight = startScale * finalBounds.height();
            final float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        final AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                final AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,startBounds.top))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(final Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}