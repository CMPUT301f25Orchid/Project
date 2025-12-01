package com.example.fairdraw.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fairdraw.R;

import java.util.ArrayList;
import java.util.List;

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
    private static final String ARG_AVAILABLE_TAGS = "available_tags";

    /**
     * Listener invoked when filters are applied or cleared by the user.
     */
    public interface FilterListener {
        /**
         * Called when the user applies new filters.
         * @param status selected status filter ("All", "Open", "Closed")
         * @param interest selected interest filter (string or "All")
         * @param availability selected availability as an int, or 0 for any
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
     * @param currentAvailability current availability or 0 for any
     * @return configured FilterEventsDialogFragment
     */
    public static FilterEventsDialogFragment newInstance(String currentStatus, String currentInterest, int currentAvailability) {
        return newInstance(currentStatus, currentInterest, currentAvailability, null);
    }

    /**
     * Create a new FilterEventsDialogFragment with optional current values and available tags.
     *
     * @param currentStatus current status filter, or "All"
     * @param currentInterest current interest filter, or "All"
     * @param currentAvailability current availability or 0 for any
     * @param availableTags list of available tags for autocomplete
     * @return configured FilterEventsDialogFragment
     */
    public static FilterEventsDialogFragment newInstance(String currentStatus, String currentInterest, 
                                                         int currentAvailability, ArrayList<String> availableTags) {
        FilterEventsDialogFragment fragment = new FilterEventsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, currentStatus);
        args.putString(ARG_INTEREST, currentInterest);
        args.putInt(ARG_AVAILABILITY, currentAvailability);
        if (availableTags != null) {
            args.putStringArrayList(ARG_AVAILABLE_TAGS, availableTags);
        }
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
        ArrayList<String> availableTags = null;
        
        if (getArguments() != null) {
            currentStatus = getArguments().getString(ARG_STATUS, "All");
            currentInterest = getArguments().getString(ARG_INTEREST, "All");
            currentAvailability = getArguments().getInt(ARG_AVAILABILITY, 0);
            availableTags = getArguments().getStringArrayList(ARG_AVAILABLE_TAGS);
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

        // Set up interest autocomplete
        AutoCompleteTextView interestAutocomplete = view.findViewById(R.id.filter_interest_autocomplete);
        if (availableTags != null && !availableTags.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, availableTags);
            interestAutocomplete.setAdapter(adapter);
            interestAutocomplete.setThreshold(1);
        }
        // Set current interest value if not "All"
        if (!"All".equals(currentInterest) && currentInterest != null && !currentInterest.isEmpty()) {
            interestAutocomplete.setText(currentInterest);
        }

        // Set up availability radio group
        RadioGroup availabilityGroup = view.findViewById(R.id.availabilityGroup);
        switch (currentAvailability) {
            case 1:
                availabilityGroup.check(R.id.availabilityHasSpots);
                break;
            case 2:
                availabilityGroup.check(R.id.availabilityFull);
                break;
            case 3:
                availabilityGroup.check(R.id.availabilityHasWaitlist);
                break;
            default:
                availabilityGroup.check(R.id.availabilityAll);
                break;
        }

        Button applyBtn = view.findViewById(R.id.applyFiltersBtn);
        Button clearBtn = view.findViewById(R.id.clearFiltersBtn);

        // Store final copies for lambda
        final ArrayList<String> finalAvailableTags = availableTags;

        applyBtn.setOnClickListener(v -> {
            if (listener != null) {
                // Get status
                String status;
                int checkedStatusId = statusGroup.getCheckedRadioButtonId();
                if (checkedStatusId == R.id.statusOpen) {
                    status = "Open";
                } else if (checkedStatusId == R.id.statusClosed) {
                    status = "Closed";
                } else {
                    status = "All";
                }

                // Get interest from autocomplete
                String interest = interestAutocomplete.getText().toString().trim();
                if (interest.isEmpty()) {
                    interest = "All";
                } else {
                    // Check for exact match (case-insensitive) in available tags
                    // If not found, use as substring search term
                    if (finalAvailableTags != null) {
                        String exactMatch = findExactMatch(interest, finalAvailableTags);
                        if (exactMatch != null) {
                            interest = exactMatch;
                        }
                        // Otherwise keep the typed interest for substring fallback
                    }
                }

                // Get availability
                int availability = 0;
                int checkedAvailabilityId = availabilityGroup.getCheckedRadioButtonId();
                if (checkedAvailabilityId == R.id.availabilityHasSpots) {
                    availability = 1;
                } else if (checkedAvailabilityId == R.id.availabilityFull) {
                    availability = 2;
                } else if (checkedAvailabilityId == R.id.availabilityHasWaitlist) {
                    availability = 3;
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

    /**
     * Finds an exact case-insensitive match for the query in the available tags.
     * 
     * @param query the search query
     * @param availableTags list of available tags
     * @return the matching tag or null if no exact match found
     */
    private String findExactMatch(String query, List<String> availableTags) {
        if (query == null || availableTags == null) return null;
        for (String tag : availableTags) {
            if (tag != null && tag.equalsIgnoreCase(query)) {
                return tag;
            }
        }
        return null;
    }
}
