package com.example.fairdraw.Others;

import com.example.fairdraw.Models.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class containing static methods for filtering events.
 * Used by EntrantHomeActivity and unit tests.
 */
public class FilterUtils {

    /**
     * Filters events by status, interest, and availability.
     *
     * @param events list of events to filter
     * @param status status filter ("All", "Open", "Closed", "Draft")
     * @param interest interest/tag filter (case-insensitive; "All" or null/empty for no filter)
     * @param availability availability filter (0=All, 1=Has free spots, 2=Full, 3=Has waiting list)
     * @return filtered list of events
     */
    public static List<Event> filter(List<Event> events, String status, String interest, int availability) {
        if (events == null) return new ArrayList<>();
        
        List<Event> filtered = new ArrayList<>(events);
        
        // Filter by status
        if (!matchesStatusAll(status)) {
            filtered.removeIf(event -> !matchesStatus(event, status));
        }
        
        // Filter by interest
        if (!matchesInterestAll(interest)) {
            filtered.removeIf(event -> !matchesInterest(event, interest));
        }
        
        // Filter by availability
        if (availability != 0) {
            filtered.removeIf(event -> !matchesAvailability(event, availability));
        }
        
        return filtered;
    }

    /**
     * Checks if status indicates "All" (no filtering).
     */
    private static boolean matchesStatusAll(String status) {
        return status == null || status.isEmpty() || "All".equalsIgnoreCase(status);
    }

    /**
     * Checks if interest indicates "All" (no filtering).
     */
    private static boolean matchesInterestAll(String interest) {
        return interest == null || interest.isEmpty() || "All".equalsIgnoreCase(interest);
    }

    /**
     * Checks if an event matches the given status filter.
     *
     * @param event the event to check
     * @param status the status to match ("Open" -> PUBLISHED, "Closed" -> CLOSED, "Draft" -> DRAFT)
     * @return true if the event matches the status
     */
    public static boolean matchesStatus(Event event, String status) {
        if (event == null || event.getState() == null) return false;
        if (matchesStatusAll(status)) return true;
        
        switch (status) {
            case "Open":
                return event.getState() == EventState.PUBLISHED;
            case "Closed":
                return event.getState() == EventState.CLOSED;
            case "Draft":
                return event.getState() == EventState.DRAFT;
            default:
                return true;
        }
    }

    /**
     * Checks if an event matches the given interest filter.
     * Matches if any tag equals the interest (case-insensitive) OR any tag contains 
     * the interest as a substring (case-insensitive).
     *
     * @param event the event to check
     * @param interest the interest/tag to match
     * @return true if the event matches the interest
     */
    public static boolean matchesInterest(Event event, String interest) {
        if (event == null) return false;
        if (matchesInterestAll(interest)) return true;
        
        List<String> tags = event.getTags();
        if (tags == null || tags.isEmpty()) return false;
        
        String lowerInterest = interest.toLowerCase();
        
        for (String tag : tags) {
            if (tag == null) continue;
            String lowerTag = tag.toLowerCase();
            // Exact match or substring match (case-insensitive)
            if (lowerTag.equals(lowerInterest) || lowerTag.contains(lowerInterest)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an event matches the given availability filter.
     * 
     * @param event the event to check
     * @param availability the availability filter:
     *                     0 = All (no filter)
     *                     1 = Has free spots (enrolled < capacity)
     *                     2 = Full (enrolled >= capacity)
     *                     3 = Has waiting list (waitingList.size() > 0)
     * @return true if the event matches the availability criteria
     */
    public static boolean matchesAvailability(Event event, int availability) {
        if (event == null) return false;
        if (availability == 0) return true; // All
        
        int enrolled = event.getEnrolledList() != null ? event.getEnrolledList().size() : 0;
        Integer capacity = event.getCapacity();
        int capacityValue = capacity != null ? capacity : 0;
        int waitingListSize = event.getWaitingList() != null ? event.getWaitingList().size() : 0;
        
        switch (availability) {
            case 1: // Has free spots
                return enrolled < capacityValue;
            case 2: // Full
                return enrolled >= capacityValue;
            case 3: // Has waiting list
                return waitingListSize > 0;
            default:
                return true;
        }
    }

    /**
     * Extracts distinct tags from a list of events, sorted alphabetically.
     *
     * @param events list of events
     * @return sorted list of distinct tags
     */
    public static List<String> extractAvailableTags(List<Event> events) {
        if (events == null) return new ArrayList<>();
        
        // Use Set for O(1) duplicate checking
        Set<String> tagSet = new HashSet<>();
        
        for (Event event : events) {
            if (event == null) continue;
            List<String> tags = event.getTags();
            if (tags == null) continue;
            for (String tag : tags) {
                if (tag != null && !tag.isEmpty()) {
                    tagSet.add(tag);
                }
            }
        }
        
        // Convert to list and sort alphabetically
        List<String> availableTags = new ArrayList<>(tagSet);
        availableTags.sort(String::compareToIgnoreCase);
        return availableTags;
    }
}
