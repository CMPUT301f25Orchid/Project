package com.example.fairdraw.Models;

import static org.junit.Assert.*;

import com.example.fairdraw.R;
import com.example.fairdraw.Others.EventState;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class EventTest {

    private Event event;

    @Before
    public void setup() {
        event = new Event("T", "D", 3, new Date(), new Date(), "L", "org", 1.0f, "path", "slug");
    }

    @Test
    public void constructor_setsFields_and_uuid_present() {
        assertNotNull(event.getUuid());
        assertEquals("T", event.getTitle());
        assertEquals((Integer)3, event.getCapacity());
        assertEquals("L", event.getLocation());
        assertEquals("org", event.getOrganizer());
        assertEquals((Float)1.0f, event.getPrice());
    }

    @Test
    public void nullDeviceChecks_returnFalseSafely() {
        event.setWaitingList(null);
        event.setInvitedList(null);
        event.setEnrolledList(null);
        event.setCancelledList(null);

        assertFalse(event.isEnrolled(null));
        assertFalse(event.isInvited(null));
        assertFalse(event.isOnWaitingList(null));
    }

    @Test
    public void canJoinWaitingList_and_buttonText_states() {
        String dev = "dev-1";
        event.getEnrolledList().add("x");
        event.getInvitedList().add("y");

        // Not related device -> can join
        assertTrue(event.canJoinWaitingList(dev));
        assertEquals(R.string.join_lottery_waitlist, event.getJoinWaitlistButtonText(dev));

        // Add to waiting list
        event.getWaitingList().add(dev);
        assertFalse(event.canJoinWaitingList(dev));
        assertEquals(R.string.on_waitlist, event.getJoinWaitlistButtonText(dev));

        // Simulate invited
        String dev2 = "dev-2";
        event.getWaitingList().remove(dev);
        event.getInvitedList().add(dev2);
        assertFalse(event.canJoinWaitingList(dev2));
        assertEquals(R.string.invitation_sent, event.getJoinWaitlistButtonText(dev2));

        // Waitlist full
        event.setWaitingListLimit(0);
        assertEquals(R.string.waitlist_full, event.getJoinWaitlistButtonText("newdev"));

        // Null device
        assertEquals(R.string.unavailable, event.getJoinWaitlistButtonText(null));
    }

    @Test
    public void drawLotteryWinners_behaviour_counts_and_removal() {
        // empty invited/enrolled; capacity 3
        event.getWaitingList().clear();
        event.getInvitedList().clear();
        event.getEnrolledList().clear();

        // Add 2 people to waiting; spotsToFill = 3
        event.getWaitingList().add("a");
        event.getWaitingList().add("b");

        List<String> invited = event.drawLotteryWinners();
        // invited should contain both since only 2 waiting
        assertEquals(2, invited.size());
        assertEquals(0, event.getWaitingList().size());

        // If capacity already filled
        event.getInvitedList().clear();
        event.getEnrolledList().add("z");
        event.getEnrolledList().add("y");
        event.getEnrolledList().add("x");
        event.getWaitingList().add("c");
        List<String> invited2 = event.drawLotteryWinners();
        // no new invites
        assertEquals(0, invited2.size());
    }

    @Test
    public void replace_accept_cancel_winner_flow() {
        event.getInvitedList().clear();
        event.getWaitingList().clear();
        event.getCancelledList().clear();
        event.getEnrolledList().clear();

        event.getInvitedList().add("old");
        event.getWaitingList().add("new");

        String replacement = event.replaceLotteryWinner("old");
        assertEquals("new", replacement);
        assertTrue(event.getCancelledList().contains("old"));
        assertTrue(event.getInvitedList().contains("new"));

        // accept the new winner
        event.acceptLotteryWinner("new");
        assertTrue(event.getEnrolledList().contains("new"));
        assertFalse(event.getInvitedList().contains("new"));

        // cancel a winner (no-op if not invited)
        event.cancelLotteryWinner("not-invited");
        // cancel an invited
        event.getInvitedList().add("canc");
        event.cancelLotteryWinner("canc");
        assertTrue(event.getCancelledList().contains("canc"));
    }

    @Test
    public void waitlist_locations_put_and_get() {
        Event.EntrantLocation loc = new Event.EntrantLocation(1.1, 2.2);
        event.putWaitlistLocation("d1", loc);
        Event.EntrantLocation got = event.getWaitlistLocation("d1");
        assertNotNull(got);
        assertEquals(1.1, got.getLat(), 1e-9);
        assertEquals(2.2, got.getLng(), 1e-9);

        // get non-existent
        assertNull(event.getWaitlistLocation("missing"));
    }

    @Test
    public void state_and_setters_behave() {
        event.setState(EventState.PUBLISHED);
        assertEquals(EventState.PUBLISHED, event.getState());
        event.setTitle("NewTitle");
        assertEquals("NewTitle", event.getTitle());
        event.setCapacity(10);
        assertEquals((Integer)10, event.getCapacity());
    }
}
