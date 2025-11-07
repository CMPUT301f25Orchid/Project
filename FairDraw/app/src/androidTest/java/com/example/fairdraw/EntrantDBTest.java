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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.Models.Entrant;
import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.Others.NotificationType;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    /**
     * Tests the pushNotificationToUser method in EntrantDB.
     *
     * @throws InterruptedException
     */
    @Test
    public void testPushNotificationToUser() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        EntrantNotification firstNotification =
                new EntrantNotification(NotificationType.WIN, "evt1", "Test Event");
        EntrantNotification secondNotification =
                new EntrantNotification(NotificationType.LOSE, "evt2", "Another Event");

        EntrantDB.addEntrant(testEntrant, addOk -> {
            assertTrue("Failed to add entrant", addOk);

            EntrantDB.pushNotificationToUser(testDeviceId, firstNotification, (push1Ok, e1) -> {
                assertTrue("First notification push failed", push1Ok);

                EntrantDB.pushNotificationToUser(testDeviceId, secondNotification, (push2Ok, e2) -> {
                    assertTrue("Second notification push failed", push2Ok);

                    FirebaseFirestore.getInstance().collection("entrants").document(testDeviceId).get()
                            .addOnSuccessListener(snap -> {
                                assertTrue("Entrant document not found", snap.exists());

                                Object notificationsObj = snap.get("notifications");
                                if (!(notificationsObj instanceof List)) {
                                    fail("Notifications field is not a List");
                                    return;
                                }

                                List<Map<String, Object>> notifs = (List<Map<String, Object>>) notificationsObj;
                                assertNotNull("Notifications field is missing", notifs);
                                assertEquals("Should be 2 notifications", 2, notifs.size());

                                assertEquals("evt1", notifs.get(0).get("eventId"));

                                assertEquals("LOSE", notifs.get(1).get("type"));
                                assertEquals("evt2", notifs.get(1).get("eventId"));
                                assertEquals("Another Event", notifs.get(1).get("title"));
                                assertFalse("New notification should be unread", (Boolean) notifs.get(1).get("read"));

                                latch.countDown();
                            })
                            .addOnFailureListener(e -> fail("Firestore read failed: " + e.getMessage()));
                });
            });
        });
        assertTrue("Test timed out", latch.await(10, TimeUnit.SECONDS));
    }
}