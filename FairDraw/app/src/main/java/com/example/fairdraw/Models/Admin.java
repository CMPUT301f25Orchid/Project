package com.example.fairdraw.Models;

import java.io.Serializable;

/**
 * Model representing an application admin. Instances store admin-specific information
 * that supplements the general {@link User} data (linked by deviceId).
 * <p>
 * This class is serializable and designed to be stored and retrieved from Firestore.
 */
public class Admin implements Serializable {
    private String deviceId;

    /**
     * Constructs an Admin with the given device identifier.
     *
     * @param deviceId unique identifier for the admin's device (links to {@link User})
     */
    public Admin(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * No-argument constructor required for Firestore (deserialization).
     */
    public Admin() {
        // Required for Firestore deserialization
    }

    /**
     * Returns the device identifier that links this admin to a {@link User}.
     *
     * @return device id string, or null if not set
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device identifier for this admin.
     *
     * @param deviceId unique device id to link to a {@link User}
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
