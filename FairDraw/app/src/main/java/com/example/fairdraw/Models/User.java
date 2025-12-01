package com.example.fairdraw.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a general application user. This model contains common user attributes
 * such as name, email, phone number, device id, FCM token and assigned roles.
 * <p>
 * The class is serializable and designed to be persisted in Firestore. Default
 * users are created with the "entrant" role.
 */
public class User implements Serializable {
    private String name;
    private String email;
    private String phoneNum;
    private String deviceId;
    private String fcmToken;
    private ArrayList<String> roles;

    /**
     * No-argument constructor required by Firestore. Initializes the default roles list
     * with the "entrant" role.
     */
    public User() {}

    /**
     * Constructs a user with the provided profile fields and initializes default roles.
     *
     * @param name     user's full name
     * @param email    user's email address
     * @param phoneNum user's phone number
     * @param deviceId device identifier for linking role-specific models
     * @param fcmToken firebase cloud messaging token for push notifications
     */
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
    /**
     * Returns the user's name.
     * @return name or null if unset
     */
    public String getName() { return name; }
    /**
     * Returns the user's email.
     * @return email or null if unset
     */
    public String getEmail() { return email; }
    /**
     * Returns the user's phone number.
     * @return phone number or null if unset
     */
    public String getPhoneNum() { return phoneNum; }
    /**
     * Returns the device id that links role-specific models (e.g. Organizer) to this user.
     * @return device id or null if unset
     */
    public String getDeviceId() { return deviceId; }
    /**
     * Returns the FCM token used for push notifications.
     * @return fcm token or null if unset
     */
    public String getFcmToken() { return fcmToken; }
    /**
     * Returns the list of role names assigned to the user (e.g. "entrant", "organizer").
     * @return list of role strings
     */
    public ArrayList<String> getRoles() { return roles; }


    // Setters (optional but nice to have)
    /**
     * Sets the user's name.
     * @param name full name to set
     */
    public void setName(String name) { this.name = name; }
    /**
     * Sets the user's email.
     * @param email email address to set
     */
    public void setEmail(String email) { this.email = email; }
    /**
     * Sets the user's phone number.
     * @param phoneNum phone number to set
     */
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }
    /**
     * Sets the device identifier for this user.
     * @param deviceId device id to set
     */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    /**
     * Sets the FCM token for push notifications.
     * @param fcmToken token string to set
     */
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    /**
     * Replaces the roles list for this user.
     * @param roles new list of role names
     */
    public void setRoles(ArrayList<String> roles) { this.roles = roles; }
}