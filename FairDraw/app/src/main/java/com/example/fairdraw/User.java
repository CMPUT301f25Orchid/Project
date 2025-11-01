package com.example.fairdraw;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    protected String name;
    protected String email;
    protected String phoneNum;
    protected String deviceId;

    // For sending messages to email, phone num, etc
    protected String fcmToken;

    protected List<String> roles;

    public User(String name, String email,
                String phoneNum, String deviceId, String fcmToken,
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

    public String getEmail() {
        return email;
    }

    public String getPhoneNum() {
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

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNum(String phoneNum) {
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
