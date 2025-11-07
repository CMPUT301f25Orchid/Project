package com.example.fairdraw.Others;

import java.io.Serializable;

/**
 * Represents a single in-app notification for an entrant in the FairDraw application.
 * This class stores notification details such as type, event information, and read status.
 * Implements Serializable for storage in Firestore and passing between activities.
 */
public class EntrantNotification implements Serializable {
    /** The notification type as a string (e.g., "WIN", "LOSE", "WAITLIST") */
    public String type;
    /** The ID of the event this notification is about */
    public String eventId;
    /** The title of the event */
    public String title;
    /** Optional custom message for the notification */
    public String message;
    /** Whether the notification has been read by the user */
    public boolean read = false;
    
    /**
     * Default constructor required for Firestore deserialization.
     */
    public EntrantNotification() {}
    
    /**
     * Constructs a new EntrantNotification with the specified details.
     * 
     * @param t The notification type
     * @param eventId The ID of the event
     * @param title The title of the event
     */
    public EntrantNotification(NotificationType t, String eventId, String title) {
        this.type = (t == null) ? null : t.name();
        this.eventId = eventId;
        this.title = title;
    }
}

