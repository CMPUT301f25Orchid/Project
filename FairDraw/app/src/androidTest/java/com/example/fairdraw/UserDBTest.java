package com.example.fairdraw;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class UserDBTest {

    private User testUser;
    private final String testDeviceId = "test_device_id";

    @Before
    public void setUp() throws Exception {
        testUser = new User("Test User", "test@example.com", "1234567890", testDeviceId, "test_fcm_token", Collections.singletonList("USER"));
        // Clean up before each test
        deleteTestUser();
    }

    @After
    public void tearDown() throws Exception {
        // Clean up after each test
        deleteTestUser();
    }

    private void deleteTestUser() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        UserDB.deleteUser(testDeviceId, success -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testAddAndGetUser() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        UserDB.addUser(testUser, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addUser timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        UserDB.getUser(testDeviceId, user -> {
            assertNotNull(user);
            assertEquals(testUser.getName(), user.getName());
            assertEquals(testUser.getEmail(), user.getEmail());
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getUser timed out");
        }
    }

    @Test
    public void testUpdateUser() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        UserDB.addUser(testUser, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addUser timed out");
        }

        testUser.setName("Updated Test User");
        CountDownLatch updateLatch = new CountDownLatch(1);
        UserDB.updateUser(testUser, success -> {
            assertTrue(success);
            updateLatch.countDown();
        });
        if (!updateLatch.await(5, TimeUnit.SECONDS)) {
            fail("updateUser timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        UserDB.getUser(testDeviceId, user -> {
            assertNotNull(user);
            assertEquals("Updated Test User", user.getName());
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getUser timed out");
        }
    }

    @Test
    public void testDeleteUser() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        UserDB.addUser(testUser, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addUser timed out");
        }

        CountDownLatch deleteLatch = new CountDownLatch(1);
        UserDB.deleteUser(testDeviceId, success -> {
            assertTrue(success);
            deleteLatch.countDown();
        });
        if (!deleteLatch.await(5, TimeUnit.SECONDS)) {
            fail("deleteUser timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        UserDB.getUser(testDeviceId, user -> {
            assertNull(user);
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getUser timed out");
        }
    }
}
