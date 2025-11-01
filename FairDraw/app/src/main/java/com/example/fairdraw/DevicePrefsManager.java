package com.example.fairdraw;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.UUID;

/**
 * This class is in charge of communicating with the device's storage
 * */
public class DevicePrefsManager {
    private static final String PREF_NAME = "device_prefs";
    private static final String DEVICE_ID_KEY = "device_id";
    private static String uniqueId = null;

    public synchronized static String getDeviceId(Context context) {
        if (uniqueId == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_NAME, Context.MODE_PRIVATE);
            uniqueId = sharedPrefs.getString(DEVICE_ID_KEY, null);

            if (uniqueId == null) {
                // Generate new ID and save it
                uniqueId = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(DEVICE_ID_KEY, uniqueId);
                editor.apply();
            }
        }
        return uniqueId;
    }

}
