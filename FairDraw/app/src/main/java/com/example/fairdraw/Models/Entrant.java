package com.example.fairdraw.Models;

import android.provider.ContactsContract;

import com.example.fairdraw.Others.EntrantNotification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * Represents an entrant in the FairDraw application.
 * This class represents the model of an entrant (only the unique things to an entrant)
 * and is linked to a user (containing basic user information) by the device ID.
 * This class implements Serializable for Android intent compatibility and Firestore serialization.
 */
public class Entrant implements Serializable {
    /** The unique device ID linking this entrant to a user */
    private String deviceId;
    /** List of event IDs that the entrant has participated in */
    private ArrayList<String> eventHistory = new ArrayList<>();
    /** List of notifications received by the entrant */
    private ArrayList<EntrantNotification> notifications = new ArrayList<>();

    /** Notification preferences mapping notification type to enabled/disabled status */
    private HashMap<String, Boolean> notificationPrefs = new HashMap<>();

    /**
     * Constructs a new Entrant with the specified device ID.
     * 
     * @param deviceId The unique device ID for this entrant
     */
    public Entrant(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Entrant() {
        // Required for Firestore deserialization
    }

    /**
     * Gets the device ID.
     * @return The device ID linking this entrant to a user
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device ID.
     * @param deviceId The device ID linking this entrant to a user
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the event history.
     * @return List of event IDs the entrant has participated in
     */
    public List<String> getEventHistory() {
        return eventHistory;
    }

    /**
     * Sets the event history.
     * @param eventHistory List of event IDs the entrant has participated in
     */
    public void setEventHistory(ArrayList<String> eventHistory) {
        this.eventHistory = eventHistory;
    }


    /**
     * Gets the notification preferences.
     * @return HashMap mapping notification types to their enabled/disabled status
     */
    public HashMap<String, Boolean> getNotificationPrefs() {
        return notificationPrefs;
    }

    /**
     * Adds an event to the entrant's history.
     * @param eventId The ID of the event to add
     */
    public void addEventToHistory(String eventId) {
        eventHistory.add(eventId);
    }

    /**
     * Updates the notification preferences by merging new preferences.
     * @param newNotificationPrefs HashMap of new notification preferences to merge
     */
    public void setNotificationPrefs(HashMap<String, Boolean> newNotificationPrefs) {
        // Get the enumeration of keys from the new dictionary
        for (String key : newNotificationPrefs.keySet()) {
            notificationPrefs.put(key, newNotificationPrefs.get(key));
        }
    }
    
    /**
     * Adds a notification to the entrant's notification list.
     * @param notification The notification to add
     */
    public void addNotification(EntrantNotification notification) {
        notifications.add(notification);
    }
    
    /**
     * Removes a notification from the entrant's notification list.
     * @param notification The notification to remove
     */
    public void removeNotification(EntrantNotification notification) {
        notifications.remove(notification);
    }
    
    /**
     * Gets the list of notifications.
     * @return ArrayList of EntrantNotification objects
     */
    public ArrayList<EntrantNotification> getNotifications() {
        return notifications;
    }
    
    /**
     * Sets the list of notifications.
     * @param notifications ArrayList of EntrantNotification objects
     */
    public void setNotifications(ArrayList<EntrantNotification> notifications) {
        this.notifications = notifications;
    }

}
