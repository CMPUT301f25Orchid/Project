package com.example.fairdraw.ServiceUtility;

import android.content.Context;

/**
 * Manages gate/onboarding preferences using SharedPreferences.
 * This utility class tracks whether a user has completed initial setup or onboarding.
 * This class is final and cannot be instantiated.
 */
public final class GatePrefs {
    /** Name of the SharedPreferences file */
    private static final String FILE = "gate_prefs";
    /** Key for tracking whether the user exists/has completed onboarding */
    private static final String KEY_EXISTS = "user_exists";

    /**
     * Private constructor to prevent instantiation.
     */
    private GatePrefs() {}

    /**
     * Checks whether the user is known to exist (has completed onboarding).
     * 
     * @param ctx The application context
     * @return True if the user has completed onboarding, false otherwise
     */
    public static boolean getKnownExists(Context ctx) {
        return ctx.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .getBoolean(KEY_EXISTS, false);
    }

    /**
     * Sets whether the user is known to exist (has completed onboarding).
     * 
     * @param ctx The application context
     * @param exists True to mark user as having completed onboarding, false otherwise
     */
    public static void setKnownExists(Context ctx, boolean exists) {
        ctx.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_EXISTS, exists).apply();
    }
}
