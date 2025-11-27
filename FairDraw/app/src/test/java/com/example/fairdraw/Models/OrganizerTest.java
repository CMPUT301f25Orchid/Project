package com.example.fairdraw.Models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OrganizerTest {

    @Test
    public void default_constructor_initializesList() {
        Organizer o = new Organizer();
        assertNotNull(o.getEventsCreated());
        assertTrue(o.getEventsCreated().isEmpty());
    }

    @Test
    public void constructor_setsDeviceId() {
        Organizer o = new Organizer("dev-1");
        assertEquals("dev-1", o.getDeviceId());
    }

    @Test
    public void addEventToCreated_appends() {
        Organizer o = new Organizer();
        o.addEventToCreated("e1");
        List<String> events = o.getEventsCreated();
        assertEquals(1, events.size());
        assertEquals("e1", events.get(0));
    }

    @Test
    public void setEventsCreated_replacesList() {
        Organizer o = new Organizer();
        ArrayList<String> newList = new ArrayList<>();
        newList.add("a");
        newList.add("b");
        o.setEventsCreated(newList);
        assertSame(newList, o.getEventsCreated());
        assertEquals(2, o.getEventsCreated().size());
    }
}

