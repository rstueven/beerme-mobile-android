package com.beerme.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beerme.android.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

// TODO: BreweryNotesFragment

public class BreweryNotesFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_brewery_notes, container, false);
    }
}