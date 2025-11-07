package com.example.fairdraw.Others;

import androidx.annotation.NonNull;

public enum EventState {
    DRAFT {
        @NonNull
        @Override
        public String toString() {
            return "Draft";
        }
    },
    PUBLISHED {
        @NonNull
        @Override
        public String toString() {
            return "Published";
        }
    },
    CLOSED {
        @NonNull
        @Override
        public String toString() {
            return "Closed";
        }
    }
}
