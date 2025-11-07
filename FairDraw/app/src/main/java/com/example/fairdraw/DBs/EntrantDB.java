package com.example.fairdraw.DBs;

import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.Models.Entrant;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a high-level API for interacting with the "entrants" collection in Firestore.
 * This class serves as a Firestore service provider for Entrant operations, handling
 * CRUD operations and notification management for entrants.
 */
public class EntrantDB {

    /**
     * Callback interface for when an Entrant is retrieved from the database.
     */
    public interface GetEntrantCallback {
        /**
         * Called when the entrant retrieval operation is complete.
         * @param entrant The retrieved Entrant object, or null if not found or an error occurred
         */
        void onCallback(Entrant entrant);
    }

    /**
     * Callback interface for when an Entrant is added to the database.
     */
    public interface AddEntrantCallback {
        /**
         * Called when the add operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when an Entrant is updated in the database.
     */
    public interface UpdateEntrantCallback {
        /**
         * Called when the update operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when an Entrant is deleted from the database.
     */
    public interface DeleteEntrantCallback {
        /**
         * Called when the delete operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Gets a reference to the "entrants" collection in Firestore.
     * @return A CollectionReference for the "entrants" collection
     */
    public static CollectionReference getEntrantCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("entrants");
    }

    /**
     * Asynchronously retrieves an Entrant object from the database by device ID.
     * @param deviceId The device ID of the entrant to retrieve
     * @param callback The callback to be invoked with the result
     */
    public static void getEntrant(String deviceId, GetEntrantCallback callback) {
        DocumentReference entrantRef = getEntrantCollection().document(deviceId);
        entrantRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Entrant entrant = task.getResult().toObject(Entrant.class);
                callback.onCallback(entrant);
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Adds a new entrant to the database.
     * @param entrant The Entrant object to add
     * @param callback The callback to be invoked with the result
     */
    public static void addEntrant(Entrant entrant, AddEntrantCallback callback) {
        getEntrantCollection().document(entrant.getDeviceId()).set(entrant)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Updates an existing entrant in the database.
     * @param entrant The Entrant object with updated data
     * @param callback The callback to be invoked with the result
     */
    public static void updateEntrant(Entrant entrant, UpdateEntrantCallback callback) {
        getEntrantCollection().document(entrant.getDeviceId()).set(entrant)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Deletes an entrant from the database.
     * @param deviceId The device ID of the entrant to delete
     * @param callback The callback to be invoked with the result
     */
    public static void deleteEntrant(String deviceId, DeleteEntrantCallback callback) {
        getEntrantCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Callback interface for when a notification is pushed to the database.
     */
    public interface PushNotificationCallback {
        /**
         * Called when the push notification operation is complete.
         * @param success True if the operation was successful, false otherwise
         * @param e The exception if an error occurred, or null otherwise
         */
        void onCallback(boolean success, Exception e);
    }

    /**
     * Callback interface for when a list of notifications is retrieved from the database.
     */
    public interface NotificationsListener {
        /**
         * Called when the notifications list changes.
         * @param notifications The updated list of notifications
         */
        void onChanged(List<EntrantNotification> notifications);

        /**
         * Called when an error occurs while listening for notifications.
         * @param e The exception that occurred
         */
        void onError(Exception e);
    }

    /**
     * Adds one notification to an entrant's notification list in the database.
     * If the notifications field doesn't exist, it will be created.
     * 
     * @param deviceId The device ID of the entrant
     * @param notification The notification to add
     * @param callB The callback to be invoked with the result
     */
    public static void pushNotificationToUser(String deviceId,
                                              EntrantNotification notification,
                                              PushNotificationCallback callB) {
        DocumentReference ref = getEntrantCollection().document(deviceId);

        ref.update("notifications", FieldValue.arrayUnion(notification))
                .addOnSuccessListener(v -> { if (callB != null) callB.onCallback(true, null); })
                .addOnFailureListener(e -> {
                    // Doc/field missing notification  create it
                    Map<String, List<EntrantNotification>> init =
                            Collections.singletonMap("notifications", Arrays.asList(notification));

                    ref.set(init, SetOptions.merge())
                            .addOnSuccessListener(v2 -> { if (callB != null) callB.onCallback(true, null); })
                            .addOnFailureListener(e2 -> { if (callB != null) callB.onCallback(false, e2); });
                });
    }
    

    ;
}
