package com.example.fairdraw.ServiceUtility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class GeoService {
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final Context appContext;
    private final FusedLocationProviderClient fusedLocationClient;

    public interface GeoCallback {
        void onLocationResult(Location location);
        void onLocationError(String message);
    }

    public GeoService(Context context) {
        appContext = context.getApplicationContext();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext);
    }

    /**
     * Returns true if either fine or coarse location permission is granted.
     */
    public boolean hasLocationPermission() {
        int fine = ContextCompat.checkSelfPermission(appContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse = ContextCompat.checkSelfPermission(appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return fine == PackageManager.PERMISSION_GRANTED
                || coarse == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request location permission from an Activity.
     * Call this from your Activity when you detect that permission is missing.
     */
    public void requestLocationPermission(@NonNull Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    /**
     * Get the last known location once.
     *
     * - If permission is missing, callback.onLocationError(...) is called immediately.
     * - If Google Play Services cannot find a last known location, location will be null.
     */
    public void getLastKnownLocation(@NonNull final GeoCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationError("Location permission not granted.");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            callback.onLocationResult(location);
                        } else {
                            callback.onLocationError("Last known location is null.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onLocationError("Failed to get location: " + e.getMessage());
                    }
                });
    }
}
