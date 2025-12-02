package com.example.fairdraw.DBs;

import com.example.fairdraw.Models.Organizer;
import com.example.fairdraw.Others.AdminNotificationLog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;

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
    /**
     * Remove an organizer and clean up their events in a single batch:
     * - Delete events with no endDate or endDate in the future (current/ongoing/upcoming)
     * - For past events (endDate in the past) set organizer -> "Unknown organizer"
     * - Delete the organizer document
     *
     * All operations are committed in one WriteBatch so they succeed or fail together.
     *
     * @param organizerId document id of the organizer to remove
     * @param callback callback invoked with success flag
     */
    public static void removeOrganizerAndCleanupEvents(
            String organizerId,
            String adminId,                        // NEW argument
            DeleteOrganizerCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCol = db.collection("events");

        Query q = eventsCol.whereEqualTo("organizer", organizerId);
        q.get().addOnCompleteListener(queryTask -> {
            if (!queryTask.isSuccessful() || queryTask.getResult() == null) {
                callback.onCallback(false);
                return;
            }

            QuerySnapshot snapshot = queryTask.getResult();
            WriteBatch batch = db.batch();
            Date now = new Date();

            for (QueryDocumentSnapshot doc : snapshot) {
                Date endDate = doc.getDate("endDate");
                if (endDate == null || endDate.after(now)) {
                    batch.delete(doc.getReference());
                } else {
                    batch.update(doc.getReference(), "organizer", "Unknown organizer");
                }
            }

            DocumentReference organizerRef = getOrganizerCollection().document(organizerId);
            batch.delete(organizerRef);

            batch.commit().addOnCompleteListener(commitTask -> {

                boolean success = commitTask.isSuccessful();

                if (success) {
                    // NEW logging block
                    AdminNotificationLog log = new AdminNotificationLog(
                            adminId,
                            "REMOVE_ORGANIZER",
                            "Organizer " + organizerId + " removed and events cleaned up",
                            //fixed audience to ADMIN only for this action
                            "ADMIN"

                    );
                    AdminDB.logNotification(log);
                }

                callback.onCallback(success);
            });
        });
    }
}

