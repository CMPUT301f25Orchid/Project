package com.example.fairdraw;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fairdraw.ServiceUtility.DevicePrefsManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented tests for {@link DevicePrefsManager}.
 * These tests verify that clearing the device id and cached account data
 * properly removes values from SharedPreferences and generates new ids.
 */
@RunWith(AndroidJUnit4.class)
public class DevicePrefsManagerInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Clear any existing preferences to start fresh for each test
        SharedPreferences sp = context.getSharedPreferences(
                DevicePrefsManager.PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
        // Reset the in-memory cache by clearing via the manager
        DevicePrefsManager.clearDeviceId(context);
    }

    /**
     * Test that clearing the device id causes getDeviceId to generate a new id.
     */
    @Test
    public void testClearDeviceId_generatesNewId() {
        // Get the original device id
        String originalId = DevicePrefsManager.getDeviceId(context);
        assertNotNull("Original device id should not be null", originalId);

        // Clear the device id
        DevicePrefsManager.clearDeviceId(context);

        // Get a new device id
        String newId = DevicePrefsManager.getDeviceId(context);
        assertNotNull("New device id should not be null", newId);

        // The new id should be different from the original
        assertNotEquals("New device id should be different from original", originalId, newId);
    }

    /**
     * Test that clearCachedAccountData removes all relevant SharedPreferences keys
     * and that a new device id is generated after clearing.
     */
    @Test
    public void testClearCachedAccountData_removesKeys() {
        // Ensure a device id exists
        String originalId = DevicePrefsManager.getDeviceId(context);
        assertNotNull("Device id should be generated", originalId);

        // Write some dummy values for FCM token and profile pic URI
        SharedPreferences sp = context.getSharedPreferences(
                DevicePrefsManager.PREF_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(DevicePrefsManager.KEY_FCM_TOKEN, "test-fcm-token")
                .putString(DevicePrefsManager.KEY_PROFILE_PIC_URI, "test-profile-pic-uri")
                .apply();

        // Verify the values were written
        assertTrue("Device id key should exist", sp.contains(DevicePrefsManager.DEVICE_ID_KEY));
        assertTrue("FCM token key should exist", sp.contains(DevicePrefsManager.KEY_FCM_TOKEN));
        assertTrue("Profile pic URI key should exist", sp.contains(DevicePrefsManager.KEY_PROFILE_PIC_URI));

        // Clear all cached account data
        DevicePrefsManager.clearCachedAccountData(context);

        // Re-get the SharedPreferences after clearing
        SharedPreferences spAfter = context.getSharedPreferences(
                DevicePrefsManager.PREF_NAME, Context.MODE_PRIVATE);

        // Verify all keys are removed
        assertFalse("Device id key should be removed", spAfter.contains(DevicePrefsManager.DEVICE_ID_KEY));
        assertFalse("FCM token key should be removed", spAfter.contains(DevicePrefsManager.KEY_FCM_TOKEN));
        assertFalse("Profile pic URI key should be removed", spAfter.contains(DevicePrefsManager.KEY_PROFILE_PIC_URI));

        // Verify calling getDeviceId() after clearing returns a new non-null id
        String newId = DevicePrefsManager.getDeviceId(context);
        assertNotNull("New device id should not be null after clearing", newId);
        assertNotEquals("New device id should be different from original after clearing", originalId, newId);
    }
}
