package com.example.fairdraw;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class EntrantDBTest {

    private Entrant testEntrant;
    private final String testDeviceId = "test_entrant_device_id";

    @Before
    public void setUp() throws Exception {
        testEntrant = new Entrant(testDeviceId);
        // Clean up before each test
        deleteTestEntrant();
    }

    @After
    public void tearDown() throws Exception {
        // Clean up after each test
        deleteTestEntrant();
    }

    private void deleteTestEntrant() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        EntrantDB.deleteEntrant(testDeviceId, success -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testAddAndGetEntrant() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        EntrantDB.addEntrant(testEntrant, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addEntrant timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        EntrantDB.getEntrant(testDeviceId, entrant -> {
            assertNotNull(entrant);
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getEntrant timed out");
        }
    }

    @Test
    public void testUpdateEntrant() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        EntrantDB.addEntrant(testEntrant, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addEntrant timed out");
        }

        ArrayList<String> events = new ArrayList<>();
        events.add("event1");
        testEntrant.setEventHistory(events);

        HashMap<String, Boolean> prefs = new HashMap<>();
        prefs.put("email", true);
        testEntrant.setNotificationPrefs(prefs);

        CountDownLatch updateLatch = new CountDownLatch(1);
        EntrantDB.updateEntrant(testEntrant, success -> {
            assertTrue(success);
            updateLatch.countDown();
        });
        if (!updateLatch.await(5, TimeUnit.SECONDS)) {
            fail("updateEntrant timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        EntrantDB.getEntrant(testDeviceId, entrant -> {
            assertNotNull(entrant);
            assertEquals(1, entrant.getEventHistory().size());
            assertEquals("event1", entrant.getEventHistory().get(0));
            assertTrue(entrant.getNotificationPrefs().get("email"));
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getEntrant timed out");
        }
    }

    @Test
    public void testDeleteEntrant() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        EntrantDB.addEntrant(testEntrant, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addEntrant timed out");
        }

        CountDownLatch deleteLatch = new CountDownLatch(1);
        EntrantDB.deleteEntrant(testDeviceId, success -> {
            assertTrue(success);
            deleteLatch.countDown();
        });
        if (!deleteLatch.await(5, TimeUnit.SECONDS)) {
            fail("deleteEntrant timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        EntrantDB.getEntrant(testDeviceId, entrant -> {
            assertNull(entrant);
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getEntrant timed out");
        }
    }
}
