package com.example.fairdraw.Models;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the model of an Organizer(only the unique things to an Organizer)
 * and is linked to a user(containing basic user information) by the deviceid
 * */
public class Organizer implements Serializable {
    private String deviceId;
    private List<String> eventsCreated = new ArrayList<>();

    public Organizer(String deviceId) {
        this.deviceId = deviceId;
    }

    public Organizer() {
        // Required for Firestore deserialization
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<String> getEventsCreated() {
        return eventsCreated;
    }

    public void setEventsCreated(ArrayList<String> eventsCreated) {
        this.eventsCreated = eventsCreated;
    }

    public void addEventToCreated(String eventId) {
        eventsCreated.add(eventId);
    }
}
