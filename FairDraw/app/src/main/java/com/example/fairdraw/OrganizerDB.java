package com.example.fairdraw;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This class serves as a Firestore service provider for Organizer operations
 */
public class OrganizerDB {

    /**
     * Callback for when an Organizer is retrieved from the database.
     */
    public interface GetOrganizerCallback {
        void onCallback(Organizer organizer);
    }

    /**
     * Callback for when an Organizer is added to the database.
     */
    public interface AddOrganizerCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Organizer is updated in the database.
     */
    public interface UpdateOrganizerCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Organizer is deleted from the database.
     */
    public interface DeleteOrganizerCallback {
        void onCallback(boolean success);
    }

    public static CollectionReference getOrganizerCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("organizers");
    }

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

    public static void addOrganizer(Organizer organizer, AddOrganizerCallback callback) {
        getOrganizerCollection().document(organizer.getDeviceId()).set(organizer)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void updateOrganizer(Organizer organizer, UpdateOrganizerCallback callback) {
        getOrganizerCollection().document(organizer.getDeviceId()).set(organizer)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void deleteOrganizer(String deviceId, DeleteOrganizerCallback callback) {
        getOrganizerCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }
}
