package com.example.fairdraw;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This class serves as a Firestore service provider for Admin operations
 */
public class AdminDB {

    /**
     * Callback for when an Admin is retrieved from the database.
     */
    public interface GetAdminCallback {
        void onCallback(Admin admin);
    }

    /**
     * Callback for when an Admin is added to the database.
     */
    public interface AddAdminCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Admin is updated in the database.
     */
    public interface UpdateAdminCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Admin is deleted from the database.
     */
    public interface DeleteAdminCallback {
        void onCallback(boolean success);
    }

    public static CollectionReference getAdminCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("admins");
    }

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

    public static void addAdmin(Admin admin, AddAdminCallback callback) {
        getAdminCollection().document(admin.getDeviceId()).set(admin)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void updateAdmin(Admin admin, UpdateAdminCallback callback) {
        getAdminCollection().document(admin.getDeviceId()).set(admin)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void deleteAdmin(String deviceId, DeleteAdminCallback callback) {
        getAdminCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }
}
