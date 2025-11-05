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


    @Test
    public void testPushNotificationToUser() throws InterruptedException {
        // Ensure entrant doc exists
        CountDownLatch addLatch = new CountDownLatch(1);
        EntrantDB.addEntrant(testEntrant, ok -> {
            assertTrue("addEntrant failed", ok);
            addLatch.countDown();
        });
        assertTrue("addEntrant timed out", addLatch.await(5, TimeUnit.SECONDS));

        // Push one notification
        EntrantNotification n =
                new EntrantNotification(NotificationType.WIN, "evt1", "Test Event");

        CountDownLatch pushLatch = new CountDownLatch(1);
        EntrantDB.pushNotificationToUser(testDeviceId, n, (ok, e) -> {
            assertTrue("pushNotification failed: " + e, ok);
            pushLatch.countDown();
        });
        assertTrue("pushNotification timed out", pushLatch.await(5, TimeUnit.SECONDS));

        // Read back notifications array directly from Firestore
        CountDownLatch readLatch = new CountDownLatch(1);
        FirebaseFirestore.getInstance()
                .collection("entrants").document(testDeviceId)
                .get()
                .addOnSuccessListener((DocumentSnapshot snap) -> {
                    assertTrue("entrant doc missing", snap.exists());
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> arr = (List<Map<String, Object>>) snap.get("notifications");
                    assertNotNull("notifications field missing", arr);
                    assertFalse("notifications array empty", arr.isEmpty());

                    Map<String, Object> last = arr.get(arr.size() - 1);
                    assertEquals("WIN", String.valueOf(last.get("type")));
                    assertEquals("evt1", String.valueOf(last.get("eventId")));
                    assertEquals("Test Event", String.valueOf(last.get("title")));
                    Object r = last.get("read");
                    assertTrue("read should be false or absent", r == null || Boolean.FALSE.equals(r));

                    readLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("read back failed: " + e);
                    readLatch.countDown();
                });

        assertTrue("read back timed out", readLatch.await(5, TimeUnit.SECONDS));
    }
}