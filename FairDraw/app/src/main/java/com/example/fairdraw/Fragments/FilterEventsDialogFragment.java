package com.example.fairdraw.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fairdraw.R;

/**
 * DialogFragment that displays filter controls for the events list.
 *
 * <p>Hosts can supply the current filter values via {@link #newInstance} and
 * receive updates via {@link FilterListener}.</p>
 */
public class FilterEventsDialogFragment extends DialogFragment {

    private FilterListener listener;
    private static final String ARG_STATUS = "current_status";
    private static final String ARG_INTEREST = "current_interest";
    private static final String ARG_AVAILABILITY = "current_availability";

    /**
     * Listener invoked when filters are applied or cleared by the user.
     */
    public interface FilterListener {
        /**
         * Called when the user applies new filters.
         * @param status selected status filter ("All", "Open", "Closed")
         * @param interest selected interest filter (string or "All")
         * @param availability selected availability as an int, or -1 for any
         */
        void onFiltersApplied(String status, String interest, int availability);

        /**
         * Called when the user clears all filters.
         */
        void onFiltersCleared();
    }

    /**
     * Create a new FilterEventsDialogFragment with optional current values.
     *
     * @param currentStatus current status filter, or "All"
     * @param currentInterest current interest filter, or "All"
     * @param currentAvailability current availability or -1 for any
     * @return configured FilterEventsDialogFragment
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
     * Set a listener which will receive filter events.
     *
     * @param listener the FilterListener to notify
     */
    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    /**
     * Create the dialog UI used to pick filters. Reads the arguments (if any)
     * to pre-select the controls and notifies the {@link FilterListener}
     * when the user taps Apply or Clear.
     *
     * @param savedInstanceState saved bundle provided by the system (may be null)
     * @return a configured {@link Dialog} displaying filter controls
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.filter_events_dialog, null);

        // Retrieve current filters from arguments
        String currentStatus = "All";
        String currentInterest = "All";
        int currentAvailability = 0;
        if (getArguments() != null) {
            currentStatus = getArguments().getString(ARG_STATUS, "All");
            currentInterest = getArguments().getString(ARG_INTEREST, "All");
            currentAvailability = getArguments().getInt(ARG_AVAILABILITY, 0);
        }

        // Pre-select the correct radio button for status
        RadioGroup statusGroup = view.findViewById(R.id.statusGroup);
        if ("Open".equals(currentStatus)) {
            statusGroup.check(R.id.statusOpen);
        } else if ("Closed".equals(currentStatus)) {
            statusGroup.check(R.id.statusClosed);
        } else {
            statusGroup.check(R.id.statusAll);
        }

        // Pre-select the correct radio button for interest
        RadioGroup interestsGroup = view.findViewById(R.id.interestsGroup);
        if ("Sports and Recreation".equalsIgnoreCase(currentInterest)) {
            interestsGroup.check(R.id.interestSports);
        } else if ("Arts/Culture".equalsIgnoreCase(currentInterest)) {
            interestsGroup.check(R.id.interestCulture);
        } else if ("Education".equalsIgnoreCase(currentInterest)) {
            interestsGroup.check(R.id.interestEducation);
        } else {
            interestsGroup.check(R.id.interestAll);
        }

        // Pre-select the correct radio button for availability
        RadioGroup availabilityGroup = view.findViewById(R.id.availabilityGroup);
        switch (currentAvailability) {
            case 1:
                availabilityGroup.check(R.id.availabilityHasFreeSpots);
                break;
            case 2:
                availabilityGroup.check(R.id.availabilityFull);
                break;
            case 3:
                availabilityGroup.check(R.id.availabilityHasWaitingList);
                break;
            default:
                availabilityGroup.check(R.id.availabilityAll);
                break;
        }

        Button applyBtn = view.findViewById(R.id.applyFiltersBtn);
        Button clearBtn = view.findViewById(R.id.clearFiltersBtn);

        applyBtn.setOnClickListener(v -> {
            if (listener != null) {

                String status;
                int checkedStatusId = statusGroup.getCheckedRadioButtonId();
                if (checkedStatusId == R.id.statusOpen) {
                    status = "Open";
                } else if (checkedStatusId == R.id.statusClosed) {
                    status = "Closed";
                } else {
                    status = "All";
                }

                // Read interest selection
                String interest;
                int checkedInterestId = interestsGroup.getCheckedRadioButtonId();
                if (checkedInterestId == R.id.interestSports) {
                    interest = "Sports and Recreation";
                } else if (checkedInterestId == R.id.interestCulture) {
                    interest = "Arts/Culture";
                } else if (checkedInterestId == R.id.interestEducation) {
                    interest = "Education";
                } else {
                    interest = "All";
                }

                // Read availability selection
                int availability;
                int checkedAvailabilityId = availabilityGroup.getCheckedRadioButtonId();
                if (checkedAvailabilityId == R.id.availabilityHasFreeSpots) {
                    availability = 1;
                } else if (checkedAvailabilityId == R.id.availabilityFull) {
                    availability = 2;
                } else if (checkedAvailabilityId == R.id.availabilityHasWaitingList) {
                    availability = 3;
                } else {
                    availability = 0;
                }

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
