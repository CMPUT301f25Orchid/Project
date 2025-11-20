package com.example.fairdraw.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing an organizer. An Organizer stores organizer-specific information
 * such as the list of events they have created and is linked to a {@link User} via deviceId.
 */
public class Organizer implements Serializable {
    private String deviceId;
    private List<String> eventsCreated = new ArrayList<>();

    /**
     * Constructs an Organizer linked to the provided device id.
     *
     * @param deviceId device identifier that links this organizer to a {@link User}
     */
    public Organizer(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * No-argument constructor required for Firestore deserialization.
     */
    public Organizer() {
        // Required for Firestore deserialization
    }

    /**
     * Returns the device identifier that links this organizer to a {@link User}.
     *
     * @return device id string, or null if not set
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device identifier for this organizer.
     *
     * @param deviceId unique device id to link to a {@link User}
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the list of event ids created by this organizer.
     *
     * @return list of event ids
     */
    public List<String> getEventsCreated() {
        return eventsCreated;
    }

    /**
     * Replaces the list of events created. Useful for deserialization.
     *
     * @param eventsCreated list of event ids
     */
    public void setEventsCreated(ArrayList<String> eventsCreated) {
        this.eventsCreated = eventsCreated;
    }

    /**
     * Adds an event id to the organizer's created events list.
     *
     * @param eventId id of the event to add
     */
    public void addEventToCreated(String eventId) {
        eventsCreated.add(eventId);
    }
}
