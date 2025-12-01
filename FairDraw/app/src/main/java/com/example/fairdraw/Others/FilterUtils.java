package com.example.fairdraw.Others;

import com.example.fairdraw.Models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for filtering events based on status, interest (tags), and availability.
 */
public class FilterUtils {

    /**
     * Filters a list of events based on status, interest, and availability criteria.
     *
     * @param events       the list of events to filter
     * @param status       status filter: "All", "Open", "Closed", or "Draft"
     * @param interest     interest/tag filter: "All" for no filter, otherwise the tag to match (exact or substring)
     * @param availability availability code: -1 for all, 0 for has free spots, 1 for full, 2 for has waiting list
     * @return filtered list of events (new list, does not mutate the original)
     */
    public static List<Event> filter(List<Event> events, String status, String interest, int availability) {
        if (events == null) {
            return new ArrayList<>();
        }
        
        List<Event> filtered = new ArrayList<>();
        String queryLower = interest != null ? interest.toLowerCase() : "";
        
        for (Event event : events) {
            if (event == null) continue;
            
            if (!matchesStatus(event, status)) continue;
            if (!matchesInterest(event, queryLower)) continue;
            if (!matchesAvailability(event, availability)) continue;
            
            filtered.add(event);
        }
        
        return filtered;
    }

    /**
     * Checks if an event matches the status filter.
     *
     * @param event  the event to check
     * @param status the status filter: "All", "Open", "Closed", or "Draft"
     * @return true if the event matches the status filter
     */
    public static boolean matchesStatus(Event event, String status) {
        if (event == null) {
            return false;
        }
        
        if (status == null || "All".equalsIgnoreCase(status)) {
            return true;
        }
        
        EventState eventState = event.getState();
        if (eventState == null) {
            return false;
        }
        
        if ("Open".equalsIgnoreCase(status)) {
            return eventState == EventState.PUBLISHED;
        } else if ("Closed".equalsIgnoreCase(status)) {
            return eventState == EventState.CLOSED;
        } else if ("Draft".equalsIgnoreCase(status)) {
            return eventState == EventState.DRAFT;
        }
        
        return true;
    }

    /**
     * Checks if an event matches the interest filter by exact tag match (case-insensitive) 
     * or substring match in any tag.
     *
     * @param event      the event to check
     * @param queryLower the interest query in lowercase ("all" or empty = no filter)
     * @return true if the event matches the interest filter
     */
    public static boolean matchesInterest(Event event, String queryLower) {
        if (event == null) {
            return false;
        }
        
        if (queryLower == null || queryLower.isEmpty() || "all".equals(queryLower)) {
            return true;
        }
        
        List<String> tags = event.getTags();
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        
        for (String tag : tags) {
            if (tag == null) continue;
            
            String tagLower = tag.toLowerCase();
            // Substring match includes exact match (case-insensitive)
            if (tagLower.contains(queryLower)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Checks if an event matches the availability filter.
     *
     * @param event the event to check
     * @param code  availability code: -1 for all, 0 for has free spots, 1 for full, 2 for has waiting list
     * @return true if the event matches the availability filter
     */
    public static boolean matchesAvailability(Event event, int code) {
        if (event == null) {
            return false;
        }
        
        if (code == -1) {
            return true; // All - no filter
        }
        
        Integer capacity = event.getCapacity();
        if (capacity == null) {
            capacity = 0;
        }
        
        int enrolledCount = event.getEnrolledList() != null ? event.getEnrolledList().size() : 0;
        int invitedCount = event.getInvitedList() != null ? event.getInvitedList().size() : 0;
        int waitingCount = event.getWaitingList() != null ? event.getWaitingList().size() : 0;
        
        // Total confirmed + pending = enrolled + invited
        int filledSpots = enrolledCount + invitedCount;
        boolean isFull = filledSpots >= capacity;
        boolean hasWaitingList = waitingCount > 0;
        
        switch (code) {
            case 0: // Has free spots (not full)
                return !isFull;
            case 1: // Full
                return isFull;
            case 2: // Has waiting list
                return hasWaitingList;
            default:
                return true;
        }
    }
}
