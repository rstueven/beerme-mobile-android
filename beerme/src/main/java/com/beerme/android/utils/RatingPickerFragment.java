package com.beerme.android.utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.beerme.android.R;

import java.util.Locale;

public class RatingPickerFragment extends DialogFragment {
    private static final String TAG_VIEW = "view";
    private static final String TAG_RATING = "rating";
    private static final String TAG_MAX = "max";
    private int mResultView;
    private float mRating;
    private int mMax;
    private RatingListener mListener;
    private SeekBar mSeekBar;

    public interface RatingListener {
        void onRatingSet(int viewId, float rating);
    }

    public static RatingPickerFragment newInstance(int viewId, float rating, int max) {
        RatingPickerFragment rpf = new RatingPickerFragment();
        Bundle args = new Bundle();
        args.putInt(TAG_VIEW, viewId);
        args.putFloat(TAG_RATING, rating);
        args.putInt(TAG_MAX, max);
        rpf.setArguments(args);
        return rpf;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.BeerMeTheme);

        mListener = (RatingListener) activity;

        Bundle args = getArguments();
        if (args != null) {
            mResultView = args.getInt(TAG_VIEW);
            mRating = args.getFloat(TAG_RATING);
            mMax = args.getInt(TAG_MAX);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Utils.trackFragment(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ratingpickerfragment, container, false);
        final TextView minView = v.findViewById(R.id.ratingpicker_min);
        minView.setText(Utils.toFrac(mRating));
        TextView maxView = v.findViewById(R.id.ratingpicker_max);
        maxView.setText(String.format(Locale.getDefault(), "/%d", mMax));

        mSeekBar = v.findViewById(R.id.ratingpicker_rating);
        mSeekBar.setProgress(scale(mRating));
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRating = Utils.roundToHalf(unScale(progress));
                minView.setText(Utils.toFrac(mRating));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button cancel = v.findViewById(R.id.ratingpicker_cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        Button save = v.findViewById(R.id.ratingpicker_save);
        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRatingSet(mResultView, Utils.roundToHalf(unScale(mSeekBar.getProgress())));
                getDialog().dismiss();
            }
        });

        return v;
    }

    private int scale(float x) {
        return (int) ((x / mMax) * 100);
    }

    private float unScale(int x) {
        return (float) (x * mMax / 100.0);
    }
}