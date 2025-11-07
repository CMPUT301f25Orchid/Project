package com.example.fairdraw.ServiceUtility;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public final class DevicePrefsManager {
    private static final String PREF_NAME = "device_prefs";
    private static final String DEVICE_ID_KEY = "device_id";
    private static volatile String uniqueId;

    private DevicePrefsManager() {}

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
