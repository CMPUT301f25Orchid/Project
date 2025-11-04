package com.example.fairdraw;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Test
    public void testPushNotificationToUser() throws Exception {
        // Ensure the entrant doc exists (mirrors your other tests)
        CountDownLatch addLatch = new CountDownLatch(1);
        EntrantDB.addEntrant(testEntrant, success -> addLatch.countDown());
        assertTrue("addEntrant timed out", addLatch.await(5, TimeUnit.SECONDS));

        // Build a tiny notification (set fields directly to avoid ctor mismatch)
        EntrantNotification n = new EntrantNotification();
        n.type = NotificationType.WIN.name();
        n.eventId = "evt_push_1";
        n.title = "Sample Event";   // short header you store in DB
        n.read = false;

        // Push the notification
        CountDownLatch pushLatch = new CountDownLatch(1);
        EntrantDB.pushNotificationToUser(testDeviceId, n, (ok, e) -> {
            assertTrue("push failed: " + (e != null ? e.getMessage() : ""), ok);
            pushLatch.countDown();
        });
        assertTrue("push timed out", pushLatch.await(7, TimeUnit.SECONDS));

        // Read back the entrant doc and check the notifications array
        CountDownLatch readLatch = new CountDownLatch(1);
        final List<Map<String, Object>>[] holder = new List[1];

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("entrants").document(testDeviceId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap != null && snap.exists()) {
                        holder[0] = (List<Map<String, Object>>) snap.get("notifications");
                    }
                    readLatch.countDown();
                })
                .addOnFailureListener(e -> readLatch.countDown());

        assertTrue("read timed out", readLatch.await(7, TimeUnit.SECONDS));
        assertNotNull("notifications should exist", holder[0]);
        assertEquals("should have exactly 1 notification", 1, holder[0].size());

        Map<String, Object> first = holder[0].get(0);
        assertEquals("WIN", first.get("type"));
        assertEquals("evt_push_1", first.get("eventId"));
        assertEquals("Sample Event", first.get("title"));
        assertEquals(false, first.get("read"));
    }

}
