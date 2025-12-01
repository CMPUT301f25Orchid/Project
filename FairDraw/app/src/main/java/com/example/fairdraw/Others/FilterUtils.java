package com.example.fairdraw.Others;

import com.example.fairdraw.Models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for filtering events. Provides testable static methods
 * for filtering by status, interest (tags), and availability.
 */
public class FilterUtils {

    /**
     * Availability filter constants.
     */
    public static final int AVAILABILITY_ALL = 0;
    public static final int AVAILABILITY_HAS_FREE_SPOTS = 1;
    public static final int AVAILABILITY_FULL = 2;
    public static final int AVAILABILITY_HAS_WAITING_LIST = 3;

    /**
     * Filters a list of events based on the given criteria.
     *
     * @param events       the list of events to filter
     * @param status       the status filter ("All", "Open", "Closed", "Draft")
     * @param interest     the interest/tag filter (case-insensitive exact match, or "All" to skip)
     * @param availability the availability filter (0=All, 1=HasFreeSpots, 2=Full, 3=HasWaitingList)
     * @return a new list containing only the events that match all filters
     */
    public static List<Event> applyFilters(List<Event> events, String status, String interest, int availability) {
        if (events == null) {
            return new ArrayList<>();
        }

        List<Event> filtered = new ArrayList<>();
        for (Event event : events) {
            if (event == null) continue;

            if (matchesStatus(event, status)
                    && matchesInterest(event, interest)
                    && matchesAvailability(event, availability)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    /**
     * Checks if an event matches the given status filter.
     *
     * @param event  the event to check
     * @param status the status filter ("All", "Open", "Closed", "Draft")
     * @return true if the event matches the status filter
     */
    public static boolean matchesStatus(Event event, String status) {
        if (status == null || "All".equalsIgnoreCase(status)) {
            return true;
        }

        EventState eventState = event.getState();
        if (eventState == null) {
            return false;
        }

        switch (status) {
            case "Open":
                return eventState == EventState.PUBLISHED;
            case "Closed":
                return eventState == EventState.CLOSED;
            case "Draft":
                return eventState == EventState.DRAFT;
            default:
                return true;
        }
    }

    /**
     * Checks if an event matches the given interest/tag filter.
     * Performs case-insensitive exact match against the event's tags.
     *
     * @param event    the event to check
     * @param interest the interest/tag to match, or "All" to skip filtering
     * @return true if the event has the given tag (case-insensitive) or interest is "All"
     */
    public static boolean matchesInterest(Event event, String interest) {
        if (interest == null || "All".equalsIgnoreCase(interest)) {
            return true;
        }

        List<String> tags = event.getTags();
        if (tags == null || tags.isEmpty()) {
            return false;
        }

        for (String tag : tags) {
            if (tag != null && tag.equalsIgnoreCase(interest)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an event matches the given availability filter.
     *
     * @param event        the event to check
     * @param availability the availability filter:
     *                     0 or negative = All (no filter)
     *                     1 = Has free spots (enrolled < capacity)
     *                     2 = Full (enrolled >= capacity)
     *                     3 = Has waiting list (waitingList.size() > 0)
     * @return true if the event matches the availability filter
     */
    public static boolean matchesAvailability(Event event, int availability) {
        if (availability <= AVAILABILITY_ALL) {
            return true;
        }

        Integer capacity = event.getCapacity();
        List<String> enrolledList = event.getEnrolledList();
        List<String> waitingList = event.getWaitingList();

        int enrolled = (enrolledList != null) ? enrolledList.size() : 0;
        int cap = (capacity != null) ? capacity : 0;
        int waiting = (waitingList != null) ? waitingList.size() : 0;

        switch (availability) {
            case AVAILABILITY_HAS_FREE_SPOTS:
                // Has free spots: enrolled < capacity
                return enrolled < cap;
            case AVAILABILITY_FULL:
                // Full: enrolled >= capacity
                return enrolled >= cap;
            case AVAILABILITY_HAS_WAITING_LIST:
                // Has waiting list: waitingList.size() > 0
                return waiting > 0;
            default:
                return true;
        }
    }
}
