package com.example.fairdraw.Others;

import java.io.Serializable;
/**
 * Represents a single in-app notification stored for an entrant.
 * <p>
 * Fields are public for easy (de)serialization with Firestore.
 */
public class EntrantNotification implements Serializable {
    /** Notification type, for example "WIN", "LOSE", etc. */
    public String type;            // status such as "WIN", "LOSE"
    /** Associated event id for this notification. */
    public String eventId;
    /** Title of the event this notification refers to. */
    public String title;           // event title
    /** Optional message body for the notification. */
    public String message;
    /** Whether the notification has been read by the user. Defaults to false. */
    public boolean read = false;

    /**
     * Default no-argument constructor required by Firestore / serialization.
     */
    public EntrantNotification() {}

    /**
     * Create a notification with a known type, event id and title.
     *
     * @param t the notification type enum (may be null)
     * @param eventId the event id associated with this notification
     * @param title the event title to display
     */
    public EntrantNotification(NotificationType t, String eventId, String title) {
        this.type = (t == null) ? null : t.name();
        this.eventId = eventId;
        this.title = title;
    }
}
