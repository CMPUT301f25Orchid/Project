package com.example.fairdraw.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.service.autofill.Validators;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fairdraw.R;
import java.util.Calendar;

public class FilterEventsDialogFragment extends DialogFragment {

    private FilterListener listener;
    private static final String ARG_STATUS = "current_status";
    private static final String ARG_INTEREST = "current_interest";
    private static final String ARG_AVAILABILITY = "current_availability";

    public interface FilterListener {
        void onFiltersApplied(String status, String interest, int availability);
        void onFiltersCleared();
    }

    // ✅ Use a static newInstance method to pass arguments
    public static FilterEventsDialogFragment newInstance(String currentStatus, String currentInterest, int currentAvailability) {
        FilterEventsDialogFragment fragment = new FilterEventsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, currentStatus);
        args.putString(ARG_INTEREST, currentInterest);
        args.putInt(ARG_AVAILABILITY, currentAvailability);
        fragment.setArguments(args);
        return fragment;
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.filter_events_dialog, null);

        // ✅ Retrieve current filters from arguments
        String currentStatus = "All";
        if (getArguments() != null) {
            currentStatus = getArguments().getString(ARG_STATUS, "All");
        }

        // ✅ Pre-select the correct radio button for status
        RadioGroup statusGroup = view.findViewById(R.id.statusGroup);
        if ("Open".equals(currentStatus)) {
            statusGroup.check(R.id.statusOpen);
        } else if ("Closed".equals(currentStatus)) {
            statusGroup.check(R.id.statusClosed);
        } else {
            statusGroup.check(R.id.statusAll);
        }


        Button applyBtn = view.findViewById(R.id.applyFiltersBtn);
        Button clearBtn = view.findViewById(R.id.clearFiltersBtn);

        applyBtn.setOnClickListener(v -> {
            if (listener != null) {

                RadioGroup sg = view.findViewById(R.id.statusGroup);
                String status;
                int checkedStatusId = sg.getCheckedRadioButtonId();
                if (checkedStatusId == R.id.statusOpen) {
                    status = "Open";
                } else if (checkedStatusId == R.id.statusClosed) {
                    status = "Closed";
                } else {
                    status = "All";
                }

                // These are still commented out, so they default to "All" and -1
                String interest = "All";
                int availability = -1;

                listener.onFiltersApplied(status, interest, availability);
            }
            dismiss();
        });

        clearBtn.setOnClickListener(v -> {
            if (listener != null) listener.onFiltersCleared();
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}
