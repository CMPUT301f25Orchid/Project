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

/**
 * DialogFragment for filtering events based on status, interest, and availability.
 * This fragment provides UI controls for applying and clearing event filters.
 */
public class FilterEventsDialogFragment extends DialogFragment {

    /** Listener interface for filter actions */
    private FilterListener listener;
    /** Bundle argument key for current status filter */
    private static final String ARG_STATUS = "current_status";
    /** Bundle argument key for current interest filter */
    private static final String ARG_INTEREST = "current_interest";
    /** Bundle argument key for current availability filter */
    private static final String ARG_AVAILABILITY = "current_availability";

    /**
     * Interface for handling filter events.
     */
    public interface FilterListener {
        /**
         * Called when filters are applied.
         * 
         * @param status The selected status filter
         * @param interest The selected interest filter
         * @param availability The selected availability filter
         */
        void onFiltersApplied(String status, String interest, int availability);
        
        /**
         * Called when filters are cleared.
         */
        void onFiltersCleared();
    }

    /**
     * Creates a new instance of FilterEventsDialogFragment with current filter values.
     * 
     * @param currentStatus The current status filter value
     * @param currentInterest The current interest filter value
     * @param currentAvailability The current availability filter value
     * @return A new instance with arguments set
     */
    public static FilterEventsDialogFragment newInstance(String currentStatus, String currentInterest, int currentAvailability) {
        FilterEventsDialogFragment fragment = new FilterEventsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, currentStatus);
        args.putString(ARG_INTEREST, currentInterest);
        args.putInt(ARG_AVAILABILITY, currentAvailability);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Sets the filter listener for handling filter events.
     * 
     * @param listener The FilterListener to receive filter events
     */
    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    /**
     * Creates the filter dialog with UI controls for selecting filters.
     * 
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The Dialog to be displayed
     */
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
