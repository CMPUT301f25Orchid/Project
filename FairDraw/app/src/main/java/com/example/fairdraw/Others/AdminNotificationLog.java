package com.example.fairdraw.Others;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class AdminNotificationLog implements Serializable {
    public String eventId;
    public String eventTitle;
    public String recipientDeviceId; // The user who received the notification
    public String notificationType;
    @ServerTimestamp
    public Date timestamp;

    public AdminNotificationLog() {}

    public AdminNotificationLog(String recipientDeviceId, String eventTitle, String notificationType, String eventId) {
        this.eventId = eventId;
        this.recipientDeviceId = recipientDeviceId;
        this.eventTitle = eventTitle;
        this.notificationType = notificationType;

    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
