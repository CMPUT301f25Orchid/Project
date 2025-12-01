package com.example.fairdraw.DBs;

import com.example.fairdraw.Models.Admin;
import com.example.fairdraw.Others.AdminNotificationLog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Firestore helper for operations on the "admins" collection.
 *
 * <p>Provides simple asynchronous CRUD operations and small callback interfaces
 * that callers can implement to receive results.</p>
 */
public class AdminDB {

    /**
     * Callback for when an Admin is retrieved from the database.
     */
    private static final String LOG_COLLECTION = "notification_logs";



    public interface GetAdminCallback {
        /**
         * Invoked when the admin read operation completes.
         * @param admin the Admin object returned from Firestore or null if not found/error
         */
        void onCallback(Admin admin);
    }

    /**
     * Callback for when an Admin is added to the database.
     */
    public interface AddAdminCallback {
        /**
         * Invoked when the add operation completes.
         * @param success true if the operation succeeded, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Admin is updated in the database.
     */
    public interface UpdateAdminCallback {
        /**
         * Invoked when the update operation completes.
         * @param success true if the operation succeeded, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Admin is deleted from the database.
     */
    public interface DeleteAdminCallback {
        /**
         * Invoked when the delete operation completes.
         * @param success true if the operation succeeded, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Returns a reference to the Firestore collection that stores admins.
     *
     * @return CollectionReference pointing to the "admins" collection
     */
    public static CollectionReference getAdminCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("admins");
    }

    /**
     * Asynchronously retrieve an Admin document by device id.
     *
     * @param deviceId document id (device id) of the admin
     * @param callback callback to receive the Admin or null on failure
     */
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

    /**
     * Add a new Admin document to Firestore.
     *
     * @param admin Admin object to persist (its deviceId is used as the document id)
     * @param callback callback invoked with success flag when the operation completes
     */
    public static void addAdmin(Admin admin, AddAdminCallback callback) {
        getAdminCollection().document(admin.getDeviceId()).set(admin)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Update an existing Admin document in Firestore.
     *
     * @param admin Admin object containing updated data (document id is admin.getDeviceId())
     * @param callback callback invoked with success flag when the operation completes
     */
    public static void updateAdmin(Admin admin, UpdateAdminCallback callback) {
        getAdminCollection().document(admin.getDeviceId()).set(admin)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Delete an Admin document from Firestore by device id.
     *
     * @param deviceId document id (device id) to delete
     * @param callback callback invoked with success flag when the operation completes
     */
    public static void deleteAdmin(String deviceId, DeleteAdminCallback callback) {
        getAdminCollection().document(deviceId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Writes a single log record to the central 'notification_logs' collection.
     * This is a simple "fire and forget" action with no callback.
     * @param log The AdminNotificationLog object to be saved.
     */
    public static void logNotification(AdminNotificationLog log) {
        FirebaseFirestore.getInstance().collection(LOG_COLLECTION).add(log);
    }

    /**
     * Provides a Firestore Query for an admin screen to read all notification logs,
     * sorted with the newest entries first.
     * @return A Query object to be used with a FirestoreRecyclerAdapter or snapshot listener.
     */
    public static com.google.firebase.firestore.Query getNotificationLogsQuery() {
        return FirebaseFirestore.getInstance().collection(LOG_COLLECTION)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING);
    }

}
