package com.example.fairdraw.DBs;

import androidx.annotation.Nullable;

import com.example.fairdraw.Models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Provides a high-level API for interacting with the "users" collection in Firestore.
 * This class handles all the database operations for User objects.
 */
public class UserDB {

    /**
     * Callback interface for when a User is fetched from the database.
     */
    public interface GetUserCallback {
        /**
         * Called when the user retrieval operation is complete.
         * @param user The retrieved User object, or null if not found.
         * @param e The exception if an error occurred, or null otherwise.
         */
        void onCallback(@Nullable User user, @Nullable Exception e);
    }

    /**
     * Callback interface for checking if a user exists in the database.
     */
    public interface ExistsCallback {
        /**
         * Called when the existence check is complete.
         * @param exists True if the user exists, false otherwise.
         * @param e The exception if an error occurred, or null otherwise.
         */
        void onCallback(boolean exists, @Nullable Exception e);
    }

    /**
     * Callback interface for write operations (add/update) on a User.
     */
    public interface WriteCallback {
        /**
         * Called when the write operation is complete.
         * @param ok True if the operation was successful, false otherwise.
         * @param e The exception if an error occurred, or null otherwise.
         */
        void onCallback(boolean ok, @Nullable Exception e);
    }

    /**
     * Callback interface for delete operations on a User.
     */
    public interface DeleteCallback {
        /**
         * Called when the delete operation is complete.
         * @param ok True if the operation was successful, false otherwise.
         * @param e The exception if an error occurred, or null otherwise.
         */
        void onCallback(boolean ok, @Nullable Exception e);
    }

    /**
     * Gets a reference to the "users" collection in Firestore.
     * @return A CollectionReference for the "users" collection.
     */
    private static CollectionReference getUserCollection() {
        return FirebaseFirestore.getInstance().collection("users");
    }

    /**
     * Asynchronously retrieves a User object from the database.
     *
     * @param deviceId The device ID of the user to retrieve.
     * @param cb The callback to be invoked with the result. On success: cb.onCallback(user, null).
     *           If the document doesn't exist: cb.onCallback(null, null). On failure: cb.onCallback(null, exception).
     */
    public static void getUserOrNull(String deviceId, GetUserCallback cb) {
        getUserCollection().document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        cb.onCallback(doc.toObject(User.class), null);
                    } else {
                        cb.onCallback(null, null);
                    }
                })
                .addOnFailureListener(e -> cb.onCallback(null, e));
    }

    /**
     * Adds a snapshot listener to a user document for real-time updates.
     * @param deviceId The device ID of the user to listen to.
     * @param cb The callback to be invoked with the result on each update.
     * @return A ListenerRegistration that can be used to remove the listener.
     */
    public static ListenerRegistration addUserSnapshotListener(String deviceId, GetUserCallback cb) {
        return getUserCollection().document(deviceId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                cb.onCallback(null, e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                cb.onCallback(snapshot.toObject(User.class), null);
            } else {
                cb.onCallback(null, null);
            }
        });
    }

    /**
     * Asynchronously checks if a user with the given device ID exists in the database.
     *
     * @param deviceId The device ID to check.
     * @param cb The callback to be invoked with the result. On success: cb.onCallback(exists, null).
     *           On failure: cb.onCallback(false, exception).
     */
    public static void userExists(String deviceId, ExistsCallback cb) {
        getUserCollection().document(deviceId).get()
                .addOnSuccessListener(DocumentSnapshot::exists)
                .addOnSuccessListener(exists -> cb.onCallback(exists.exists(), null))
                .addOnFailureListener(e -> cb.onCallback(false, e));
    }

    /**
     * Creates or updates a user in the database.
     * If a user with the same device ID already exists, it will be updated.
     * Otherwise, a new user will be created.
     *
     * @param user The User object to upsert.
     * @param cb The callback to be invoked with the result. On success: cb.onCallback(true, null).
     *           On failure: cb.onCallback(false, exception).
     */
    public static void upsertUser(User user, WriteCallback cb) {
        getUserCollection().document(user.getDeviceId()).set(user)
                .addOnSuccessListener(v -> cb.onCallback(true, null))
                .addOnFailureListener(e -> cb.onCallback(false, e));
    }

    /**
     * Deletes a user from the database.
     * @param deviceId The device ID of the user to delete.
     * @param cb The callback to be invoked with the result. On success: cb.onCallback(true, null).
     *           On failure: cb.onCallback(false, exception).
     */
    public static void deleteUser(String deviceId, DeleteCallback cb) {
        getUserCollection().document(deviceId).delete()
                .addOnSuccessListener(v -> cb.onCallback(true, null))
                .addOnFailureListener(e -> cb.onCallback(false, e));
    }

//    /**
//     * Generic callback for operations that succeed or fail without returning data.
//     */
//    public interface GeneralCallback {
//        /**
//         * Called when the operation is complete.
//         * @param ok True if the operation was successful, false otherwise.
//         * @param e The exception if an error occurred, or null otherwise.
//         */
//        void onCallback(boolean ok, @Nullable Exception e);
//    }
//
//    /**
//     * Deletes a user's role document from a specified collection (e.g., "Organisers", "Entrants").
//     * @param collectionName The name of the role collection.
//     * @param userId The ID of the user document to delete from that collection.
//     * @param cb The callback to be invoked with the result.
//     */
//    public static void deleteRole(String collectionName, String userId, GeneralCallback cb) {
//        FirebaseFirestore.getInstance().collection(collectionName).document(userId)
//                .delete()
//                .addOnSuccessListener(v -> cb.onCallback(true, null))
//                .addOnFailureListener(e -> cb.onCallback(false, e));
//    } DON'T FORGET TO MOVE GENERAL CALLBACK UP, CLOSER TO THE OTHER CALLBACK METHODS
}
