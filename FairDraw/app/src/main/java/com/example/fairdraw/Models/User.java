package com.example.fairdraw.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a user in the FairDraw application.
 * A user has basic profile information and can have multiple roles such as entrant, organizer, or admin.
 * This class implements Serializable for compatibility with Android intents and Firestore serialization.
 */
public class User implements Serializable {
    /** The user's full name */
    private String name;
    /** The user's email address */
    private String email;
    /** The user's phone number */
    private String phoneNum;
    /** The unique device ID associated with this user */
    private String deviceId;
    /** Firebase Cloud Messaging token for push notifications */
    private String fcmToken;
    /** List of roles assigned to this user (e.g., "entrant", "organizer", "admin") */
    private ArrayList<String> roles;

    /**
     * Default constructor required by Firestore for deserialization.
     * Initializes a new user with the default "entrant" role.
     */
    public User() {
        this.roles = new ArrayList<String>();
        roles.add("entrant");
    }

    /**
     * Constructs a new User with the specified details.
     * Automatically assigns the "entrant" role to the new user.
     * 
     * @param name The user's full name
     * @param email The user's email address
     * @param phoneNum The user's phone number
     * @param deviceId The unique device ID
     * @param fcmToken The Firebase Cloud Messaging token for notifications
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

    /**
     * Gets the user's name.
     * @return The user's full name
     */
    public String getName() { return name; }
    
    /**
     * Gets the user's email address.
     * @return The user's email address
     */
    public String getEmail() { return email; }
    
    /**
     * Gets the user's phone number.
     * @return The user's phone number
     */
    public String getPhoneNum() { return phoneNum; }
    
    /**
     * Gets the unique device ID.
     * @return The device ID associated with this user
     */
    public String getDeviceId() { return deviceId; }
    
    /**
     * Gets the Firebase Cloud Messaging token.
     * @return The FCM token for push notifications
     */
    public String getFcmToken() { return fcmToken; }
    
    /**
     * Gets the list of roles assigned to this user.
     * @return An ArrayList of role names
     */
    public ArrayList<String> getRoles() { return roles; }


    /**
     * Sets the user's name.
     * @param name The user's full name
     */
    public void setName(String name) { this.name = name; }
    
    /**
     * Sets the user's email address.
     * @param email The user's email address
     */
    public void setEmail(String email) { this.email = email; }
    
    /**
     * Sets the user's phone number.
     * @param phoneNum The user's phone number
     */
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }
    
    /**
     * Sets the unique device ID.
     * @param deviceId The device ID to associate with this user
     */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    /**
     * Sets the Firebase Cloud Messaging token.
     * @param fcmToken The FCM token for push notifications
     */
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    
    /**
     * Sets the list of roles for this user.
     * @param roles An ArrayList of role names
     */
    public void setRoles(ArrayList<String> roles) { this.roles = roles; }
}