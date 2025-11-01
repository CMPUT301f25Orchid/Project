package com.example.fairdraw;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class OrganizerDBTest {

    private Organizer testOrganizer;
    private final String testDeviceId = "test_organizer_device_id";

    @Before
    public void setUp() throws Exception {
        testOrganizer = new Organizer(testDeviceId);
        // Clean up before each test
        deleteTestOrganizer();
    }

    @After
    public void tearDown() throws Exception {
        // Clean up after each test
        deleteTestOrganizer();
    }

    private void deleteTestOrganizer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        OrganizerDB.deleteOrganizer(testDeviceId, success -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testAddAndGetOrganizer() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        OrganizerDB.addOrganizer(testOrganizer, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addOrganizer timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        OrganizerDB.getOrganizer(testDeviceId, organizer -> {
            assertNotNull(organizer);
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getOrganizer timed out");
        }
    }

    @Test
    public void testUpdateOrganizer() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        OrganizerDB.addOrganizer(testOrganizer, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addOrganizer timed out");
        }

        ArrayList<String> events = new ArrayList<>();
        events.add("event1");
        testOrganizer.setEventsCreated(events);

        CountDownLatch updateLatch = new CountDownLatch(1);
        OrganizerDB.updateOrganizer(testOrganizer, success -> {
            assertTrue(success);
            updateLatch.countDown();
        });
        if (!updateLatch.await(5, TimeUnit.SECONDS)) {
            fail("updateOrganizer timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        OrganizerDB.getOrganizer(testDeviceId, organizer -> {
            assertNotNull(organizer);
            assertEquals(1, organizer.getEventsCreated().size());
            assertEquals("event1", organizer.getEventsCreated().get(0));
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getOrganizer timed out");
        }
    }

    @Test
    public void testDeleteOrganizer() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        OrganizerDB.addOrganizer(testOrganizer, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addOrganizer timed out");
        }

        CountDownLatch deleteLatch = new CountDownLatch(1);
        OrganizerDB.deleteOrganizer(testDeviceId, success -> {
            assertTrue(success);
            deleteLatch.countDown();
        });
        if (!deleteLatch.await(5, TimeUnit.SECONDS)) {
            fail("deleteOrganizer timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        OrganizerDB.getOrganizer(testDeviceId, organizer -> {
            assertNull(organizer);
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getOrganizer timed out");
        }
    }
}
