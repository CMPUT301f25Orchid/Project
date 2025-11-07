package com.example.fairdraw.Others;

import androidx.annotation.NonNull;

/**
 * Represents the possible states of an event in the FairDraw application.
 * An event progresses through these states during its lifecycle.
 */
public enum EventState {
    /** Event is in draft mode and not visible to entrants */
    DRAFT {
        @NonNull
        @Override
        public String toString() {
            return "Draft";
        }
    },
    /** Event has been published and is visible to entrants for registration */
    PUBLISHED {
        @NonNull
        @Override
        public String toString() {
            return "Published";
        }
    },
    /** Event registration is closed and lottery has been conducted */
    CLOSED {
        @NonNull
        @Override
        public String toString() {
            return "Closed";
        }
    }
}
