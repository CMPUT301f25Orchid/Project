package com.example.fairdraw;

import android.provider.ContactsContract;

import java.util.List;

public class Organizer extends User {

    private List<String> eventsCreated;

    public Organizer(String name, ContactsContract.CommonDataKinds.Email email,
                     ContactsContract.CommonDataKinds.Phone phoneNum, String deviceId,
                     String fcmToken, List<String> eventsCreated, List<String> roles) {
        super(name, email, phoneNum, deviceId, fcmToken, roles);
        this.eventsCreated = eventsCreated;
    }
}
