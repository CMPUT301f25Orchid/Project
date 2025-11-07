package com.example.fairdraw.ServiceUtility;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Manages device-specific preferences using SharedPreferences.
 * This utility class provides thread-safe access to a unique device ID that persists across app sessions.
 * The device ID is generated once and cached for subsequent use.
 * This class is final and cannot be instantiated.
 */
public final class DevicePrefsManager {
    /** Name of the SharedPreferences file */
    private static final String PREF_NAME = "device_prefs";
    /** Key for storing the device ID in SharedPreferences */
    private static final String DEVICE_ID_KEY = "device_id";
    /** Cached device ID to avoid repeated SharedPreferences reads */
    private static volatile String uniqueId;

    /**
     * Private constructor to prevent instantiation.
     */
    private DevicePrefsManager() {}

    /**
     * Gets or creates a unique device ID for this device.
     * The ID is generated once using UUID, stored in SharedPreferences, and cached in memory.
     * This method is thread-safe using double-checked locking.
     * 
     * @param context The application context
     * @return A unique device ID string (UUID format)
     */
    public static String getDeviceId(Context context) {
        if (uniqueId != null) return uniqueId;

        synchronized (DevicePrefsManager.class) {
            if (uniqueId != null) return uniqueId;

            Context appCtx = context.getApplicationContext();
            SharedPreferences sp = appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String cached = sp.getString(DEVICE_ID_KEY, null);

            if (cached == null || cached.trim().isEmpty()) {
                cached = UUID.randomUUID().toString();
                sp.edit().putString(DEVICE_ID_KEY, cached).apply();
            }

            uniqueId = cached;
            return uniqueId;
        }
    }
}
