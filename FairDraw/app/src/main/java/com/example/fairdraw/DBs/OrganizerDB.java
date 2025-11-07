package com.example.fairdraw.DBs;

import com.example.fairdraw.Models.Organizer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Provides a high-level API for interacting with the "organizers" collection in Firestore.
 * This class serves as a Firestore service provider for Organizer operations, handling
 * CRUD operations for organizers.
 */
public class OrganizerDB {

    /**
     * Callback interface for when an Organizer is retrieved from the database.
     */
    public interface GetOrganizerCallback {
        /**
         * Called when the organizer retrieval operation is complete.
         * @param organizer The retrieved Organizer object, or null if not found or an error occurred
         */
        void onCallback(Organizer organizer);
    }

    /**
     * Callback interface for when an Organizer is added to the database.
     */
    public interface AddOrganizerCallback {
        /**
         * Called when the add operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when an Organizer is updated in the database.
     */
    public interface UpdateOrganizerCallback {
        /**
         * Called when the update operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when an Organizer is deleted from the database.
     */
    public interface DeleteOrganizerCallback {
        /**
         * Called when the delete operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Gets a reference to the "organizers" collection in Firestore.
     * @return A CollectionReference for the "organizers" collection
     */
    public static CollectionReference getOrganizerCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("organizers");
    }

    /**
     * Asynchronously retrieves an Organizer object from the database by device ID.
     * @param deviceId The device ID of the organizer to retrieve
     * @param callback The callback to be invoked with the result
     */
    public static void getOrganizer(String deviceId, GetOrganizerCallback callback) {
        DocumentReference organizerRef = getOrganizerCollection().document(deviceId);
        organizerRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Organizer organizer = task.getResult().toObject(Organizer.class);
                callback.onCallback(organizer);
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Adds a new organizer to the database.
     * @param organizer The Organizer object to add
     * @param callback The callback to be invoked with the result
     */
    public static void addOrganizer(Organizer organizer, AddOrganizerCallback callback) {
        getOrganizerCollection().document(organizer.getDeviceId()).set(organizer)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Updates an existing organizer in the database.
     * @param organizer The Organizer object with updated data
     * @param callback The callback to be invoked with the result
     */
    public static void updateOrganizer(Organizer organizer, UpdateOrganizerCallback callback) {
        getOrganizerCollection().document(organizer.getDeviceId()).set(organizer)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Deletes an organizer from the database.
     * @param deviceId The device ID of the organizer to delete
     * @param callback The callback to be invoked with the result
     */
    public static void deleteOrganizer(String deviceId, DeleteOrganizerCallback callback) {
        getOrganizerCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }
}
