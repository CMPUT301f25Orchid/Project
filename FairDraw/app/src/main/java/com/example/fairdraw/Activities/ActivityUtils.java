package com.example.fairdraw.Activities;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;

/**
 * Small helper utilities for Activities.
 */
public class ActivityUtils {
    private static final String TAG = "ActivityUtils";

    /**
     * Ensure a user exists for the current deviceId. If not, redirect to SplashActivity
     * clearing the activity stack to avoid navigation back into a corrupted state.
     *
     * This method is asynchronous: it will return immediately and might start the
     * SplashActivity later on the UI thread if the user doesn't exist.
     */
    public static void ensureUserExistsOrRedirect(Activity activity) {
        if (activity == null) return;

        String deviceId = DevicePrefsManager.getDeviceId(activity);
        if (deviceId == null || deviceId.isEmpty()) {
            // No deviceId — treat as not found and redirect
            redirectToSplash(activity);
            return;
        }

        UserDB.userExists(deviceId, (exists, e) -> {
            if (e != null) {
                Log.w(TAG, "Error checking user existence", e);
                // Treat errors as non-fatal: do not redirect in case of transient errors.
                return;
            }

            if (!exists) {
                redirectToSplash(activity);
            }
        });
    }

    private static void redirectToSplash(Activity activity) {
        activity.runOnUiThread(() -> {
            try {
                Intent intent = new Intent(activity, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to redirect to splash activity", ex);
            }
        });
    }
}

