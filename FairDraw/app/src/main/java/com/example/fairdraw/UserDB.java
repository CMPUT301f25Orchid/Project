package com.example.fairdraw;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This class serves as a Firestore service provider for the basic User information
 */
public class UserDB {

    /**
     * Callback for when a User is retrieved from the database.
     */
    public interface GetUserCallback {
        void onCallback(User user);
    }

    /**
     * Callback for when a User is added to the database.
     */
    public interface AddUserCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when a User is updated in the database.
     */
    public interface UpdateUserCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when a User is deleted from the database.
     */
    public interface DeleteUserCallback {
        void onCallback(boolean success);
    }

    public static CollectionReference getUserCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("users");
    }

    public static void getUser(String deviceId, GetUserCallback callback) {
        DocumentReference userRef = getUserCollection().document(deviceId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                callback.onCallback(user);
            } else {
                callback.onCallback(null);
            }
        });
    }

    public static void addUser(User user, AddUserCallback callback) {
        getUserCollection().document(user.getDeviceId()).set(user)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void updateUser(User user, UpdateUserCallback callback) {
        getUserCollection().document(user.getDeviceId()).set(user)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void deleteUser(String deviceId, DeleteUserCallback callback) {
        getUserCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }
}
