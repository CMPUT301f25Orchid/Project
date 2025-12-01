package com.example.fairdraw;

import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.Models.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Verifies the state of an Event lists from an Entrant perspective after a lottery.
 * Covers US 01.04.01 and US 01.04.02.
 */
public class EntrantNotificationTest {

    private MockedStatic<EntrantDB> entrantDbMock;

    @Before
    public void setUp() {
        // Prevent Firebase from running during unit tests
        entrantDbMock = Mockito.mockStatic(EntrantDB.class);
    }

    @After
    public void tearDown() {
        if (entrantDbMock != null) {
            entrantDbMock.close();
        }
    }

    /**
     * US 01.04.01 winner goes from waiting list to invited list.
     */
    @Test
    public void winnerMovedToInvitedList() {
        Event event = new Event();
        event.setCapacity(1);
        List<String> originalWaitingList = Arrays.asList("winner-entrant", "loser-entrant");
        event.setWaitingList(new ArrayList<>(originalWaitingList));

        event.drawLotteryWinners();

        List<String> winners = event.getInvitedList();
        List<String> currentWaitingList = event.getWaitingList();

        assertEquals(1, winners.size());
        assertEquals(1, currentWaitingList.size());

        List<String> allEntrants = new ArrayList<>(winners);
        allEntrants.addAll(currentWaitingList);

        assertTrue(allEntrants.containsAll(originalWaitingList));
        assertTrue(originalWaitingList.containsAll(allEntrants));
    }

    /**
     * US 01.04.02 loser is not in the invited list.
     */
    @Test
    public void loserNotInvitedStaysOnWaitingList() {
        Event event = new Event();
        event.setCapacity(1);
        List<String> originalWaitingList = Arrays.asList("winner-entrant", "loser-entrant");
        event.setWaitingList(new ArrayList<>(originalWaitingList));

        event.drawLotteryWinners();

        List<String> winners = event.getInvitedList();
        List<String> currentWaitingList = event.getWaitingList();

        assertEquals(1, winners.size());

        String winnerId = winners.get(0);
        String loserId = originalWaitingList.get(0).equals(winnerId)
                ? originalWaitingList.get(1)
                : originalWaitingList.get(0);

        assertFalse(winners.contains(loserId));
        assertTrue(currentWaitingList.contains(loserId));
    }
}
