package com.example.fairdraw;

import android.provider.ContactsContract;

import java.util.Dictionary;
import java.util.List;

public class Entrant extends User {
    private List<String> eventHistory;

    // Just email and phone true or false
    private Dictionary<String, Boolean> notificationPrefs;

    public Entrant(String name, ContactsContract.CommonDataKinds.Email email,
                   ContactsContract.CommonDataKinds.Phone phoneNum, String deviceId,
                   String fcmToken, List<String> eventHistory,
                   Dictionary<String, Boolean> notificationPrefs, List<String> roles) {
        super(name, email, phoneNum, deviceId, fcmToken, roles);
        this.eventHistory = eventHistory;
        this.notificationPrefs = notificationPrefs;
    }
}
