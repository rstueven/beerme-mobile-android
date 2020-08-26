package com.beerme.android.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.beerme.android.R;
import com.beerme.android.ui.DialogFrag;

public class Help {
	private Help() {
	}

	private static final Map<String, Integer> helpMap;
	static {
		Map<String, Integer> aMap = new HashMap<String, Integer>();
		aMap.put("com.beerme.android.ui.MainActivity", R.layout.help_main);
		aMap.put("com.beerme.android.ui.BreweryActivity", R.layout.help_brewery);
		aMap.put("com.beerme.android.ui.BeerActivity", R.layout.help_beer);
		aMap.put("com.beerme.android.ui.EditableBreweryNote",
				R.layout.help_brewerynote);
		helpMap = Collections.unmodifiableMap(aMap);
	}

	public static void show(Context context) {
		int help_id = -1;

		if (context == null) {
			throw new IllegalArgumentException();
		} else {
			help_id = helpMap.get(context.getClass().getName()).intValue();
			Bundle args = new Bundle();
			args.putInt("layout", help_id);
			DialogFragment frag = DialogFrag.newInstance(DialogFrag.Mode.HELP,
					args);
			frag.show(((FragmentActivity) context).getSupportFragmentManager(),
					"help");
		}
	}
}