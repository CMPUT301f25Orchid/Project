package com.example.fairdraw;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Instrumented tests for the {@link UserDB} class.
 * These tests cover the basic CRUD operations for users in Firestore.
 */
@RunWith(AndroidJUnit4.class)
public class UserDBTest {

    private User testUser;
    private final String testDeviceId = "test_device_id";

    /**
     * Sets up the test environment before each test.
     * This involves creating a test User object and ensuring the test user does not exist in the database.
     * @throws Exception if setup fails.
     */
    @Before
    public void setUp() throws Exception {
        testUser = new User("Test User", "test@example.com", "1234567890", testDeviceId, "test_fcm_token");
        // Clean up before each test
        deleteTestUser();
    }

    /**
     * Cleans up the test environment after each test.
     * This involves deleting the test user from the database.
     * @throws Exception if cleanup fails.
     */
    @After
    public void tearDown() throws Exception {
        // Clean up after each test
        deleteTestUser();
    }

    /**
     * Helper method to delete the test user from the database.
     * Uses a CountDownLatch to wait for the asynchronous Firestore operation to complete.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    private void deleteTestUser() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        UserDB.deleteUser(testDeviceId, (ok, e) -> latch.countDown());
        if (!latch.await(5, TimeUnit.SECONDS)) {
            fail("deleteTestUser timed out");
        }
    }

    /**
     * Tests adding a user to the database and then retrieving it.
     * Verifies that the retrieved user's data matches the data that was added.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Test
    public void testAddAndGetUser() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        UserDB.upsertUser(testUser, (success, e) -> {
            assertTrue(success);
            assertNull(e);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("upsertUser timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        UserDB.getUserOrNull(testDeviceId, (user, e) -> {
            assertNotNull(user);
            assertNull(e);
            assertEquals(testUser.getName(), user.getName());
            assertEquals(testUser.getEmail(), user.getEmail());
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getUserOrNull timed out");
        }
    }

    /**
     * Tests updating an existing user's information in the database.
     * Verifies that the changes are correctly persisted.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Test
    public void testUpdateUser() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        UserDB.upsertUser(testUser, (success, e) -> {
            assertTrue(success);
            assertNull(e);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("upsertUser timed out");
        }

        testUser.setName("Updated Test User");
        CountDownLatch updateLatch = new CountDownLatch(1);
        UserDB.upsertUser(testUser, (success, e) -> {
            assertTrue(success);
            assertNull(e);
            updateLatch.countDown();
        });
        if (!updateLatch.await(5, TimeUnit.SECONDS)) {
            fail("updateUser timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        UserDB.getUserOrNull(testDeviceId, (user, e) -> {
            assertNotNull(user);
            assertNull(e);
            assertEquals("Updated Test User", user.getName());
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getUserOrNull timed out");
        }
    }

    /**
     * Tests deleting a user from the database.
     * Verifies that the user can no longer be retrieved after deletion.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Test
    public void testDeleteUser() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        UserDB.upsertUser(testUser, (success, e) -> {
            assertTrue(success);
            assertNull(e);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("upsertUser timed out");
        }

        CountDownLatch deleteLatch = new CountDownLatch(1);
        UserDB.deleteUser(testDeviceId, (success, e) -> {
            assertTrue(success);
            assertNull(e);
            deleteLatch.countDown();
        });
        if (!deleteLatch.await(5, TimeUnit.SECONDS)) {
            fail("deleteUser timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        UserDB.getUserOrNull(testDeviceId, (user, e) -> {
            assertNull(user);
            assertNull(e);
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getUserOrNull timed out");
        }
    }
}
