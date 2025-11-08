package com.example.fairdraw.ServiceUtility;

import android.content.Context;

/**
 * Simple preferences helper to store whether a user record "exists" on the server.
 *
 * <p>This wraps a boolean flag in a private SharedPreferences file and provides
 * a clear API for getting and setting the value.
 */
public final class GatePrefs {
    private static final String FILE = "gate_prefs";
    private static final String KEY_EXISTS = "user_exists";

    private GatePrefs() {}

    /**
     * Returns whether the local device believes a corresponding user exists on the server.
     *
     * @param ctx any Context; the application context will be used
     * @return true if a user record is known to exist; false otherwise
     */
    public static boolean getKnownExists(Context ctx) {
        return ctx.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .getBoolean(KEY_EXISTS, false);
    }

    /**
     * Persist the flag indicating whether a user record exists on the server.
     *
     * @param ctx any Context; the application context will be used
     * @param exists true if the user record exists, false otherwise
     */
    public static void setKnownExists(Context ctx, boolean exists) {
        ctx.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_EXISTS, exists).apply();
    }
}
