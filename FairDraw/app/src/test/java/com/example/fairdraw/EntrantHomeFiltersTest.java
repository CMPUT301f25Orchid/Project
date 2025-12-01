package com.example.fairdraw;

import static org.junit.Assert.*;

import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.Others.FilterUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for FilterUtils filtering logic used by EntrantHomeActivity.
 */
public class EntrantHomeFiltersTest {

    private List<Event> testEvents;
    private Event eventPublished;
    private Event eventClosed;
    private Event eventDraft;
    private Event eventFull;
    private Event eventWithWaitlist;

    @Before
    public void setUp() {
        testEvents = new ArrayList<>();

        // Event 1: Published, has free spots, tags: Sports, Outdoor
        eventPublished = new Event("Sports Event", "A sports competition", 10, new Date(), new Date(), "Stadium", "org1", 25.0f, "path1", "slug1");
        eventPublished.setState(EventState.PUBLISHED);
        eventPublished.setTags(Arrays.asList("Sports", "Outdoor"));
        eventPublished.setEnrolledList(new ArrayList<>(Arrays.asList("user1", "user2"))); // 2 enrolled out of 10
        testEvents.add(eventPublished);

        // Event 2: Closed, tags: Music, Indoor
        eventClosed = new Event("Music Concert", "A music event", 5, new Date(), new Date(), "Hall", "org2", 50.0f, "path2", "slug2");
        eventClosed.setState(EventState.CLOSED);
        eventClosed.setTags(Arrays.asList("Music", "Indoor"));
        eventClosed.setEnrolledList(new ArrayList<>(Arrays.asList("user1", "user2", "user3")));
        testEvents.add(eventClosed);

        // Event 3: Draft, no tags
        eventDraft = new Event("Draft Event", "A draft event", 20, new Date(), new Date(), "TBD", "org3", 0.0f, "path3", "slug3");
        eventDraft.setState(EventState.DRAFT);
        testEvents.add(eventDraft);

        // Event 4: Published, full (enrolled == capacity)
        eventFull = new Event("Full Event", "A fully booked event", 3, new Date(), new Date(), "Venue", "org4", 100.0f, "path4", "slug4");
        eventFull.setState(EventState.PUBLISHED);
        eventFull.setTags(Arrays.asList("Workshop", "Education"));
        eventFull.setEnrolledList(new ArrayList<>(Arrays.asList("user1", "user2", "user3"))); // 3 enrolled, capacity 3
        testEvents.add(eventFull);

        // Event 5: Published, has waiting list
        eventWithWaitlist = new Event("Waitlist Event", "An event with waiting list", 5, new Date(), new Date(), "Center", "org5", 15.0f, "path5", "slug5");
        eventWithWaitlist.setState(EventState.PUBLISHED);
        eventWithWaitlist.setTags(Arrays.asList("Sports", "Competition"));
        eventWithWaitlist.setEnrolledList(new ArrayList<>(Arrays.asList("user1", "user2")));
        eventWithWaitlist.setWaitingList(new ArrayList<>(Arrays.asList("user3", "user4"))); // Has waiting list
        testEvents.add(eventWithWaitlist);
    }

    // ============== Status Filter Tests ==============

    @Test
    public void testFilterByStatusAll_returnsAllEvents() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "All", 0);
        assertEquals(5, result.size());
    }

    @Test
    public void testFilterByStatusOpen_returnsPublishedEvents() {
        List<Event> result = FilterUtils.filter(testEvents, "Open", "All", 0);
        assertEquals(3, result.size());
        for (Event e : result) {
            assertEquals(EventState.PUBLISHED, e.getState());
        }
    }

    @Test
    public void testFilterByStatusClosed_returnsClosedEvents() {
        List<Event> result = FilterUtils.filter(testEvents, "Closed", "All", 0);
        assertEquals(1, result.size());
        assertEquals(EventState.CLOSED, result.get(0).getState());
    }

    @Test
    public void testFilterByStatusDraft_returnsDraftEvents() {
        List<Event> result = FilterUtils.filter(testEvents, "Draft", "All", 0);
        assertEquals(1, result.size());
        assertEquals(EventState.DRAFT, result.get(0).getState());
    }

    // ============== Interest Filter Tests ==============

    @Test
    public void testFilterByInterestExactMatch() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "Sports", 0);
        assertEquals(2, result.size()); // eventPublished and eventWithWaitlist
    }

    @Test
    public void testFilterByInterestCaseInsensitive() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "sports", 0);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByInterestSubstring() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "door", 0);
        // Should match "Outdoor" and "Indoor"
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByInterestNoMatch() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "NonExistentTag", 0);
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterByInterestAll_noFiltering() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "All", 0);
        assertEquals(5, result.size());
    }

    @Test
    public void testFilterByInterestEmpty_noFiltering() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "", 0);
        assertEquals(5, result.size());
    }

    @Test
    public void testFilterByInterestNull_noFiltering() {
        List<Event> result = FilterUtils.filter(testEvents, "All", null, 0);
        assertEquals(5, result.size());
    }

    // ============== Availability Filter Tests ==============

    @Test
    public void testFilterByAvailabilityAll_returnsAllEvents() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "All", 0);
        assertEquals(5, result.size());
    }

    @Test
    public void testFilterByAvailabilityHasFreeSpots() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "All", 1);
        // eventPublished (2/10), eventClosed (3/5), eventDraft (0/20), eventWithWaitlist (2/5)
        // eventFull (3/3) should be excluded
        assertEquals(4, result.size());
        assertFalse(result.contains(eventFull));
    }

    @Test
    public void testFilterByAvailabilityFull() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "All", 2);
        // Only eventFull should be included
        assertEquals(1, result.size());
        assertTrue(result.contains(eventFull));
    }

    @Test
    public void testFilterByAvailabilityHasWaitingList() {
        List<Event> result = FilterUtils.filter(testEvents, "All", "All", 3);
        // Only eventWithWaitlist should be included
        assertEquals(1, result.size());
        assertTrue(result.contains(eventWithWaitlist));
    }

    // ============== Combined Filter Tests ==============

    @Test
    public void testCombinedFilters_statusAndInterest() {
        List<Event> result = FilterUtils.filter(testEvents, "Open", "Sports", 0);
        assertEquals(2, result.size());
        for (Event e : result) {
            assertEquals(EventState.PUBLISHED, e.getState());
        }
    }

    @Test
    public void testCombinedFilters_statusInterestAndAvailability() {
        // Open status, Sports interest, has free spots
        List<Event> result = FilterUtils.filter(testEvents, "Open", "Sports", 1);
        assertEquals(2, result.size());
    }

    @Test
    public void testCombinedFilters_noMatchingResults() {
        // Closed status with Sports interest (no closed sports events)
        List<Event> result = FilterUtils.filter(testEvents, "Closed", "Sports", 0);
        assertEquals(0, result.size());
    }

    // ============== Edge Cases ==============

    @Test
    public void testFilterWithNullEventsList() {
        List<Event> result = FilterUtils.filter(null, "All", "All", 0);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterWithEmptyEventsList() {
        List<Event> result = FilterUtils.filter(new ArrayList<>(), "All", "All", 0);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterEventWithNullTags() {
        Event eventNullTags = new Event("Null Tags Event", "No tags", 10, new Date(), new Date(), "Place", "org", 0.0f, "path", "slug");
        eventNullTags.setState(EventState.PUBLISHED);
        eventNullTags.setTags(null);
        
        List<Event> events = new ArrayList<>();
        events.add(eventNullTags);
        
        // Should not match any interest filter
        List<Event> result = FilterUtils.filter(events, "All", "SomeTag", 0);
        assertEquals(0, result.size());
        
        // Should still appear with "All" interest filter
        List<Event> resultAll = FilterUtils.filter(events, "All", "All", 0);
        assertEquals(1, resultAll.size());
    }

    @Test
    public void testFilterEventWithNullCapacity() {
        Event eventNullCapacity = new Event();
        eventNullCapacity.setTitle("No Capacity Event");
        eventNullCapacity.setState(EventState.PUBLISHED);
        eventNullCapacity.setCapacity(null);
        eventNullCapacity.setEnrolledList(new ArrayList<>(Arrays.asList("user1")));
        
        List<Event> events = new ArrayList<>();
        events.add(eventNullCapacity);
        
        // Availability filter with has free spots (enrolled < capacity)
        // With null capacity (treated as 0), 1 enrolled >= 0 capacity
        List<Event> result = FilterUtils.filter(events, "All", "All", 1);
        assertEquals(0, result.size()); // Not included since 1 >= 0
    }

    // ============== Individual Match Method Tests ==============

    @Test
    public void testMatchesStatus_withNullEvent() {
        assertFalse(FilterUtils.matchesStatus(null, "Open"));
    }

    @Test
    public void testMatchesStatus_withNullState() {
        Event eventNullState = new Event();
        assertFalse(FilterUtils.matchesStatus(eventNullState, "Open"));
    }

    @Test
    public void testMatchesInterest_withNullEvent() {
        assertFalse(FilterUtils.matchesInterest(null, "Sports"));
    }

    @Test
    public void testMatchesAvailability_withNullEvent() {
        assertFalse(FilterUtils.matchesAvailability(null, 1));
    }

    // ============== Extract Available Tags Tests ==============

    @Test
    public void testExtractAvailableTags_returnsDistinctSortedTags() {
        List<String> tags = FilterUtils.extractAvailableTags(testEvents);
        
        // Expected tags: Competition, Education, Indoor, Music, Outdoor, Sports, Workshop
        assertEquals(7, tags.size());
        
        // Verify sorted alphabetically (case-insensitive)
        for (int i = 0; i < tags.size() - 1; i++) {
            assertTrue(tags.get(i).compareToIgnoreCase(tags.get(i + 1)) <= 0);
        }
    }

    @Test
    public void testExtractAvailableTags_withNullList() {
        List<String> tags = FilterUtils.extractAvailableTags(null);
        assertNotNull(tags);
        assertEquals(0, tags.size());
    }

    @Test
    public void testExtractAvailableTags_withEmptyList() {
        List<String> tags = FilterUtils.extractAvailableTags(new ArrayList<>());
        assertNotNull(tags);
        assertEquals(0, tags.size());
    }

    @Test
    public void testExtractAvailableTags_excludesNullAndEmptyTags() {
        Event eventWithNullTag = new Event();
        eventWithNullTag.setTags(Arrays.asList("Valid", null, "", "Another"));
        
        List<Event> events = new ArrayList<>();
        events.add(eventWithNullTag);
        
        List<String> tags = FilterUtils.extractAvailableTags(events);
        assertEquals(2, tags.size()); // Only "Another" and "Valid"
        assertFalse(tags.contains(null));
        assertFalse(tags.contains(""));
    }
}
