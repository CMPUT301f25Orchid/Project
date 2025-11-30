package com.example.fairdraw.Models;

import org.junit.Test;

import static org.junit.Assert.*;

public class AdminTest {

    @Test
    public void constructor_setsDeviceId() {
        Admin a = new Admin("device-123");
        assertEquals("device-123", a.getDeviceId());
    }

    @Test
    public void defaultConstructor_deviceIdIsNull() {
        Admin a = new Admin();
        assertNull(a.getDeviceId());
    }

    @Test
    public void setDeviceId_allowsNull_and_updates() {
        Admin a = new Admin();
        a.setDeviceId("d1");
        assertEquals("d1", a.getDeviceId());
        a.setDeviceId(null);
        assertNull(a.getDeviceId());
    }
}
