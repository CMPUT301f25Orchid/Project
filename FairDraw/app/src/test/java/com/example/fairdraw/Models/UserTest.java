package com.example.fairdraw.Models;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void defaultConstructor_initializesRoles() {
        User u = new User();
        assertNotNull(u.getRoles());
        assertTrue(u.getRoles().contains("entrant"));
        assertTrue(u.getRoles().contains("organizer"));
    }

    @Test
    public void parameterizedConstructor_setsFieldsAndRoles() {
        User u = new User("Alice", "a@x.com", "123", "dev-1", "tkn");
        assertEquals("Alice", u.getName());
        assertEquals("a@x.com", u.getEmail());
        assertEquals("123", u.getPhoneNum());
        assertEquals("dev-1", u.getDeviceId());
        assertEquals("tkn", u.getFcmToken());
        assertNotNull(u.getRoles());
        assertTrue(u.getRoles().contains("entrant"));
    }

    @Test
    public void setRoles_replacesList_and_allowsNull() {
        User u = new User();
        ArrayList<String> newRoles = new ArrayList<>();
        newRoles.add("admin");
        u.setRoles(newRoles);
        assertSame(newRoles, u.getRoles());

        u.setRoles(null);
        assertNull(u.getRoles());
    }

    @Test
    public void setters_updateFields() {
        User u = new User();
        u.setName("B");
        u.setEmail("b@x.com");
        u.setPhoneNum("555");
        u.setDeviceId("dev-2");
        u.setFcmToken("ft");
        assertEquals("B", u.getName());
        assertEquals("b@x.com", u.getEmail());
        assertEquals("555", u.getPhoneNum());
        assertEquals("dev-2", u.getDeviceId());
        assertEquals("ft", u.getFcmToken());
    }
}
