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
    public void setDeviceId_allowsNull() {
        Admin a = new Admin();
        a.setDeviceId(null);
        assertNull(a.getDeviceId());
    }
}

