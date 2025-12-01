package com.example.fairdraw.Others;

public class EntrantEventStatus {
    public static final String REGISTERED   = "REGISTERED";    // Registered, waiting for draw
    public static final String INVITED      = "INVITED";       // If you have an invitedList
    public static final String SELECTED     = "SELECTED";      // Won / final entrant
    public static final String NOT_SELECTED = "NOT_SELECTED";  // Lost the draw
    public static final String CANCELLED    = "CANCELLED";     // Removed / withdrew
    public static final String CHECKED_IN   = "CHECKED_IN";    // Scanned in at event
}
