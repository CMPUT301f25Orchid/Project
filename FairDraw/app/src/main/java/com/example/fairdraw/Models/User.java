package com.example.fairdraw.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String name;
    private String email;
    private String phoneNum;
    private String deviceId;
    private String fcmToken;
    private ArrayList<String> roles;

    // Required by Firestore
    public User() {
        this.roles = new ArrayList<String>();
        roles.add("entrant");
    }

    public User(String name, String email, String phoneNum, String deviceId, String fcmToken) {
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.roles = new ArrayList<String>();
        this.roles.add("entrant");
    }

    // Getters (Firestore needs these)
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNum() { return phoneNum; }
    public String getDeviceId() { return deviceId; }
    public String getFcmToken() { return fcmToken; }
    public ArrayList<String> getRoles() { return roles; }


    // Setters (optional but nice to have)
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setRoles(ArrayList<String> roles) { this.roles = roles; }
}