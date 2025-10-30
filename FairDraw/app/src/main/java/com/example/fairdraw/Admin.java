package com.example.fairdraw;

import android.provider.ContactsContract;

public class Admin extends User {
    public Admin(String name, ContactsContract.CommonDataKinds.Email email,
                 ContactsContract.CommonDataKinds.Phone phoneNum, String deviceId,
                 String fcmToken) {
        super(name, email, phoneNum, deviceId, fcmToken);
    }
}
