package com.example.fairdraw;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
}
