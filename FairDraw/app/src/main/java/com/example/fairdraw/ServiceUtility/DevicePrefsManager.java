package com.example.fairdraw.ServiceUtility;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Helper to manage a stable device identifier stored in SharedPreferences.
 *
 * <p>The device id is generated once and persisted to application-scoped
 * preferences. Subsequent calls return the same id. This is commonly used
 * as a lightweight, app-local unique identifier for entrants when a user
 * account is not available.
 */
public final class DevicePrefsManager {
    /** SharedPreferences file name for device-related preferences. */
    static final String PREF_NAME = "device_prefs";
    /** Key for the stored device id. */
    static final String DEVICE_ID_KEY = "device_id";
    /** Key for the cached FCM token. */
    static final String KEY_FCM_TOKEN = "fcm_token";
    /** Key for the cached profile picture URI. */
    static final String KEY_PROFILE_PIC_URI = "profile_pic_uri";

    private static volatile String uniqueId;

    private DevicePrefsManager() {}

    /**
     * Returns a stable device id for this application. If none exists it will be
     * generated, stored, and returned.
     *
     * @param context any Context; the application context will be used internally
     * @return a non-null UUID string that uniquely identifies this installation
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

    /**
     * Clears the stored device id from SharedPreferences.
     * The next call to {@link #getDeviceId(Context)} will generate a new id.
     *
     * @param context any Context; the application context will be used internally
     */
    public static void clearDeviceId(Context context) {
        synchronized (DevicePrefsManager.class) {
            Context appCtx = context.getApplicationContext();
            SharedPreferences sp = appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sp.edit().remove(DEVICE_ID_KEY).apply();
            uniqueId = null;
        }
    }

    /**
     * Clears all cached account-related data from SharedPreferences.
     * This includes the device id, FCM token, and profile picture URI.
     * Should be called when a user deletes their own account to prevent
     * reuse of the same device id on re-signup.
     *
     * @param context any Context; the application context will be used internally
     */
    public static void clearCachedAccountData(Context context) {
        synchronized (DevicePrefsManager.class) {
            Context appCtx = context.getApplicationContext();
            SharedPreferences sp = appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sp.edit()
                    .remove(DEVICE_ID_KEY)
                    .remove(KEY_FCM_TOKEN)
                    .remove(KEY_PROFILE_PIC_URI)
                    .apply();
            uniqueId = null;
        }
    }
}
