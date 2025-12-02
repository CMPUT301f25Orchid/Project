package com.example.fairdraw.DBs;

import android.util.Log;

import com.example.fairdraw.Others.AdminNotificationLog;
import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.Models.Entrant;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

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

    static String TAG = "EntrantDB";

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

    public interface SimpleCallback {
        void onCallback(boolean success, Exception e);
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

    /**
     * Attach a real-time snapshot listener for a single entrant document.
     *
     * @param entrantId id of the entrant to listen to (usually deviceId)
     * @param callback callback that receives Entrant updates
     * @return ListenerRegistration handle which can be used to remove the listener
     */
    public static ListenerRegistration listenToEntrant(String entrantId, GetEntrantCallback callback) {

        DocumentReference entrantRef = getEntrantCollection().document(entrantId);

        return entrantRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "listenToEntrant: snapshot listener error for id " + entrantId, e);
                callback.onCallback(null);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    Entrant entrant = snapshot.toObject(Entrant.class);
                    callback.onCallback(entrant);
                } catch (Exception ex) {
                    Log.e(TAG, "listenToEntrant: failed to deserialize entrant with id " + entrantId, ex);
                    callback.onCallback(null);
                }
            } else {
                callback.onCallback(null);
            }
        });
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
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "pushNotificationToUser: successfully pushed notification to user " + deviceId);
                    AdminNotificationLog log = new AdminNotificationLog(
                            deviceId,
                            notification.title,
                            notification.type,
                            notification.eventId

                    );
                    Log.d(TAG, "pushNotificationToUser: logging admin notification: " + log.recipientDeviceId + ", " + log.notificationType + ", " + log.eventId);
                    AdminDB.logNotification(log);
                    if (callB != null) callB.onCallback(true, null);
                })
                .addOnFailureListener(e -> {
                    // Create the notifications array if missing
                    Map<String, List<EntrantNotification>> init = Collections.singletonMap(
                            "notifications",
                            Collections.singletonList(notification)
                    );
                    init.put("notifications", Collections.singletonList(notification));

                    ref.set(init, SetOptions.merge())
                            .addOnSuccessListener(v2 -> {
                                AdminNotificationLog log = new AdminNotificationLog(
                                        deviceId,
                                        notification.title,
                                        notification.type,
                                        notification.eventId
                                );
                                AdminDB.logNotification(log);
                                if (callB != null) callB.onCallback(true, null);

                            })
                            .addOnFailureListener(e2 -> {
                                if (callB != null) callB.onCallback(false, e2);
                            });
                });
    }


    public static void addEventToHistory(
            String entrantId,
            String eventId,
            String initialStatus,
            SimpleCallback callback
    ) {
        DocumentReference ref = getEntrantCollection().document(entrantId);

        Map<String, Object> statusObj = new HashMap<>();
        statusObj.put("status", initialStatus);
        statusObj.put("lastUpdated", FieldValue.serverTimestamp());

        Map<String, Object> updates = new HashMap<>();
        updates.put("eventHistory", FieldValue.arrayUnion(eventId));
        updates.put("eventHistoryStatus." + eventId, statusObj);

        ref.update(updates)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCallback(true, null);
                })
                .addOnFailureListener(e -> {
                    // Create the entire structure if missing
                    Map<String, Object> init = new HashMap<>();
                    init.put("eventHistory", Collections.singletonList(eventId));
                    init.put("eventHistoryStatus",
                            Collections.singletonMap(eventId, statusObj));

                    ref.set(init, SetOptions.merge())
                            .addOnSuccessListener(v2 -> {
                                if (callback != null) callback.onCallback(true, null);
                            })
                            .addOnFailureListener(e2 -> {
                                if (callback != null) callback.onCallback(false, e2);
                            });
                });
    }

    public static void updateEventHistoryStatus(
            String entrantId,
            String eventId,
            String newStatus,
            SimpleCallback callback
    ) {
        DocumentReference ref = getEntrantCollection().document(entrantId);

        Map<String, Object> statusObj = new HashMap<>();
        statusObj.put("status", newStatus);
        statusObj.put("lastUpdated", FieldValue.serverTimestamp());

        Map<String, Object> updates = new HashMap<>();
        updates.put("eventHistoryStatus." + eventId, statusObj);

        ref.update(updates)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCallback(true, null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onCallback(false, e);
                });
    }


}
