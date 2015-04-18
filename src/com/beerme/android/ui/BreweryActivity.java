package com.beerme.android.ui;

import java.io.ByteArrayOutputStream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.database.Brewery;
import com.beerme.android.prefs.BreweryStatusFilterPreference;
import com.beerme.android.ui.actionbar.BeerMeActionBarActivity;
import com.beerme.android.utils.DownloadImageTask;
import com.beerme.android.utils.SendUpdateDialog;
import com.beerme.android.utils.Utils;

public class BreweryActivity extends BeerMeActionBarActivity {
	private final static String TAG_BREWERY_ID = "breweryId";
	private final static String TAG_BREWERY_IMAGE = "breweryImage";
	private long mBreweryId = -1;
	private Brewery mBrewery = null;
	private ImageView breweryImageView = null;
	private Bitmap breweryImageBitmap = null;
	private Animator mCurrentAnimator;
	private int mShortAnimationDuration;
	private ViewPager mViewPager;
	private BreweryDataPagerAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.brewery_activity);

		mBreweryId = getIntent().getLongExtra("id", -1);
		mBrewery = new Brewery(this, mBreweryId);

		if (mBrewery != null) {
			TextView nameView = (TextView) findViewById(R.id.brewery_name);
			nameView.setText(mBrewery.getName());

			TextView addressView = (TextView) findViewById(R.id.brewery_address);
			addressView.setText(mBrewery.getAddress());

			TextView statusView = (TextView) findViewById(R.id.brewery_status);
			int statusCode = mBrewery.getStatus();
			if (statusCode != BreweryStatusFilterPreference.OPEN) {
				String status = Utils.breweryStatusString(this, statusCode);
				statusView.setText(status);
				statusView.setVisibility(View.VISIBLE);
			} else {
				statusView.setVisibility(View.GONE);
			}
		}

		mAdapter = new BreweryDataPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.brewery_data_pager);
		mViewPager.setAdapter(mAdapter);

		breweryImageView = (ImageView) findViewById(R.id.brewery_image);

		if (!mBrewery.getImage().equals("")) {
			breweryImageView.setVisibility(View.VISIBLE);
		} else {
			breweryImageView.setVisibility(View.GONE);
		}

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(TAG_BREWERY_ID)) {
				mBreweryId = savedInstanceState.getLong(TAG_BREWERY_ID);
				mBrewery = new Brewery(this, mBreweryId);
			}
			if (savedInstanceState.containsKey(TAG_BREWERY_IMAGE)) {
				byte[] byteArray = savedInstanceState
						.getByteArray(TAG_BREWERY_IMAGE);
				breweryImageBitmap = BitmapFactory.decodeByteArray(byteArray,
						0, byteArray.length);
			}
		}

		if (breweryImageBitmap == null && !"".equals(mBrewery.getImage())) {
			new DownloadImageTask(breweryImageView)
					.execute(mBrewery.getImage());
		}

		breweryImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomImageFromThumb(breweryImageView,
						Utils.getBitmap(breweryImageView));
			}
		});

		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (breweryImageBitmap != null) {
			breweryImageView.setImageBitmap(breweryImageBitmap);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.trackActivityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Utils.trackActivityStop(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(TAG_BREWERY_ID, mBreweryId);
		if (breweryImageBitmap != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Bitmap bitmap = Utils.getBitmap(breweryImageView);
			bitmap.compress(Bitmap.CompressFormat.PNG, 60, stream);
			outState.putByteArray(TAG_BREWERY_IMAGE, stream.toByteArray());
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.brewery_actions, menu);

		if (mBrewery.getPhone().equals("")) {
			menu.findItem(R.id.action_call).setVisible(false);
		}

		if (mBrewery.getWeb().equals("")) {
			menu.findItem(R.id.action_web).setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentManager fm = getSupportFragmentManager();
		Intent intent;

		switch (item.getItemId()) {
		case R.id.action_call:
			final boolean telAvailable = Utils.isFeatureAvailable(this,
					"android.hardware.telephony");
			final String telno = mBrewery.getPhone();
			if (!telno.equals("")) {
				if (telAvailable) {
					String tel = mBrewery.getPhone().replaceAll("\\(0\\)", "");
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"
							+ tel));
					startActivity(intent);
				} else {
					Bundle args = new Bundle();
					args.putString("telno", telno);
					DialogFrag dialog = DialogFrag.newInstance(
							DialogFrag.Mode.SHOW_TELNO, args);
					dialog.show(fm, "telno");
				}
			}
			return true;
		case R.id.action_web:
			final String web = mBrewery.getWeb();
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + web));
			startActivity(intent);
			return true;
		case R.id.action_map:
			intent = MapFactory.newIntent(this, mBrewery);
			startActivity(intent);
			return true;
		case R.id.action_update:
			SendUpdateDialog updateDialog = SendUpdateDialog
					.newInstance(mBrewery.getId());
			updateDialog.show(fm, "update");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class BreweryDataPagerAdapter extends FragmentPagerAdapter {
		private static final int NUM_PAGES = 3;

		public BreweryDataPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return HoursFrag.getInstance(mBrewery.getId());
			case 1:
				return BeerListFrag.getInstance(mBreweryId);
			case 2:
				return BreweryNotesFrag.getInstance(mBreweryId);
			}
			return null;
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.Hours_Services);
			case 1:
				return getString(R.string.Beer_list);
			case 2:
				return getString(R.string.Brewery_notes);
			}
			return "Unknown";
		}
	}

	// http://developer.android.com/training/animation/zoom.html
	@SuppressLint("NewApi")
	private void zoomImageFromThumb(final View thumbView, Bitmap bitmap) {
		// Load the high-resolution "zoomed-in" image.
		final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_brewery_image);
		expandedImageView.setImageBitmap(bitmap);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			// If there's an animation in progress, cancel it immediately and
			// proceed with this one.
			if (mCurrentAnimator != null) {
				mCurrentAnimator.cancel();
			}

			// Calculate the starting and ending bounds for the zoomed-in image.
			// This step involves lots of math. Yay, math.
			final Rect startBounds = new Rect();
			final Rect finalBounds = new Rect();
			final Point globalOffset = new Point();

			// The start bounds are the global visible rectangle of the
			// thumbnail, and the final bounds are the global visible rectangle
			// of the container view. Also set the container view's offset as
			// the origin for the bounds, since that's the origin for the
			// positioning animation properties (X, Y).
			thumbView.getGlobalVisibleRect(startBounds);
			findViewById(R.id.container).getGlobalVisibleRect(finalBounds,
					globalOffset);
			startBounds.offset(-globalOffset.x, -globalOffset.y);
			finalBounds.offset(-globalOffset.x, -globalOffset.y);

			// Adjust the start bounds to be the same aspect ratio as the final
			// bounds using the "center crop" technique. This prevents
			// undesirable stretching during the animation. Also calculate the
			// start scaling factor (the end scaling factor is always 1.0).
			float startScale;
			if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds
					.width() / startBounds.height()) {
				// Extend start bounds horizontally
				startScale = (float) startBounds.height()
						/ finalBounds.height();
				float startWidth = startScale * finalBounds.width();
				float deltaWidth = (startWidth - startBounds.width()) / 2;
				startBounds.left -= deltaWidth;
				startBounds.right += deltaWidth;
			} else {
				// Extend start bounds vertically
				startScale = (float) startBounds.width() / finalBounds.width();
				float startHeight = startScale * finalBounds.height();
				float deltaHeight = (startHeight - startBounds.height()) / 2;
				startBounds.top -= deltaHeight;
				startBounds.bottom += deltaHeight;
			}

			// Hide the thumbnail and show the zoomed-in view. When the
			// animation begins, it will position the zoomed-in view in the
			// place of the thumbnail.
			thumbView.setAlpha(0f);
			expandedImageView.setVisibility(View.VISIBLE);

			// Set the pivot point for SCALE_X and SCALE_Y transformations to
			// the top-left corner of the zoomed-in view (the default is the
			// center of the view).
			expandedImageView.setPivotX(0f);
			expandedImageView.setPivotY(0f);

			// Construct and run the parallel animation of the four translation
			// and scale properties (X, Y, SCALE_X, and SCALE_Y).
			AnimatorSet set = new AnimatorSet();
			set.play(
					ObjectAnimator.ofFloat(expandedImageView, View.X,
							startBounds.left, finalBounds.left))
					.with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
							startBounds.top, finalBounds.top))
					.with(ObjectAnimator.ofFloat(expandedImageView,
							View.SCALE_X, startScale, 1f))
					.with(ObjectAnimator.ofFloat(expandedImageView,
							View.SCALE_Y, startScale, 1f));
			set.setDuration(mShortAnimationDuration);
			set.setInterpolator(new DecelerateInterpolator());
			set.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mCurrentAnimator = null;
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					mCurrentAnimator = null;
				}
			});
			set.start();
			mCurrentAnimator = set;

			// Upon clicking the zoomed-in image, it should zoom back down to
			// the original bounds and show the thumbnail instead of the
			// expanded image.
			final float startScaleFinal = startScale;
			expandedImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mCurrentAnimator != null) {
						mCurrentAnimator.cancel();
					}

					// Animate the four positioning/sizing properties in
					// parallel, back to their original values.
					AnimatorSet set = new AnimatorSet();
					set.play(
							ObjectAnimator.ofFloat(expandedImageView, View.X,
									startBounds.left))
							.with(ObjectAnimator.ofFloat(expandedImageView,
									View.Y, startBounds.top))
							.with(ObjectAnimator.ofFloat(expandedImageView,
									View.SCALE_X, startScaleFinal))
							.with(ObjectAnimator.ofFloat(expandedImageView,
									View.SCALE_Y, startScaleFinal));
					set.setDuration(mShortAnimationDuration);
					set.setInterpolator(new DecelerateInterpolator());
					set.addListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							thumbView.setAlpha(1f);
							expandedImageView.setVisibility(View.GONE);
							mCurrentAnimator = null;
						}

						@Override
						public void onAnimationCancel(Animator animation) {
							thumbView.setAlpha(1f);
							expandedImageView.setVisibility(View.GONE);
							mCurrentAnimator = null;
						}
					});
					set.start();
					mCurrentAnimator = set;
				}
			});
		} else {
			// Pre-Honeycomb
			thumbView.setVisibility(View.INVISIBLE);
			expandedImageView.setVisibility(View.VISIBLE);
			expandedImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					thumbView.setVisibility(View.VISIBLE);
					expandedImageView.setVisibility(View.GONE);
				}
			});
		}
	}
}