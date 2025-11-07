package com.example.fairdraw.Models;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an admin in the FairDraw application.
 * This class represents the model of an admin (only the unique things to an admin)
 * and is linked to a user (containing basic user information) by the device ID.
 * This class implements Serializable for Android intent compatibility and Firestore serialization.
 */
public class Admin implements Serializable {
    /** The unique device ID linking this admin to a user */
    private String deviceId;

    /**
     * Constructs a new Admin with the specified device ID.
     * 
     * @param deviceId The unique device ID for this admin
     */
    public Admin(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Admin() {
        // Required for Firestore deserialization
    }

    /**
     * Gets the device ID.
     * @return The device ID linking this admin to a user
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device ID.
     * @param deviceId The device ID linking this admin to a user
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
