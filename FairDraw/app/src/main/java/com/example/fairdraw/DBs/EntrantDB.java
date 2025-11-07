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
 * This class serves as a Firestore service provider for Entrant operations
 */
public class EntrantDB {

    /**
     * Callback for when an Entrant is retrieved from the database.
     */
    public interface GetEntrantCallback {
        void onCallback(Entrant entrant);
    }

    /**
     * Callback for when an Entrant is added to the database.
     */
    public interface AddEntrantCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Entrant is updated in the database.
     */
    public interface UpdateEntrantCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Entrant is deleted from the database.
     */
    public interface DeleteEntrantCallback {
        void onCallback(boolean success);
    }

    public static CollectionReference getEntrantCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("entrants");
    }

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

    public static void addEntrant(Entrant entrant, AddEntrantCallback callback) {
        getEntrantCollection().document(entrant.getDeviceId()).set(entrant)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void updateEntrant(Entrant entrant, UpdateEntrantCallback callback) {
        getEntrantCollection().document(entrant.getDeviceId()).set(entrant)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void deleteEntrant(String deviceId, DeleteEntrantCallback callback) {
        getEntrantCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /***
     * Callback for when a notification is pushed to the database.
     */
    public interface PushNotificationCallback {
        void onCallback(boolean success, Exception e);
    }

    /**
     * Callback for when a list of notifications is retrieved from the database.
     */
    public interface NotificationsListener {
        void onChanged(List<EntrantNotification> notifications);

        void onError(Exception e);
    }

    /**
     * Adds one notification to one
     **/
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
