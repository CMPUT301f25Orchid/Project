package com.example.fairdraw.Others;

import java.io.Serializable;
/**
 * AppNotification represents a single in-app notification stored in
*/
public class EntrantNotification implements Serializable {
    public String type;            // status such as "WIN", "LOSE"
    public String eventId;
    public String title;           // event title
    public String message;
    public boolean read = false;
    public EntrantNotification() {}
    public EntrantNotification(NotificationType t, String eventId, String title) {
        this.type = (t == null) ? null : t.name();
        this.eventId = eventId;
        this.title = title;
    }
}

