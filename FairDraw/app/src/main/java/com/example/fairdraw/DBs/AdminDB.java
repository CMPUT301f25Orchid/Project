package com.example.fairdraw.DBs;

import com.example.fairdraw.Models.Admin;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Provides a high-level API for interacting with the "admins" collection in Firestore.
 * This class serves as a Firestore service provider for Admin operations, handling
 * CRUD operations for admins.
 */
public class AdminDB {

    /**
     * Callback interface for when an Admin is retrieved from the database.
     */
    public interface GetAdminCallback {
        /**
         * Called when the admin retrieval operation is complete.
         * @param admin The retrieved Admin object, or null if not found or an error occurred
         */
        void onCallback(Admin admin);
    }

    /**
     * Callback interface for when an Admin is added to the database.
     */
    public interface AddAdminCallback {
        /**
         * Called when the add operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when an Admin is updated in the database.
     */
    public interface UpdateAdminCallback {
        /**
         * Called when the update operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when an Admin is deleted from the database.
     */
    public interface DeleteAdminCallback {
        /**
         * Called when the delete operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Gets a reference to the "admins" collection in Firestore.
     * @return A CollectionReference for the "admins" collection
     */
    public static CollectionReference getAdminCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("admins");
    }

    /**
     * Asynchronously retrieves an Admin object from the database by device ID.
     * @param deviceId The device ID of the admin to retrieve
     * @param callback The callback to be invoked with the result
     */
    public static void getAdmin(String deviceId, GetAdminCallback callback) {
        DocumentReference adminRef = getAdminCollection().document(deviceId);
        adminRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Admin admin = task.getResult().toObject(Admin.class);
                callback.onCallback(admin);
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Adds a new admin to the database.
     * @param admin The Admin object to add
     * @param callback The callback to be invoked with the result
     */
    public static void addAdmin(Admin admin, AddAdminCallback callback) {
        getAdminCollection().document(admin.getDeviceId()).set(admin)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Updates an existing admin in the database.
     * @param admin The Admin object with updated data
     * @param callback The callback to be invoked with the result
     */
    public static void updateAdmin(Admin admin, UpdateAdminCallback callback) {
        getAdminCollection().document(admin.getDeviceId()).set(admin)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Deletes an admin from the database.
     * @param deviceId The device ID of the admin to delete
     * @param callback The callback to be invoked with the result
     */
    public static void deleteAdmin(String deviceId, DeleteAdminCallback callback) {
        getAdminCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }
}
