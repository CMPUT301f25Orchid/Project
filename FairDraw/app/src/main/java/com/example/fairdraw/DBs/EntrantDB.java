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
 * Firestore helper for operations on the "entrants" collection.
 *
 * <p>Provides asynchronous CRUD operations and helpers for pushing notifications
 * into an entrant's notifications array.</p>
 */
public class EntrantDB {

    /**
     * Callback for when an Entrant is retrieved from the database.
     */
    public interface GetEntrantCallback {
        /**
         * Invoked when the entrant read operation completes.
         * @param entrant the Entrant object or null if not found/error
         */
        void onCallback(Entrant entrant);
    }

    /**
     * Callback for when an Entrant is added to the database.
     */
    public interface AddEntrantCallback {
        /**
         * Invoked when the add operation completes.
         * @param success true if the operation succeeded, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Entrant is updated in the database.
     */
    public interface UpdateEntrantCallback {
        /**
         * Invoked when the update operation completes.
         * @param success true if the operation succeeded, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Entrant is deleted from the database.
     */
    public interface DeleteEntrantCallback {
        /**
         * Invoked when the delete operation completes.
         * @param success true if the operation succeeded, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Returns a CollectionReference pointing to the "entrants" collection.
     *
     * @return collection reference for entrants
     */
    public static CollectionReference getEntrantCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("entrants");
    }

    /**
     * Asynchronously fetch an Entrant document by device id.
     *
     * @param deviceId document id (device id)
     * @param callback callback to receive the Entrant or null on failure
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
     * Create a new Entrant document.
     *
     * @param entrant Entrant to create (document id is entrant.getDeviceId())
     * @param callback callback invoked with success flag on completion
     */
    public static void addEntrant(Entrant entrant, AddEntrantCallback callback) {
        getEntrantCollection().document(entrant.getDeviceId()).set(entrant)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Update an existing Entrant document.
     *
     * @param entrant Entrant object with updated values
     * @param callback callback invoked with success flag on completion
     */
    public static void updateEntrant(Entrant entrant, UpdateEntrantCallback callback) {
        getEntrantCollection().document(entrant.getDeviceId()).set(entrant)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Delete an Entrant document by device id.
     *
     * @param deviceId document id to delete
     * @param callback callback invoked with success flag on completion
     */
    public static void deleteEntrant(String deviceId, DeleteEntrantCallback callback) {
        getEntrantCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /***
     * Callback for when a notification is pushed to the database.
     */
    public interface PushNotificationCallback {
        /**
         * Invoked when the push completes.
         * @param success true if the push succeeded
         * @param e exception occurred during the operation or null on success
         */
        void onCallback(boolean success, Exception e);
    }

    /**
     * Callback for when a list of notifications is retrieved from the database.
     */
    public interface NotificationsListener {
        /**
         * Invoked when notifications change for the monitored entrant.
         * @param notifications the updated list of notifications
         */
        void onChanged(List<EntrantNotification> notifications);

        /**
         * Invoked when an error occurs while listening for notifications.
         * @param e exception describing the error
         */
        void onError(Exception e);
    }

    /**
     * Push a single notification into a user's notifications array. If the field
     * does not exist this method will create it.
     *
     * @param deviceId target user's device id
     * @param notification notification to push
     * @param callB optional callback to receive operation result
     */
    public static void pushNotificationToUser(String deviceId,
                                              EntrantNotification notification,
                                              PushNotificationCallback callB) {
        DocumentReference ref = getEntrantCollection().document(deviceId);

        ref.update("notifications", FieldValue.arrayUnion(notification))
                .addOnSuccessListener(v -> { if (callB != null) callB.onCallback(true, null); })
                .addOnFailureListener(e -> {
                    // Doc/field missing -> create it
                    Map<String, List<EntrantNotification>> init =
                            Collections.singletonMap("notifications", Arrays.asList(notification));

                    ref.set(init, SetOptions.merge())
                            .addOnSuccessListener(v2 -> { if (callB != null) callB.onCallback(true, null); })
                            .addOnFailureListener(e2 -> { if (callB != null) callB.onCallback(false, e2); });
                });
    }
    

    ;
}
