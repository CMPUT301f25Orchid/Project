package com.example.fairdraw;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Models.Event;

@RunWith(AndroidJUnit4.class)
public class EventDBTest {

    private Event testEvent;
    private final String testEventId = UUID.randomUUID().toString();

    @Before
    public void setUp() throws Exception {
        testEvent = new Event("Test Event", "Test Description", 100, new Date(), new Date(), "Test Location", "Test Organizer", 0.0f, null, "test-qr");
        testEvent.setUuid(testEventId); // Ensure the test uses a consistent UUID
        // Clean up before each test
        deleteTestEvent();
    }

    @After
    public void tearDown() throws Exception {
        // Clean up after each test
        deleteTestEvent();
    }

    private void deleteTestEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        EventDB.deleteEvent(testEventId, success -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testAddAndGetEvent() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        EventDB.addEvent(testEvent, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addEvent timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        EventDB.getEvent(testEventId, event -> {
            assertNotNull(event);
            assertEquals(testEvent.getTitle(), event.getTitle());
            assertEquals(testEvent.getDescription(), event.getDescription());
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getEvent timed out");
        }
    }

    @Test
    public void testUpdateEvent() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        EventDB.addEvent(testEvent, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addEvent timed out");
        }

        testEvent.setTitle("Updated Test Event");
        CountDownLatch updateLatch = new CountDownLatch(1);
        EventDB.updateEvent(testEvent, success -> {
            assertTrue(success);
            updateLatch.countDown();
        });
        if (!updateLatch.await(5, TimeUnit.SECONDS)) {
            fail("updateEvent timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        EventDB.getEvent(testEventId, event -> {
            assertNotNull(event);
            assertEquals("Updated Test Event", event.getTitle());
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getEvent timed out");
        }
    }

    @Test
    public void testDeleteEvent() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        EventDB.addEvent(testEvent, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addEvent timed out");
        }

        CountDownLatch deleteLatch = new CountDownLatch(1);
        EventDB.deleteEvent(testEventId, success -> {
            assertTrue(success);
            deleteLatch.countDown();
        });
        if (!deleteLatch.await(5, TimeUnit.SECONDS)) {
            fail("deleteEvent timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        EventDB.getEvent(testEventId, event -> {
            assertNull(event);
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getEvent timed out");
        }
    }
}
