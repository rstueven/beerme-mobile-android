package com.beerme.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.Toast;

import com.beerme.android.R;
import com.beerme.android.utils.Utils;

public class DialogFrag extends DialogFragment {
	private static final String TAG_MODE = "mode";

	public enum Mode {
		NONE, OFFLINE, UNLICENSED, SHOW_TELNO, NETWORK_ERROR, HELP, LICENSE_OFFLINE
	}

    private Activity mActivity = null;

	public static DialogFrag newInstance(Mode mode) {
		return newInstance(mode, null);
	}

	public static DialogFrag newInstance(Mode mode, Bundle args) {
		DialogFrag frag = new DialogFrag();

		Bundle newArgs = new Bundle();
		newArgs.putInt(TAG_MODE, mode.ordinal());

		if (args != null) {
			newArgs.putAll(args);
		}

		frag.setArguments(newArgs);

		return frag;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@NonNull
    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
        Mode mode = Mode.values()[args.getInt(TAG_MODE)];
		mActivity = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setCancelable(false);

		switch (mode) {
		case OFFLINE:
			// MEDIUM: Positive button to send the user to Network settings
			builder.setTitle(R.string.No_network_connection)
					.setMessage(R.string.Requires_network_access)
					.setNegativeButton(R.string.Exit,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mActivity.finish();
								}
							});
			break;
		case UNLICENSED:
			builder.setTitle(R.string.App_not_licensed)
					.setMessage(R.string.Please_purchase)
					.setPositiveButton(R.string.Buy_app,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									try {
										Intent marketIntent = new Intent(
												Intent.ACTION_VIEW);
										marketIntent.setData(Uri
												.parse("market://details?id=com.beerme.android"));
										startActivity(marketIntent);
									} catch (ActivityNotFoundException e) {
										Intent marketIntent = new Intent(
												Intent.ACTION_VIEW,
												Uri.parse("https://play.google.com/store/apps/details?id="
														+ mActivity
																.getPackageName()));
										startActivity(marketIntent);
									} finally {
										mActivity.finish();
									}
								}
							})
					.setNegativeButton(R.string.Exit,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mActivity.finish();
								}
							});
			break;
		case SHOW_TELNO:
			String telno = args.getString("telno");
			builder.setTitle(R.string.Telephone_number).setMessage(telno);
			break;
		case NETWORK_ERROR:
			Toast.makeText(getActivity(), "DialogFrag(" + mode.name() + ")",
					Toast.LENGTH_LONG).show();
			break;
		case HELP:
			int layout = getArguments().getInt("layout", -1);
			if (layout != -1) {
				View view = mActivity.getLayoutInflater().inflate(layout, null);
				builder.setView(view);
				builder.setNegativeButton(R.string.Close,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
			}
			break;
		case LICENSE_OFFLINE:
			// MEDIUM: Positive button to send the user to Network settings
			builder.setTitle(R.string.No_network_connection)
					.setMessage(R.string.Requires_network_access_to_check_license)
					.setNegativeButton(R.string.Exit,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int which) {
									mActivity.finish();
								}
							});
				break;
		default:
			Toast.makeText(getActivity(), "DialogFrag(" + mode.name() + ")",
					Toast.LENGTH_LONG).show();
			break;
		}

		return builder.create();
	}
}