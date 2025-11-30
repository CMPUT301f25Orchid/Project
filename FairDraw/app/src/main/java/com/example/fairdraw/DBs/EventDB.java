package com.example.fairdraw.DBs;

import androidx.annotation.Nullable;

import com.example.fairdraw.Models.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore helper for operations on the "events" collection.
 *
 * <p>Provides CRUD operations, list retrieval, and real-time listeners for events.</p>
 */
public class EventDB {

    // Tag for logging
    private static final String TAG = "EventDB";

    /**
     * Callback for when an Event is retrieved from the database.
     */
    public interface GetEventCallback {
        /**
         * Invoked when a single event has been read.
         * @param event the Event object or null if not found/error
         */
        void onCallback(Event event);
    }

    /**
     * Callback for when a list of Events is retrieved from the database.
     */
    public interface GetEventsCallback {
        /**
         * Invoked when the events list read completes.
         * @param events list of events or null on error
         */
        void onCallback(List<Event> events);
    }

    /**
     * Callback for when an Event is added to the database.
     */
    public interface AddEventCallback {
        /**
         * Invoked when the add operation completes.
         * @param success true if the operation succeeded
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Event is updated in the database.
     */
    public interface UpdateEventCallback {
        /**
         * Invoked when the update operation completes.
         * @param success true if the operation succeeded
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Event is deleted from the database.
     */
    public interface DeleteEventCallback {
        /**
         * Invoked when the delete operation completes.
         * @param success true if the operation succeeded
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when a user is removed from the waitlist.
     */
    public interface RemoveFromWaitlistCallback {
        /**
         * Invoked when the remove-from-waitlist operation completes.
         * @param success true if the operation succeeded
         */
        void onCallback(boolean success);
    }

    /**
     * Callback for when a user is added to the waitlist.
     */
    public interface AddToWaitlistCallback {
        /**
         * Invoked when the add-to-waitlist operation completes.
         * @param success true if the operation succeeded
         */
        void onCallback(boolean success);
    }

    /**
     * Returns a CollectionReference pointing to the "events" collection.
     * @return collection reference for events
     */
    public static CollectionReference getEventCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("events");
    }

    /**
     * Get a single event by id.
     * @param eventId id of the event to retrieve
     * @param callback callback invoked with the Event or null
     */
    public static void getEvent(String eventId, GetEventCallback callback) {
        DocumentReference eventRef = getEventCollection().document(eventId);
        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    if (task.getResult() != null) {
                        Event event = task.getResult().toObject(Event.class);
                        callback.onCallback(event);
                    } else {
                        Log.e(TAG, "getEvent: task returned null result for id " + eventId);
                        callback.onCallback(null);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to deserialize event with id " + eventId, ex);
                    callback.onCallback(null);
                }
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Attach a real-time snapshot listener for a single event document.
     *
     * @param eventId id of the event to listen to
     * @param callback callback that receives Event updates
     * @return ListenerRegistration handle which can be used to remove the listener
     */
    public static ListenerRegistration listenToEvent(String eventId, GetEventCallback callback) {
        DocumentReference eventRef = getEventCollection().document(eventId);
        return eventRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                callback.onCallback(null);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    Event event = snapshot.toObject(Event.class);
                    callback.onCallback(event);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to deserialize event in listener for id " + eventId, ex);
                    callback.onCallback(null);
                }
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Add a new event to the database.
     *
     * @param event event to add (event.getUuid() used as document id)
     * @param callback callback invoked with success flag when the operation completes
     */
    public static void addEvent(Event event, AddEventCallback callback) {
        getEventCollection().document(event.getUuid().toString()).set(event)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Update an existing event document.
     *
     * @param event event with updated data
     * @param callback callback invoked with success flag when the operation completes
     */
    public static void updateEvent(Event event, UpdateEventCallback callback) {
        getEventCollection().document(event.getUuid().toString()).set(event)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Delete an event by id.
     *
     * @param eventId id of the event to delete
     * @param callback callback invoked with success flag when the operation completes
     */
    public static void deleteEvent(String eventId, DeleteEventCallback callback) {
        getEventCollection().document(eventId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Retrieve all events once.
     *
     * @param callback callback invoked with the list of events or null on error
     */
    public static void getEvents(GetEventsCallback callback) {
        getEventCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Event> events = new ArrayList<>();
                if (task.getResult() != null) {
                    for (DocumentSnapshot ds : task.getResult().getDocuments()) {
                        try {
                            Event ev = ds.toObject(Event.class);
                            events.add(ev);
                        } catch (Exception ex) {
                            Log.e(TAG, "Failed to deserialize event with id " + ds.getId(), ex);
                            // continue processing other documents
                        }
                    }
                }
                callback.onCallback(events);
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Attaches a snapshot listener to the events collection for real-time updates.
     *
     * @param callback The callback to be invoked on data changes.
     * @return The listener registration.
     */
    public static ListenerRegistration listenToEvents(GetEventsCallback callback) {
        return getEventCollection().addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                // An error occurred
                callback.onCallback(null);
                return;
            }

            if (snapshots != null) {
                List<Event> events = new ArrayList<>();
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    try {
                        Event ev = ds.toObject(Event.class);
                        events.add(ev);
                    } catch (Exception ex) {
                        Log.e(TAG, "Failed to deserialize event with id " + ds.getId(), ex);
                    }
                }
                callback.onCallback(events);
            } else {
                callback.onCallback(null);
            }
        });
    }


    /**
     * Removes a device from the waitlist of a specific event.
     * @param eventId The ID of the event to modify.
     * @param deviceId The ID of the device to remove from the waitlist.
     * @param callback The callback to be invoked upon completion.
     */
    public static void removeFromWaitlist(String eventId, String deviceId, RemoveFromWaitlistCallback callback) {
        DocumentReference eventRef = getEventCollection().document(eventId);
        eventRef.update("waitingList", FieldValue.arrayRemove(deviceId))
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Adds a device to the waitlist of a specific event.
     * @param eventId The ID of the event to modify.
     * @param deviceId The ID of the device to add to the waitlist.
     * @param callback The callback to be invoked upon completion.
     */
    public static void addToWaitlist(String eventId,
                                     String deviceId,
                                     @Nullable Event.EntrantLocation location,
                                     AddToWaitlistCallback callback) {
        DocumentReference eventRef = getEventCollection().document(eventId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("waitingList", FieldValue.arrayUnion(deviceId));

        if (location != null) {
            updates.put("waitlistLocations." + deviceId, location);
        }

        eventRef.update(updates)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }
}
