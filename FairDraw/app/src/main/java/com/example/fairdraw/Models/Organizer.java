package com.example.fairdraw.Models;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an organizer in the FairDraw application.
 * This class represents the model of an organizer (only the unique things to an organizer)
 * and is linked to a user (containing basic user information) by the device ID.
 * This class implements Serializable for Android intent compatibility and Firestore serialization.
 */
public class Organizer implements Serializable {
    /** The unique device ID linking this organizer to a user */
    private String deviceId;
    /** List of event IDs created by this organizer */
    private List<String> eventsCreated = new ArrayList<>();

    /**
     * Constructs a new Organizer with the specified device ID.
     * 
     * @param deviceId The unique device ID for this organizer
     */
    public Organizer(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Organizer() {
        // Required for Firestore deserialization
    }

    /**
     * Gets the device ID.
     * @return The device ID linking this organizer to a user
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device ID.
     * @param deviceId The device ID linking this organizer to a user
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the list of events created by this organizer.
     * @return List of event IDs created by this organizer
     */
    public List<String> getEventsCreated() {
        return eventsCreated;
    }

    /**
     * Sets the list of events created by this organizer.
     * @param eventsCreated List of event IDs created by this organizer
     */
    public void setEventsCreated(ArrayList<String> eventsCreated) {
        this.eventsCreated = eventsCreated;
    }

    /**
     * Adds an event to the list of events created by this organizer.
     * @param eventId The ID of the event to add
     */
    public void addEventToCreated(String eventId) {
        eventsCreated.add(eventId);
    }
}
