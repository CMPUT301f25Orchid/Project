package com.example.fairdraw.Models;

import com.example.fairdraw.Others.EntrantNotification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Model representing an entrant in the application. An Entrant holds entrant-specific
 * data such as event participation history, notification preferences and a list of
 * notifications. It is linked to a {@link User} via the deviceId field.
 */
public class Entrant implements Serializable {
    private String deviceId;
    private ArrayList<String> eventHistory = new ArrayList<>();
    private ArrayList<EntrantNotification> notifications = new ArrayList<>();

    // Just email and phone true or false
    private final HashMap<String, Boolean> notificationPrefs = new HashMap<>();

    /**
     * Constructs an Entrant for the given device id.
     *
     * @param deviceId device identifier that links this entrant to a {@link User}
     */
    public Entrant(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * No-argument constructor required for Firestore deserialization.
     */
    public Entrant() {
        // Required for Firestore deserialization
    }

    /**
     * Returns the device identifier that links this entrant to a {@link User}.
     *
     * @return device id string, or null if not set
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device identifier for this entrant.
     *
     * @param deviceId unique device id to link to a {@link User}
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the event history for this entrant.
     *
     * @return unmodifiable list of event ids representing this entrant's past events
     */
    public List<String> getEventHistory() {
        return eventHistory;
    }

    /**
     * Replaces the event history list. Useful for deserialization.
     *
     * @param eventHistory list of event ids to set as history
     */
    public void setEventHistory(ArrayList<String> eventHistory) {
        this.eventHistory = eventHistory;
    }

    public void addEventToHistoryOnce(String eventId) {
        if (!eventHistory.contains(eventId)) {
            eventHistory.add(eventId);
        }
    }


    /**
     * Returns the notification preferences map where keys are preference names (e.g. "email",
     * "phone") and values indicate whether that channel is enabled.
     *
     * @return map of notification preference flags
     */
    public HashMap<String, Boolean> getNotificationPrefs() {
        return notificationPrefs;
    }

    /**
     * Adds an event id to the entrant's event history.
     *
     * @param eventId id of the event to add
     */
    public void addEventToHistory(String eventId) {
        eventHistory.add(eventId);
    }

    /**
     * Updates notification preferences by copying entries from the provided map into the
     * local preferences map.
     * \Existing keys will be overwritten.
     *
     * @param newNotificationPrefs map of preference keys to boolean values
     */
    public void setNotificationPrefs(HashMap<String, Boolean> newNotificationPrefs) {
        // Get the enumeration of keys from the new dictionary
        for (String key : newNotificationPrefs.keySet()) {
            notificationPrefs.put(key, newNotificationPrefs.get(key));
        }
    }

    /**
     * Adds a notification to the entrant's notification list.
     *
     * @param notification notification object to add
     */
    public void addNotification(EntrantNotification notification) {
        notifications.add(notification);
    }

    /**
     * Removes a notification from the entrant's notification list.
     *
     * @param notification notification object to remove
     */
    public void removeNotification(EntrantNotification notification) {
        notifications.remove(notification);
    }

    /**
     * Returns the list of notifications for this entrant.
     *
     * @return list of {@link EntrantNotification} objects
     */
    public ArrayList<EntrantNotification> getNotifications() {
        return notifications;
    }

    /**
     * Replaces the notifications list. Useful for deserialization.
     *
     * @param notifications new list of notifications
     */
    public void setNotifications(ArrayList<EntrantNotification> notifications) {
        this.notifications = notifications;
    }

}

