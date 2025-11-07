package com.example.fairdraw.Others;

import androidx.annotation.NonNull;

/**
 * Represents the lifecycle state of an {@code Event}.
 * <p>
 * Each enum constant overrides {@link #toString()} to provide a human-readable label.
 * </p>
 * <p>
 * The possible states are:
 * <ul>
 * <li><code>DRAFT</code>: The event is in draft mode, not yet published.</li>
 * <li><code>PUBLISHED</code>: The event is published and visible to others.</li>
 * <li><code>CLOSED</code>: The event is closed and no longer active.</li>
 * </ul>
 * </p>
 */
public enum EventState {
    /**
     * The event is in draft mode, not yet published.
     */
    DRAFT {
        @NonNull
        @Override
        public String toString() {
            return "Draft";
        }
    },
    /**
     * The event is published and visible to others.
     */
    PUBLISHED {
        @NonNull
        @Override
        public String toString() {
            return "Published";
        }
    },
    /**
     * The event is closed and no longer active.
     */
    CLOSED {
        @NonNull
        @Override
        public String toString() {
            return "Closed";
        }
    }
}
