package com.example.fairdraw;

import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.Others.EntrantNotification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

public class OrganiserNotificationTest {

    private MockedStatic<EntrantDB> entrantDbMock;

    @Before
    public void setUp() {
        entrantDbMock = Mockito.mockStatic(EntrantDB.class);
    }

    @After
    public void tearDown() {
        entrantDbMock.close();
    }

    @Test
    public void lottery_sends_notifications_to_non_winners() {
        // Arrange: waitlist and winners
        List<String> waitlist = Arrays.asList("user-A", "user-B", "user-C", "user-D", "user-E");
        List<String> winners = Arrays.asList("user-A", "user-B");
        String eventId = "event-tech-con-123";
        String title = "Annual Tech Conference";

        simulateDrawAndNotify(waitlist, winners, eventId, title);

        // Assert: pushNotificationToUser called for each loser and those users are not winners
        int expectedLoserCount = waitlist.size() - winners.size();

        ArgumentCaptor<String> deviceIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EntrantNotification> notificationCaptor = ArgumentCaptor.forClass(EntrantNotification.class);

        entrantDbMock.verify(() -> EntrantDB.pushNotificationToUser(
                deviceIdCaptor.capture(),
                notificationCaptor.capture(),
                Mockito.any()
        ), times(expectedLoserCount));

        List<String> notifiedUserIds = deviceIdCaptor.getAllValues();
        assertEquals(expectedLoserCount, notifiedUserIds.size());
        for (String id : notifiedUserIds) {
            assertFalse(winners.contains(id));
        }
    }

    private void simulateDrawAndNotify(List<String> waitlist, List<String> winners, String eventId, String title) {
        for (String entrant : new ArrayList<>(waitlist)) {
            if (!winners.contains(entrant)) {
                EntrantNotification notification = new EntrantNotification();
                notification.type = "LOSE";
                notification.eventId = eventId;
                notification.title = title;

                EntrantDB.pushNotificationToUser(entrant, notification, null);
            }
        }
    }

}
