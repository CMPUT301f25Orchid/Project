package com.example.fairdraw.Others;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class AdminNotificationLog {
    private String eventId;
    private String eventTitle;
    private String recipientDeviceId; // The user who received the notification
    private String userName;
    private NotificationType notificationType;
    @ServerTimestamp
    private Date timestamp;

    public AdminNotificationLog() {}

    public AdminNotificationLog(String adminId, String removeOrganizer, String s, Date date) {
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }



    public AdminNotificationLog(String eventId, String userName,String eventTitle, String recipientDeviceId, NotificationType notificationType) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.recipientDeviceId = recipientDeviceId;
        this.notificationType = notificationType;
        this.userName = userName;
    }

    public String getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public String getRecipientDeviceId() { return recipientDeviceId; }
    public NotificationType getNotificationType() { return notificationType; }
    public String getUserName() { return userName; }
    public Date getTimestamp() { return timestamp; }

}
