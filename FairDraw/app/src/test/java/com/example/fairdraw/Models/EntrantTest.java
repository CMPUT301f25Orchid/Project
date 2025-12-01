package com.example.fairdraw.Models;

import com.example.fairdraw.Others.EntrantNotification;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class EntrantTest {

    @Test
    public void constructor_setsDeviceId_and_initializesLists() {
        Entrant e = new Entrant("dev-9");
        assertEquals("dev-9", e.getDeviceId());
        assertNotNull(e.getEventHistory());
        assertNotNull(e.getNotifications());
        assertNotNull(e.getNotificationPrefs());
    }

    @Test
    public void addEventToHistory_and_setEventHistory_behave() {
        Entrant e = new Entrant();
        e.addEventToHistory("evt1");
        List<String> hist = e.getEventHistory();
        assertEquals(1, hist.size());
        assertEquals("evt1", hist.get(0));

        ArrayList<String> newHist = new ArrayList<>();
        newHist.add("a");
        e.setEventHistory(newHist);
        assertSame(newHist, e.getEventHistory());
    }

    @Test
    public void notificationPrefs_copyAndNotifications_mutate() {
        Entrant e = new Entrant();
        HashMap<String, Boolean> prefs = new HashMap<>();
        prefs.put("email", true);
        e.setNotificationPrefs(prefs);
        assertTrue(e.getNotificationPrefs().containsKey("email"));
        assertEquals(Boolean.TRUE, e.getNotificationPrefs().get("email"));

        EntrantNotification n = new EntrantNotification();
        e.addNotification(n);
        assertEquals(1, e.getNotifications().size());
        e.removeNotification(n);
        assertTrue(e.getNotifications().isEmpty());

        ArrayList<EntrantNotification> newNotifs = new ArrayList<>();
        newNotifs.add(n);
        e.setNotifications(newNotifs);
        assertSame(newNotifs, e.getNotifications());
    }

    @Test
    public void setDeviceId_allowsNull_and_updates() {
        Entrant e = new Entrant();
        e.setDeviceId("d2");
        assertEquals("d2", e.getDeviceId());
        e.setDeviceId(null);
        assertNull(e.getDeviceId());
    }
}
