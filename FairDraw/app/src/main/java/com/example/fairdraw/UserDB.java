package com.example.fairdraw;

import android.provider.Settings;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

public class UserDB {
    // Function to get current user's device id


    public static CollectionReference getUserCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("users");
        }

    public static Map<String, Object> getUser(String deviceId) {
        DocumentReference userRef = UserDB.getUserCollection().document(deviceId);
        Task<DocumentSnapshot> task = userRef.get();
        if (task.isSuccessful())
        {
            DocumentSnapshot document = task.getResult();
            return document.getData();
        }
        else
        {
            return null;
        }
    }

    public static Boolean addUser(Map<String, Object> user) {
        String deviceId = (String) user.get("deviceId");
        assert deviceId != null;
        DocumentReference userRef = UserDB.getUserCollection().document(deviceId);
        Task<Void> task = userRef.set(user);
        return task.isSuccessful();
    }

    public static Boolean updateUser(Map<String, Object> user) {
        String deviceId = (String) user.get("deviceId");
        assert deviceId != null;
        DocumentReference userRef = UserDB.getUserCollection().document(deviceId);
        return userRef.set(user).isSuccessful();
    }
    public static Boolean deleteUser(String deviceId) {
        DocumentReference userRef = UserDB.getUserCollection().document(deviceId);
        return userRef.delete().isSuccessful();
    }
}
