package com.example.fairdraw.ServiceUtility;

import android.content.Context;

public final class GatePrefs {
    private static final String FILE = "gate_prefs";
    private static final String KEY_EXISTS = "user_exists";

    private GatePrefs() {}

    public static boolean getKnownExists(Context ctx) {
        return ctx.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .getBoolean(KEY_EXISTS, false);
    }

    public static void setKnownExists(Context ctx, boolean exists) {
        ctx.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_EXISTS, exists).apply();
    }
}
