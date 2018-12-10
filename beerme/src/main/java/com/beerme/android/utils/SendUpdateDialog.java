package com.beerme.android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.database.Brewery;
import com.beerme.android.prefs.BreweryStatusFilterPreference;
import com.beerme.android.utils.StatusPickerFragment.StatusSetter;

import java.util.Date;

public class SendUpdateDialog extends DialogFragment implements
		StatusPickerFragment.StatusSetter {
	private static final String TAG_ID = "id";
	private long mBreweryId = -1;
	private Brewery mBrewery = null;
	private EditText mNameView = null;
	private EditText mAddressView = null;
	private EditText mPhoneView = null;
	private TextView mStatusView = null;
	private EditText mWebView = null;
	private CheckBox mOpenBox = null;
	private LinearLayout mOpenLayout = null;
	private EditText mHoursView = null;
	private CheckBox mBarBox = null;
	private CheckBox mBeerGardenBox = null;
	private CheckBox mFoodBox = null;
	private CheckBox mGiftShopBox = null;
	private CheckBox mHotelBox = null;
	private CheckBox mInternetBox = null;
	private CheckBox mRetailBox = null;
	private CheckBox mToursBox = null;
	private EditText mCommentsView = null;
	private String mName = "";
	private String mAddress = "";
	private String mPhone = "";
	private String mStatus = "";
	private String mWeb = "";
	private boolean mOpen = false;
	private String mHours = "";
	private boolean mBar = false;
	private boolean mBeerGarden = false;
	private boolean mFood = false;
	private boolean mGiftShop = false;
	private boolean mHotel = false;
	private boolean mInternet = false;
	private boolean mRetail = false;
	private boolean mTours = false;

	// MEDIUM: Doesn't scroll when keyboard is displayed and Open to Public is unchecked.
	public static SendUpdateDialog newInstance(long breweryId) {
		SendUpdateDialog frag = new SendUpdateDialog();
		Bundle args = new Bundle();
		args.putLong(TAG_ID, breweryId);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.setRetainInstance(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Utils.trackFragment(this);
		
		Bundle args = getArguments();
		if (args != null) {
			mBreweryId = args.getLong(TAG_ID);
		}
		
		// setStyle(R.style.BeerMeTheme, R.style.BeerMeTheme);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View view = getActivity().getLayoutInflater().inflate(R.layout.sendupdate_dialog, (ViewGroup) null);
		builder.setView(view)
				.setInverseBackgroundForced(true)
				.setTitle((mBreweryId > 0) ? R.string.Update : R.string.Add_a_brewery)
				.setPositiveButton(R.string.Submit, submitListener)
				.setNegativeButton(R.string.Cancel, null);

		mNameView = (EditText) view.findViewById(R.id.fixerror_name);
		mAddressView = (EditText) view.findViewById(R.id.fixerror_address);
		mPhoneView = (EditText) view.findViewById(R.id.fixerror_phone);
		mStatusView = (TextView) view.findViewById(R.id.fixerror_status);
		mStatusView.setOnClickListener(new OnStatusViewClicked(this));
		mWebView = (EditText) view.findViewById(R.id.fixerror_web);
		mOpenBox = (CheckBox) view.findViewById(R.id.fixerror_openPublic);
		mOpenBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mOpenLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			}
		});
		mOpenLayout = (LinearLayout) view
				.findViewById(R.id.fixerror_open_details);
		mHoursView = (EditText) view.findViewById(R.id.fixerror_hours);
		mBarBox = (CheckBox) view.findViewById(R.id.fixerror_bar);
		mBeerGardenBox = (CheckBox) view.findViewById(R.id.fixerror_beergarden);
		mFoodBox = (CheckBox) view.findViewById(R.id.fixerror_food);
		mGiftShopBox = (CheckBox) view.findViewById(R.id.fixerror_giftshop);
		mHotelBox = (CheckBox) view.findViewById(R.id.fixerror_hotel);
		mInternetBox = (CheckBox) view.findViewById(R.id.fixerror_internet);
		mRetailBox = (CheckBox) view.findViewById(R.id.fixerror_retail);
		mToursBox = (CheckBox) view.findViewById(R.id.fixerror_tours);
		mCommentsView = (EditText) view.findViewById(R.id.fixerror_comments);

		if (mBreweryId > 0) {
			loadFields();
		}

		return builder.create();
	}

	private DialogInterface.OnClickListener submitListener = new DialogInterface.OnClickListener() {
		// LOW: AND0100: RFE: Autofill user name, email headers
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String s;

			StringBuffer body = new StringBuffer("Beer Me! Brewery Update\n");

			Date now = new Date();
			body.append("Submitted: " + now.toString() + "\n");
			body.append("Referer: " + Utils.getAppVersion() + " ("
					+ Utils.getPlatformVersion() + ")\n");
			body.append("Brewery ID: " + mBreweryId + "\n");

			if (!mName.equals((s = mNameView.getText().toString()))) {
				body.append("Brewery Name: " + s + "\n");
			}

			if (!mAddress.equals((s = mAddressView.getText().toString()))) {
				body.append("Address: " + s + "\n");
			}

			if (!mPhone.equals((s = mPhoneView.getText().toString()))) {
				body.append("Telephone: " + s + "\n");
			}

			if (!mStatus.equals((s = mStatusView.getText().toString()))) {
				body.append("Status: " + s + "\n");
			}

			if (!mWeb.equals((s = mWebView.getText().toString()))) {
				body.append("Web Site(s): " + s + "\n");
			}

			boolean b;

			if ((b = mOpenBox.isChecked()) != mOpen) {
				body.append("Open to the public: " + (b ? "Yes" : "No") + "\n");
			}

			if (!mHours.equals((s = mHoursView.getText().toString()))) {
				body.append("Hours of Operation: " + s + "\n");
			}

			if ((b = mBarBox.isChecked()) != mBar) {
				body.append("Bar/Tasting Room: " + (b ? "Yes" : "No") + "\n");
			}

			if ((b = mBeerGardenBox.isChecked()) != mBeerGarden) {
				body.append("Beer Garden: " + (b ? "Yes" : "No") + "\n");
			}

			if ((b = mFoodBox.isChecked()) != mFood) {
				body.append("Food: " + (b ? "Yes" : "No") + "\n");
			}

			if ((b = mHotelBox.isChecked()) != mHotel) {
				body.append("Hotel Rooms: " + (b ? "Yes" : "No") + "\n");
			}

			if ((b = mGiftShopBox.isChecked()) != mGiftShop) {
				body.append("Items for Sale: " + (b ? "Yes" : "No") + "\n");
			}

			if ((b = mRetailBox.isChecked()) != mRetail) {
				body.append("Beer to Go: " + (b ? "Yes" : "No") + "\n");
			}

			if ((b = mToursBox.isChecked()) != mTours) {
				body.append("Tours: " + (b ? "Yes" : "No") + "\n");
			}

			if ((b = mInternetBox.isChecked()) != mInternet) {
				body.append("Internet Access: " + (b ? "Yes" : "No") + "\n");
			}

			if (!"".equals((s = mCommentsView.getText().toString()))) {
				body.append("Comments: " + s + "\n");
			}

			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { "beermeupdate@gmail.com" });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Beer Me! Brewery Update");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					body.toString());
			startActivity(Intent.createChooser(emailIntent,
					"Send update to Beer Me!™"));
			dialog.dismiss();
		}
	};

	private void loadFields() {
		mBrewery = new Brewery(getActivity(), mBreweryId);

		if (mBrewery != null) {
			mName = mBrewery.getName();
			mNameView.setText(mName);

			mAddress = mBrewery.getAddress();
			mAddressView.setText(mAddress);

			mStatus = this.getResources().getStringArray(R.array.status_value)[BreweryStatusFilterPreference
					.getIndex(mBrewery.getStatus())];
			mStatusView.setText(mStatus);

			mPhone = mBrewery.getPhone();
			mPhoneView.setText(mPhone);

			mHours = mBrewery.getHours();
			mHoursView.setText(mHours);

			mWeb = mBrewery.getWeb();
			mWebView.setText(mWeb);

			mOpen = mBrewery.isOpenPublic();
			mOpenBox.setChecked(mOpen);

			mOpenLayout.setVisibility(mOpenBox.isChecked() ? View.VISIBLE
					: View.GONE);

			mBar = mBrewery.hasBar();
			mBarBox.setChecked(mBar);

			mBeerGarden = mBrewery.hasBeergarden();
			mBeerGardenBox.setChecked(mBeerGarden);

			mFood = mBrewery.hasFood();
			mFoodBox.setChecked(mFood);

			mGiftShop = mBrewery.hasGiftshop();
			mGiftShopBox.setChecked(mGiftShop);

			mHotel = mBrewery.hasHotel();
			mHotelBox.setChecked(mHotel);

			mInternet = mBrewery.hasInternet();
			mInternetBox.setChecked(mInternet);

			mRetail = mBrewery.hasRetail();
			mRetailBox.setChecked(mRetail);

			mTours = mBrewery.hasTours();
			mToursBox.setChecked(mTours);
		}
	}

	class OnStatusViewClicked implements OnClickListener {
		private StatusSetter mSetter;

		public OnStatusViewClicked(StatusSetter statusSetter) {
			this.mSetter = statusSetter;
		}

		@Override
		public void onClick(View v) {
			StatusPickerFragment newFragment = StatusPickerFragment
					.newInstance();
			newFragment.registerListener(mSetter);
			newFragment.show(getChildFragmentManager(), "statusPicker");
		}
	}

	@Override
	public void setStatusFromPicker(int status) {
		mStatusView
				.setText(getResources().getStringArray(R.array.status_value)[status]);
	}
}