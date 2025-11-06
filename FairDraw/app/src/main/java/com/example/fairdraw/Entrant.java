package com.example.fairdraw;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents the model of an entrant(only the unique things to and entrant)
 * and is linked to a user(containing basic user information) by the deviceid
 * */
public class Entrant implements Serializable {
    private String deviceId;
    private ArrayList<String> eventHistory = new ArrayList<>();
    private ArrayList<EntrantNotification> notifications = new ArrayList<>();

    // Just email and phone true or false
    private HashMap<String, Boolean> notificationPrefs = new HashMap<>();

    public Entrant(String deviceId) {
        this.deviceId = deviceId;
    }

    public Entrant() {
        // Required for Firestore deserialization
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<String> getEventHistory() {
        return eventHistory;
    }

    public void setEventHistory(ArrayList<String> eventHistory) {
        this.eventHistory = eventHistory;
    }


    public HashMap<String, Boolean> getNotificationPrefs() {
        return notificationPrefs;
    }

    public void addEventToHistory(String eventId) {
        eventHistory.add(eventId);
    }

    public void setNotificationPrefs(HashMap<String, Boolean> newNotificationPrefs) {
        // Get the enumeration of keys from the new dictionary
        for (String key : newNotificationPrefs.keySet()) {
            notificationPrefs.put(key, newNotificationPrefs.get(key));
        }
    }
    public void addNotification(EntrantNotification notification) {
        notifications.add(notification);
    }
    public void removeNotification(EntrantNotification notification) {
        notifications.remove(notification);
    }
    public ArrayList<EntrantNotification> getNotifications() {
        return notifications;
    }
    public void setNotifications(ArrayList<EntrantNotification> notifications) {
        this.notifications = notifications;
    }

}
