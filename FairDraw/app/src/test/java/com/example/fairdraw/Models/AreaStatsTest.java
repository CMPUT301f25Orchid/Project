package com.example.fairdraw.Models;

import org.junit.Test;

import static org.junit.Assert.*;

public class AreaStatsTest {

    @Test
    public void constructor_setsLatLng_and_initialCountZero() {
        AreaStats a = new AreaStats(53.5232, -113.5263);
        assertEquals(53.5232, a.getLat(), 1e-9);
        assertEquals(-113.5263, a.getLng(), 1e-9);
        assertEquals(0, a.getCount());
    }

    @Test
    public void increment_increasesCount() {
        AreaStats a = new AreaStats(0.0, 0.0);
        assertEquals(0, a.getCount());
        a.increment();
        assertEquals(1, a.getCount());
        a.increment();
        assertEquals(2, a.getCount());
    }

    @Test
    public void getters_returnValues() {
        AreaStats a = new AreaStats(-10.5, 120.75);
        assertEquals(-10.5, a.getLat(), 1e-9);
        assertEquals(120.75, a.getLng(), 1e-9);
    }
}
