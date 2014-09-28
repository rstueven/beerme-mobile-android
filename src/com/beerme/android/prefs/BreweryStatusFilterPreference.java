package com.beerme.android.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.beerme.android.R;

public class BreweryStatusFilterPreference extends DialogPreference {
	public static final int OPEN = 0x1;
	public static final int PLANNED = 0x2;
	public static final int NOTBREWING = 0x4;
	public static final int CLOSED = 0x8;
	public static final int DEFAULT_VALUE = OPEN | PLANNED;
	private int mIntValue = DEFAULT_VALUE;
	private CheckBox ckOpen;
	private CheckBox ckPlanned;
	private CheckBox ckNotBrewing;
	private CheckBox ckClosed;

	public BreweryStatusFilterPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDialogLayoutResource(R.layout.brewerystatusfilter_dialog);
		setDialogIcon(null);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindView(view);

		ckOpen = (CheckBox) view.findViewById(R.id.open);
		ckOpen.setOnCheckedChangeListener(ckListener);
		ckPlanned = (CheckBox) view.findViewById(R.id.planned);
		ckPlanned.setOnCheckedChangeListener(ckListener);
		ckNotBrewing = (CheckBox) view.findViewById(R.id.nolongerbrewing);
		ckNotBrewing.setOnCheckedChangeListener(ckListener);
		ckClosed = (CheckBox) view.findViewById(R.id.closed);
		ckClosed.setOnCheckedChangeListener(ckListener);

		ckOpen.setChecked((mIntValue & OPEN) != 0);
		ckPlanned.setChecked((mIntValue & PLANNED) != 0);
		ckNotBrewing.setChecked((mIntValue & NOTBREWING) != 0);
		ckClosed.setChecked((mIntValue & CLOSED) != 0);
	}

	private OnCheckedChangeListener ckListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int mask = 0;
			if (buttonView == ckOpen) {
				mask = OPEN;
			} else if (buttonView == ckPlanned) {
				mask = PLANNED;
			} else if (buttonView == ckNotBrewing) {
				mask = NOTBREWING;
			} else if (buttonView == ckClosed) {
				mask = CLOSED;
			}

			if (mask != 0) {
				mIntValue = isChecked ? mIntValue | mask : mIntValue & ~mask;
				persistInt(mIntValue);
			}
		}
	};

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		if (restorePersistedValue) {
			// Restore existing state
			mIntValue = this.getPersistedInt(DEFAULT_VALUE);
		} else {
			// Set default state from the XML attribute
			mIntValue = (Integer) defaultValue;
			persistInt(mIntValue);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, DEFAULT_VALUE);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		// Check whether this Preference is persistent (continually saved)
		if (isPersistent()) {
			// No need to save instance state since it's persistent, use
			// superclass state
			return superState;
		}

		// Create instance of custom BaseSavedState
		final SavedState myState = new SavedState(superState);
		// Set the state's value with the class member that holds current
		// setting value
		myState.value = mIntValue;
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// Check whether we saved the state in onSaveInstanceState
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// Cast state to custom BaseSavedState and pass to superclass
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());

		// Set this Preference's widget to reflect the restored state
		this.onSetInitialValue(false, myState.value);
	}

	private static class SavedState extends BaseSavedState {
		// Member that holds the setting's value
		// Change this data type to match the type saved by your Preference
		int value;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public SavedState(Parcel source) {
			super(source);
			// Get the current preference's value
			value = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			// Write the preference's value
			dest.writeInt(value);
		}

		// Standard creator object using an instance of this class
		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	// Returns the position of the first set bit in status.
	public static int getIndex(int status) {
		int test = status;
		int index = 0;

		for (index = 0; index < 5; index++) {
			if ((test & (1L << index)) != 0)
				break;
		}

		return (index >= 5) ? -1 : index;
	}

	private static boolean is(int status, int mask) {
		return (status & mask) != 0;
	}

	public static boolean isOpen(int status) {
		return is(status, OPEN);
	}

	public static boolean isPlanned(int status) {
		return is(status, PLANNED);
	}

	public static boolean isNoLongerBrewing(int status) {
		return is(status, NOTBREWING);
	}

	public static boolean isClosed(int status) {
		return is(status, CLOSED);
	}

	public static boolean match(Context context, int status) {
		SharedPreferences prefs = Prefs.getSettings(context);
		int mask = prefs.getInt(Prefs.KEY_STATUS_FILTER, DEFAULT_VALUE);
		return is(status, mask);
	}

	public static String toString(Context context, int status) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;

		if (isOpen(status)) {
			sb.append(context.getString(R.string.Open));
			first = false;
		}

		if (isPlanned(status)) {
			if (!first) {
				sb.append(',');
			}
			sb.append(context.getString(R.string.Planned));
			first = false;
		}

		if (isNoLongerBrewing(status)) {
			if (!first) {
				sb.append(',');
			}
			sb.append(context.getString(R.string.No_longer_brewing));
			first = false;
		}

		if (isClosed(status)) {
			if (!first) {
				sb.append(',');
			}
			sb.append(context.getString(R.string.Closed));
			first = false;
		}

		return sb.toString();
	}
}