package com.example.fairdraw;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditEventPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditEventPage extends Fragment {

    public EditEventPage() {
        // Required empty public constructor
        super(R.layout.fragment_edit_event_page);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_event_page, container, false);
    }
}