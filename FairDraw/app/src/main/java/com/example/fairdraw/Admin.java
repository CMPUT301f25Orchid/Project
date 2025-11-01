package com.example.fairdraw;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents the model of an Admin(only the unique things to an Admin)
 * and is linked to a user(containing basic user information) by the deviceid
 * */
public class Admin implements Serializable {
    private String deviceId;

    public Admin(String deviceId) {
        this.deviceId = deviceId;
    }

    public Admin() {
        // Required for Firestore deserialization
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
