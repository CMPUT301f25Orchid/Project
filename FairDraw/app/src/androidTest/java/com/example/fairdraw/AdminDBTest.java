package com.example.fairdraw;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class AdminDBTest {

    private Admin testAdmin;
    private final String testDeviceId = "test_admin_device_id";

    @Before
    public void setUp() throws Exception {
        testAdmin = new Admin(testDeviceId);
        // Clean up before each test
        deleteTestAdmin();
    }

    @After
    public void tearDown() throws Exception {
        // Clean up after each test
        deleteTestAdmin();
    }

    private void deleteTestAdmin() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AdminDB.deleteAdmin(testDeviceId, success -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testAddAndGetAdmin() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        AdminDB.addAdmin(testAdmin, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addAdmin timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        AdminDB.getAdmin(testDeviceId, admin -> {
            assertNotNull(admin);
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getAdmin timed out");
        }
    }

    @Test
    public void testDeleteAdmin() throws InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        AdminDB.addAdmin(testAdmin, success -> {
            assertTrue(success);
            addLatch.countDown();
        });
        if (!addLatch.await(5, TimeUnit.SECONDS)) {
            fail("addAdmin timed out");
        }

        CountDownLatch deleteLatch = new CountDownLatch(1);
        AdminDB.deleteAdmin(testDeviceId, success -> {
            assertTrue(success);
            deleteLatch.countDown();
        });
        if (!deleteLatch.await(5, TimeUnit.SECONDS)) {
            fail("deleteAdmin timed out");
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        AdminDB.getAdmin(testDeviceId, admin -> {
            assertNull(admin);
            getLatch.countDown();
        });
        if (!getLatch.await(5, TimeUnit.SECONDS)) {
            fail("getAdmin timed out");
        }
    }
}
