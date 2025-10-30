package com.example.fairdraw;

import android.provider.ContactsContract;

import java.util.List;

public class Admin extends User {
    public Admin(String name, ContactsContract.CommonDataKinds.Email email,
                 ContactsContract.CommonDataKinds.Phone phoneNum, String deviceId,
                 String fcmToken, List<String> roles) {
        super(name, email, phoneNum, deviceId, fcmToken, roles);
    }
}
