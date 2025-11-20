package com.example.fairdraw.DBs;

import com.example.fairdraw.Models.Organizer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Firestore helper for operations on the "organizers" collection.
 */
public class OrganizerDB {

    /**
     * Callback for when an Organizer is retrieved from the database.
     */
    public interface GetOrganizerCallback {
        /**
         * Invoked when the organizer read operation completes.
         * @param organizer the Organizer object or null if not found/error
         */
        void onCallback(Organizer organizer);
    }

    /**
     * Callback for when an Organizer is added to the database.
     */
    public interface AddOrganizerCallback {
        /**
         * Invoked when the add operation completes.
         * @param success true if operation succeeded
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Organizer is updated in the database.
     */
    public interface UpdateOrganizerCallback {
        /**
         * Invoked when the update operation completes.
         * @param success true if operation succeeded
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Organizer is deleted from the database.
     */
    public interface DeleteOrganizerCallback {
        /**
         * Invoked when the delete operation completes.
         * @param success true if operation succeeded
         */
        void onCallback(boolean success);
    }

    /**
     * Returns a CollectionReference pointing to the "organizers" collection.
     * @return collection reference for organizers
     */
    public static CollectionReference getOrganizerCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("organizers");
    }

    /**
     * Retrieve an Organizer by device id.
     * @param deviceId document id to fetch
     * @param callback callback invoked with Organizer or null
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
     * Add a new Organizer document.
     * @param organizer organizer to add (organizer.getDeviceId() used as document id)
     * @param callback callback invoked with success flag
     */
    public static void addOrganizer(Organizer organizer, AddOrganizerCallback callback) {
        getOrganizerCollection().document(organizer.getDeviceId()).set(organizer)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Update an existing Organizer document.
     * @param organizer organizer with updated data
     * @param callback callback invoked with success flag
     */
    public static void updateOrganizer(Organizer organizer, UpdateOrganizerCallback callback) {
        getOrganizerCollection().document(organizer.getDeviceId()).set(organizer)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Delete an Organizer document by device id.
     * @param deviceId document id to delete
     * @param callback callback invoked with success flag
     */
    public static void deleteOrganizer(String deviceId, DeleteOrganizerCallback callback) {
        getOrganizerCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }
}
