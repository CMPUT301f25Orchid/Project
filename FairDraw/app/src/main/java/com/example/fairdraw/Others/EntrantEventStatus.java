package com.example.fairdraw.Others;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.fairdraw.R;

public class EntrantEventStatus {

    // Core statuses for the entrant relative to an event
    public static final String REGISTERED   = "REGISTERED";    // Registered, draw pending
    public static final String INVITED      = "INVITED";       // Organizer invited
    public static final String SELECTED     = "SELECTED";      // Entrant chosen
    public static final String NOT_SELECTED = "NOT_SELECTED";  // Draw finalized, not chosen
    public static final String CANCELLED    = "CANCELLED";     // Removed / withdrew
    public static final String CHECKED_IN   = "CHECKED_IN";    // Checked in at event

    /**
     * Map an entrant-event status to a consistent color.
     */
    public static int colorForStatus(Context context, String status) {

        if (status == null) {
            return ContextCompat.getColor(context, R.color.neutral);
        }

        switch (status) {

            case SELECTED:
                return ContextCompat.getColor(context, R.color.selected);

            case CHECKED_IN:
                return ContextCompat.getColor(context, R.color.checked_in);

            case INVITED:
                return ContextCompat.getColor(context, R.color.invited);

            case REGISTERED:
                return ContextCompat.getColor(context, R.color.registered);

            case NOT_SELECTED:
                return ContextCompat.getColor(context, R.color.not_selected);

            case CANCELLED:
                return ContextCompat.getColor(context, R.color.cancelled);

            default:
                return ContextCompat.getColor(context, R.color.neutral);
        }
    }

}
