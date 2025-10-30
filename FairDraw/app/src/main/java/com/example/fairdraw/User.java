package com.example.fairdraw;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    protected String name;
    protected ContactsContract.CommonDataKinds.Email email;
    protected ContactsContract.CommonDataKinds.Phone phoneNum;
    protected String deviceId;

    // For sending messages to email, phone num, etc
    protected String fcmToken;

    protected List<String> roles;

    public User(String name, ContactsContract.CommonDataKinds.Email email,
                ContactsContract.CommonDataKinds.Phone phoneNum, String deviceId, String fcmToken,
                List<String> roles) {
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.roles = roles;
    }

    public User() {
        // Required for Firestore deserialization
    }

    public String getName() {
        return name;
    }

    public ContactsContract.CommonDataKinds.Email getEmail() {
        return email;
    }

    public ContactsContract.CommonDataKinds.Phone getPhoneNum() {
        return phoneNum;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(ContactsContract.CommonDataKinds.Email email) {
        this.email = email;
    }

    public void setPhoneNum(ContactsContract.CommonDataKinds.Phone phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
