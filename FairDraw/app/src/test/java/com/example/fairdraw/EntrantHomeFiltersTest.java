package com.example.fairdraw;

import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.Others.FilterUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the EntrantHome filtering logic via FilterUtils.
 * Tests status filtering, tag/interest filtering, and availability filtering.
 */
public class EntrantHomeFiltersTest {

    /**
     * Creates a synthetic Event for testing purposes.
     */
    private Event createTestEvent(String title, EventState state, List<String> tags, 
                                   int capacity, int enrolled, int invited, int waiting) {
        Event event = new Event();
        event.setTitle(title);
        event.setState(state);
        event.setTags(tags != null ? new ArrayList<>(tags) : new ArrayList<>());
        event.setCapacity(capacity);
        
        List<String> enrolledList = new ArrayList<>();
        for (int i = 0; i < enrolled; i++) {
            enrolledList.add("enrolled_" + i);
        }
        event.setEnrolledList(enrolledList);
        
        List<String> invitedList = new ArrayList<>();
        for (int i = 0; i < invited; i++) {
            invitedList.add("invited_" + i);
        }
        event.setInvitedList(invitedList);
        
        List<String> waitingList = new ArrayList<>();
        for (int i = 0; i < waiting; i++) {
            waitingList.add("waiting_" + i);
        }
        event.setWaitingList(waitingList);
        
        return event;
    }

    // ==================== Status Filtering Tests ====================

    @Test
    public void testFilterByStatus_All() {
        List<Event> events = Arrays.asList(
            createTestEvent("Open Event", EventState.PUBLISHED, null, 10, 0, 0, 0),
            createTestEvent("Closed Event", EventState.CLOSED, null, 10, 0, 0, 0),
            createTestEvent("Draft Event", EventState.DRAFT, null, 10, 0, 0, 0)
        );

        List<Event> result = FilterUtils.filter(events, "All", "All", -1);
        assertEquals(3, result.size());
    }

    @Test
    public void testFilterByStatus_Open() {
        List<Event> events = Arrays.asList(
            createTestEvent("Open Event", EventState.PUBLISHED, null, 10, 0, 0, 0),
            createTestEvent("Closed Event", EventState.CLOSED, null, 10, 0, 0, 0),
            createTestEvent("Draft Event", EventState.DRAFT, null, 10, 0, 0, 0)
        );

        List<Event> result = FilterUtils.filter(events, "Open", "All", -1);
        assertEquals(1, result.size());
        assertEquals("Open Event", result.get(0).getTitle());
    }

    @Test
    public void testFilterByStatus_Closed() {
        List<Event> events = Arrays.asList(
            createTestEvent("Open Event", EventState.PUBLISHED, null, 10, 0, 0, 0),
            createTestEvent("Closed Event", EventState.CLOSED, null, 10, 0, 0, 0),
            createTestEvent("Draft Event", EventState.DRAFT, null, 10, 0, 0, 0)
        );

        List<Event> result = FilterUtils.filter(events, "Closed", "All", -1);
        assertEquals(1, result.size());
        assertEquals("Closed Event", result.get(0).getTitle());
    }

    @Test
    public void testFilterByStatus_Draft() {
        List<Event> events = Arrays.asList(
            createTestEvent("Open Event", EventState.PUBLISHED, null, 10, 0, 0, 0),
            createTestEvent("Closed Event", EventState.CLOSED, null, 10, 0, 0, 0),
            createTestEvent("Draft Event", EventState.DRAFT, null, 10, 0, 0, 0)
        );

        List<Event> result = FilterUtils.filter(events, "Draft", "All", -1);
        assertEquals(1, result.size());
        assertEquals("Draft Event", result.get(0).getTitle());
    }

    // ==================== Tag/Interest Filtering Tests ====================

    @Test
    public void testFilterByTag_ExactMatch_CaseInsensitive() {
        List<Event> events = Arrays.asList(
            createTestEvent("Sports Event", EventState.PUBLISHED, Arrays.asList("Sports", "Outdoor"), 10, 0, 0, 0),
            createTestEvent("Music Event", EventState.PUBLISHED, Arrays.asList("Music", "Indoor"), 10, 0, 0, 0),
            createTestEvent("Mixed Event", EventState.PUBLISHED, Arrays.asList("Sports", "Music"), 10, 0, 0, 0)
        );

        // Exact match (case-insensitive)
        List<Event> result = FilterUtils.filter(events, "All", "sports", -1);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByTag_SubstringMatch() {
        List<Event> events = Arrays.asList(
            createTestEvent("Sports Event", EventState.PUBLISHED, Arrays.asList("Sports", "Outdoor"), 10, 0, 0, 0),
            createTestEvent("Music Event", EventState.PUBLISHED, Arrays.asList("Music", "Indoor"), 10, 0, 0, 0),
            createTestEvent("Sportswear Event", EventState.PUBLISHED, Arrays.asList("Sportswear"), 10, 0, 0, 0)
        );

        // Substring match - "sport" should match "Sports" and "Sportswear"
        List<Event> result = FilterUtils.filter(events, "All", "sport", -1);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByTag_NoMatch() {
        List<Event> events = Arrays.asList(
            createTestEvent("Sports Event", EventState.PUBLISHED, Arrays.asList("Sports", "Outdoor"), 10, 0, 0, 0),
            createTestEvent("Music Event", EventState.PUBLISHED, Arrays.asList("Music", "Indoor"), 10, 0, 0, 0)
        );

        List<Event> result = FilterUtils.filter(events, "All", "Technology", -1);
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterByTag_AllFilter() {
        List<Event> events = Arrays.asList(
            createTestEvent("Sports Event", EventState.PUBLISHED, Arrays.asList("Sports"), 10, 0, 0, 0),
            createTestEvent("Music Event", EventState.PUBLISHED, Arrays.asList("Music"), 10, 0, 0, 0)
        );

        List<Event> result = FilterUtils.filter(events, "All", "All", -1);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByTag_EmptyInterest() {
        List<Event> events = Arrays.asList(
            createTestEvent("Sports Event", EventState.PUBLISHED, Arrays.asList("Sports"), 10, 0, 0, 0),
            createTestEvent("Music Event", EventState.PUBLISHED, Arrays.asList("Music"), 10, 0, 0, 0)
        );

        // Empty string should behave like "All"
        List<Event> result = FilterUtils.filter(events, "All", "", -1);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByTag_EventWithNoTags() {
        List<Event> events = Arrays.asList(
            createTestEvent("Sports Event", EventState.PUBLISHED, Arrays.asList("Sports"), 10, 0, 0, 0),
            createTestEvent("No Tags Event", EventState.PUBLISHED, null, 10, 0, 0, 0)
        );

        // Filter by tag should exclude events with no tags
        List<Event> result = FilterUtils.filter(events, "All", "Sports", -1);
        assertEquals(1, result.size());
        assertEquals("Sports Event", result.get(0).getTitle());
    }

    // ==================== Availability Filtering Tests ====================

    @Test
    public void testFilterByAvailability_All() {
        List<Event> events = Arrays.asList(
            createTestEvent("Full Event", EventState.PUBLISHED, null, 10, 10, 0, 0),
            createTestEvent("Not Full Event", EventState.PUBLISHED, null, 10, 5, 0, 0)
        );

        List<Event> result = FilterUtils.filter(events, "All", "All", -1);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByAvailability_HasFreeSpots() {
        List<Event> events = Arrays.asList(
            createTestEvent("Full Event", EventState.PUBLISHED, null, 10, 10, 0, 0),
            createTestEvent("Not Full Event", EventState.PUBLISHED, null, 10, 5, 0, 0),
            createTestEvent("Partially Full", EventState.PUBLISHED, null, 10, 3, 5, 0) // 8/10 spots taken
        );

        // Code 0 = Has Free Spots
        List<Event> result = FilterUtils.filter(events, "All", "All", 0);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByAvailability_Full() {
        List<Event> events = Arrays.asList(
            createTestEvent("Full Event", EventState.PUBLISHED, null, 10, 10, 0, 0),
            createTestEvent("Not Full Event", EventState.PUBLISHED, null, 10, 5, 0, 0),
            createTestEvent("Full via Invited", EventState.PUBLISHED, null, 10, 5, 5, 0) // 10/10 via enrolled+invited
        );

        // Code 1 = Full
        List<Event> result = FilterUtils.filter(events, "All", "All", 1);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByAvailability_HasWaitingList() {
        List<Event> events = Arrays.asList(
            createTestEvent("No Waiting List", EventState.PUBLISHED, null, 10, 5, 0, 0),
            createTestEvent("Has Waiting List", EventState.PUBLISHED, null, 10, 10, 0, 5),
            createTestEvent("Also Has Waiting", EventState.PUBLISHED, null, 10, 5, 0, 3)
        );

        // Code 2 = Has Waiting List
        List<Event> result = FilterUtils.filter(events, "All", "All", 2);
        assertEquals(2, result.size());
    }

    // ==================== Combined Filtering Tests ====================

    @Test
    public void testCombinedFiltering() {
        List<Event> events = Arrays.asList(
            createTestEvent("Open Sports", EventState.PUBLISHED, Arrays.asList("Sports"), 10, 5, 0, 0),
            createTestEvent("Closed Sports", EventState.CLOSED, Arrays.asList("Sports"), 10, 5, 0, 0),
            createTestEvent("Open Music", EventState.PUBLISHED, Arrays.asList("Music"), 10, 5, 0, 0),
            createTestEvent("Open Full Sports", EventState.PUBLISHED, Arrays.asList("Sports"), 10, 10, 0, 0)
        );

        // Filter: Open + Sports + Has Free Spots
        List<Event> result = FilterUtils.filter(events, "Open", "Sports", 0);
        assertEquals(1, result.size());
        assertEquals("Open Sports", result.get(0).getTitle());
    }

    // ==================== Edge Case Tests ====================

    @Test
    public void testFilterWithNullEvents() {
        List<Event> result = FilterUtils.filter(null, "All", "All", -1);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterWithEmptyList() {
        List<Event> result = FilterUtils.filter(new ArrayList<>(), "All", "All", -1);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterDoesNotMutateOriginalList() {
        List<Event> original = new ArrayList<>(Arrays.asList(
            createTestEvent("Open Event", EventState.PUBLISHED, null, 10, 0, 0, 0),
            createTestEvent("Closed Event", EventState.CLOSED, null, 10, 0, 0, 0)
        ));
        int originalSize = original.size();

        List<Event> result = FilterUtils.filter(original, "Open", "All", -1);
        
        assertEquals(originalSize, original.size()); // Original list unchanged
        assertEquals(1, result.size()); // Filtered list has only matching event
    }

    // ==================== Helper Method Tests ====================

    @Test
    public void testMatchesStatus_NullEvent() {
        assertFalse(FilterUtils.matchesStatus(null, "Open"));
    }

    @Test
    public void testMatchesInterest_NullEvent() {
        assertFalse(FilterUtils.matchesInterest(null, "sports"));
    }

    @Test
    public void testMatchesAvailability_NullEvent() {
        assertFalse(FilterUtils.matchesAvailability(null, 0));
    }
}
