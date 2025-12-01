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
 * Unit tests for the FilterUtils class used by EntrantHomeActivity.
 * Tests the filtering logic for status, interest (tags), and availability.
 */
public class EntrantHomeActivityFiltersTest {

    private List<Event> testEvents;
    private Event openEventWithSports;
    private Event closedEventWithArts;
    private Event draftEvent;
    private Event fullEvent;
    private Event eventWithWaitingList;

    @Before
    public void setup() {
        testEvents = new ArrayList<>();

        // Event 1: Open (Published), has "Sports and Recreation" tag, not full, no waiting list
        openEventWithSports = new Event("Sports Event", "A sports event", 10, new Date(), new Date(), "Location", "org1", 5.0f, "path", "slug1");
        openEventWithSports.setState(EventState.PUBLISHED);
        openEventWithSports.setTags(Arrays.asList("Sports and Recreation", "Outdoor"));
        openEventWithSports.setEnrolledList(new ArrayList<>(Arrays.asList("user1", "user2"))); // 2/10 enrolled
        openEventWithSports.setWaitingList(new ArrayList<>());
        testEvents.add(openEventWithSports);

        // Event 2: Closed, has "Arts/Culture" tag, not full
        closedEventWithArts = new Event("Arts Event", "An arts event", 20, new Date(), new Date(), "Location", "org2", 10.0f, "path", "slug2");
        closedEventWithArts.setState(EventState.CLOSED);
        closedEventWithArts.setTags(Arrays.asList("Arts/Culture"));
        closedEventWithArts.setEnrolledList(new ArrayList<>(Arrays.asList("user3", "user4", "user5"))); // 3/20 enrolled
        closedEventWithArts.setWaitingList(new ArrayList<>());
        testEvents.add(closedEventWithArts);

        // Event 3: Draft, has "Education" tag
        draftEvent = new Event("Draft Event", "A draft event", 5, new Date(), new Date(), "Location", "org3", 0.0f, "path", "slug3");
        draftEvent.setState(EventState.DRAFT);
        draftEvent.setTags(Arrays.asList("Education"));
        draftEvent.setEnrolledList(new ArrayList<>());
        draftEvent.setWaitingList(new ArrayList<>());
        testEvents.add(draftEvent);

        // Event 4: Open (Published), full event (enrolled >= capacity)
        fullEvent = new Event("Full Event", "A full event", 3, new Date(), new Date(), "Location", "org4", 15.0f, "path", "slug4");
        fullEvent.setState(EventState.PUBLISHED);
        fullEvent.setTags(Arrays.asList("Sports and Recreation"));
        fullEvent.setEnrolledList(new ArrayList<>(Arrays.asList("user6", "user7", "user8"))); // 3/3 enrolled = full
        fullEvent.setWaitingList(new ArrayList<>());
        testEvents.add(fullEvent);

        // Event 5: Open (Published), has waiting list
        eventWithWaitingList = new Event("Waiting List Event", "An event with waiting list", 5, new Date(), new Date(), "Location", "org5", 20.0f, "path", "slug5");
        eventWithWaitingList.setState(EventState.PUBLISHED);
        eventWithWaitingList.setTags(Arrays.asList("Education", "Workshop"));
        eventWithWaitingList.setEnrolledList(new ArrayList<>(Arrays.asList("user9", "user10"))); // 2/5 enrolled
        eventWithWaitingList.setWaitingList(new ArrayList<>(Arrays.asList("wait1", "wait2", "wait3"))); // 3 on waiting list
        testEvents.add(eventWithWaitingList);
    }

    @Test
    public void testFilterByStatus_All() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "All", "All", FilterUtils.AVAILABILITY_ALL);
        assertEquals(5, result.size());
    }

    @Test
    public void testFilterByStatus_Open() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "Open", "All", FilterUtils.AVAILABILITY_ALL);
        assertEquals(3, result.size()); // openEventWithSports, fullEvent, eventWithWaitingList
        for (Event e : result) {
            assertEquals(EventState.PUBLISHED, e.getState());
        }
    }

    @Test
    public void testFilterByStatus_Closed() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "Closed", "All", FilterUtils.AVAILABILITY_ALL);
        assertEquals(1, result.size());
        assertEquals(EventState.CLOSED, result.get(0).getState());
    }

    @Test
    public void testFilterByStatus_Draft() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "Draft", "All", FilterUtils.AVAILABILITY_ALL);
        assertEquals(1, result.size());
        assertEquals(EventState.DRAFT, result.get(0).getState());
    }

    @Test
    public void testFilterByInterest_Sports() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "All", "Sports and Recreation", FilterUtils.AVAILABILITY_ALL);
        assertEquals(2, result.size()); // openEventWithSports and fullEvent
    }

    @Test
    public void testFilterByInterest_Arts() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "All", "Arts/Culture", FilterUtils.AVAILABILITY_ALL);
        assertEquals(1, result.size());
        assertEquals("Arts Event", result.get(0).getTitle());
    }

    @Test
    public void testFilterByInterest_Education() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "All", "Education", FilterUtils.AVAILABILITY_ALL);
        assertEquals(2, result.size()); // draftEvent and eventWithWaitingList
    }

    @Test
    public void testFilterByInterest_CaseInsensitive() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "All", "SPORTS AND RECREATION", FilterUtils.AVAILABILITY_ALL);
        assertEquals(2, result.size());

        result = FilterUtils.applyFilters(testEvents, "All", "education", FilterUtils.AVAILABILITY_ALL);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterByInterest_NonExistent() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "All", "NonExistentTag", FilterUtils.AVAILABILITY_ALL);
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterByAvailability_HasFreeSpots() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "All", "All", FilterUtils.AVAILABILITY_HAS_FREE_SPOTS);
        assertEquals(4, result.size()); // openEventWithSports (2/10), closedEventWithArts (3/20), draftEvent (0/5), eventWithWaitingList (2/5)
        // fullEvent (3/3) should NOT be included
        for (Event e : result) {
            assertTrue(e.getEnrolledList().size() < e.getCapacity());
        }
    }

    @Test
    public void testFilterByAvailability_Full() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "All", "All", FilterUtils.AVAILABILITY_FULL);
        assertEquals(1, result.size());
        assertEquals("Full Event", result.get(0).getTitle());
    }

    @Test
    public void testFilterByAvailability_HasWaitingList() {
        List<Event> result = FilterUtils.applyFilters(testEvents, "All", "All", FilterUtils.AVAILABILITY_HAS_WAITING_LIST);
        assertEquals(1, result.size());
        assertEquals("Waiting List Event", result.get(0).getTitle());
    }

    @Test
    public void testCombinedFilters_StatusAndInterest() {
        // Open events with Sports and Recreation tag
        List<Event> result = FilterUtils.applyFilters(testEvents, "Open", "Sports and Recreation", FilterUtils.AVAILABILITY_ALL);
        assertEquals(2, result.size()); // openEventWithSports and fullEvent
    }

    @Test
    public void testCombinedFilters_StatusAndAvailability() {
        // Open events that have free spots
        List<Event> result = FilterUtils.applyFilters(testEvents, "Open", "All", FilterUtils.AVAILABILITY_HAS_FREE_SPOTS);
        assertEquals(2, result.size()); // openEventWithSports and eventWithWaitingList
    }

    @Test
    public void testCombinedFilters_AllThree() {
        // Open events with Education tag that have free spots
        List<Event> result = FilterUtils.applyFilters(testEvents, "Open", "Education", FilterUtils.AVAILABILITY_HAS_FREE_SPOTS);
        assertEquals(1, result.size());
        assertEquals("Waiting List Event", result.get(0).getTitle());
    }

    @Test
    public void testMatchesStatus_NullState() {
        Event eventWithNullState = new Event("Null State Event", "Test", 5, new Date(), new Date(), "Loc", "org", 0.0f, "path", "slug");
        eventWithNullState.setState(null);
        
        assertFalse(FilterUtils.matchesStatus(eventWithNullState, "Open"));
        assertFalse(FilterUtils.matchesStatus(eventWithNullState, "Closed"));
        assertTrue(FilterUtils.matchesStatus(eventWithNullState, "All"));
    }

    @Test
    public void testMatchesInterest_NullTags() {
        Event eventWithNullTags = new Event("No Tags Event", "Test", 5, new Date(), new Date(), "Loc", "org", 0.0f, "path", "slug");
        eventWithNullTags.setTags(null);
        
        assertTrue(FilterUtils.matchesInterest(eventWithNullTags, "All"));
        assertFalse(FilterUtils.matchesInterest(eventWithNullTags, "Sports"));
    }

    @Test
    public void testMatchesInterest_EmptyTags() {
        Event eventWithEmptyTags = new Event("Empty Tags Event", "Test", 5, new Date(), new Date(), "Loc", "org", 0.0f, "path", "slug");
        eventWithEmptyTags.setTags(new ArrayList<>());
        
        assertTrue(FilterUtils.matchesInterest(eventWithEmptyTags, "All"));
        assertFalse(FilterUtils.matchesInterest(eventWithEmptyTags, "Sports"));
    }

    @Test
    public void testMatchesAvailability_NullCapacity() {
        Event eventWithNullCapacity = new Event();
        eventWithNullCapacity.setCapacity(null);
        eventWithNullCapacity.setEnrolledList(new ArrayList<>(Arrays.asList("user1")));
        
        // When capacity is null (treated as 0), enrolled (1) >= capacity (0) means full
        assertTrue(FilterUtils.matchesAvailability(eventWithNullCapacity, FilterUtils.AVAILABILITY_ALL));
        assertFalse(FilterUtils.matchesAvailability(eventWithNullCapacity, FilterUtils.AVAILABILITY_HAS_FREE_SPOTS));
        assertTrue(FilterUtils.matchesAvailability(eventWithNullCapacity, FilterUtils.AVAILABILITY_FULL));
    }

    @Test
    public void testMatchesAvailability_NullLists() {
        Event eventWithNullLists = new Event();
        eventWithNullLists.setCapacity(10);
        eventWithNullLists.setEnrolledList(null);
        eventWithNullLists.setWaitingList(null);
        
        assertTrue(FilterUtils.matchesAvailability(eventWithNullLists, FilterUtils.AVAILABILITY_ALL));
        assertTrue(FilterUtils.matchesAvailability(eventWithNullLists, FilterUtils.AVAILABILITY_HAS_FREE_SPOTS)); // 0 < 10
        assertFalse(FilterUtils.matchesAvailability(eventWithNullLists, FilterUtils.AVAILABILITY_FULL)); // 0 >= 10 is false
        assertFalse(FilterUtils.matchesAvailability(eventWithNullLists, FilterUtils.AVAILABILITY_HAS_WAITING_LIST)); // 0 > 0 is false
    }

    @Test
    public void testApplyFilters_NullEventsList() {
        List<Event> result = FilterUtils.applyFilters(null, "All", "All", FilterUtils.AVAILABILITY_ALL);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyFilters_EmptyEventsList() {
        List<Event> result = FilterUtils.applyFilters(new ArrayList<>(), "All", "All", FilterUtils.AVAILABILITY_ALL);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyFilters_ListWithNullEvent() {
        List<Event> listWithNull = new ArrayList<>(testEvents);
        listWithNull.add(null);
        
        List<Event> result = FilterUtils.applyFilters(listWithNull, "All", "All", FilterUtils.AVAILABILITY_ALL);
        assertEquals(5, result.size()); // Should skip the null entry
    }
}
