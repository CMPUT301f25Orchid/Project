package com.example.fairdraw.DBs;

import com.example.fairdraw.Models.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Provides a high-level API for interacting with the "events" collection in Firestore.
 * This class serves as a Firestore service provider for Event operations, handling
 * CRUD operations, real-time listeners, and waitlist management for events.
 */
public class EventDB {

    /**
     * Callback interface for when an Event is retrieved from the database.
     */
    public interface GetEventCallback {
        /**
         * Called when the event retrieval operation is complete.
         * @param event The retrieved Event object, or null if not found or an error occurred
         */
        void onCallback(Event event);
    }

    /**
     * Callback interface for when a list of Events is retrieved from the database.
     */
    public interface GetEventsCallback {
        /**
         * Called when the events retrieval operation is complete.
         * @param events The list of retrieved Event objects, or null if an error occurred
         */
        void onCallback(List<Event> events);
    }

    /**
     * Callback interface for when an Event is added to the database.
     */
    public interface AddEventCallback {
        /**
         * Called when the add operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when an Event is updated in the database.
     */
    public interface UpdateEventCallback {
        /**
         * Called when the update operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when an Event is deleted from the database.
     */
    public interface DeleteEventCallback {
        /**
         * Called when the delete operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when a user is removed from the waitlist.
     */
    public interface RemoveFromWaitlistCallback {
        /**
         * Called when the remove operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Callback interface for when a user is added to the waitlist.
     */
    public interface AddToWaitlistCallback {
        /**
         * Called when the add operation is complete.
         * @param success True if the operation was successful, false otherwise
         */
        void onCallback(boolean success);
    }

    /**
     * Gets a reference to the "events" collection in Firestore.
     * @return A CollectionReference for the "events" collection
     */
    public static CollectionReference getEventCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("events");
    }

    /**
     * Asynchronously retrieves an Event object from the database by event ID.
     * @param eventId The UUID of the event to retrieve
     * @param callback The callback to be invoked with the result
     */
    public static void getEvent(String eventId, GetEventCallback callback) {
        DocumentReference eventRef = getEventCollection().document(eventId);
        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Event event = task.getResult().toObject(Event.class);
                callback.onCallback(event);
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Attaches a real-time snapshot listener to a specific event for live updates.
     * @param eventId The UUID of the event to listen to
     * @param callback The callback to be invoked when the event data changes
     * @return A ListenerRegistration that can be used to stop listening
     */
    public static ListenerRegistration listenToEvent(String eventId, GetEventCallback callback) {
        DocumentReference eventRef = getEventCollection().document(eventId);
        return eventRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                callback.onCallback(null);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Event event = snapshot.toObject(Event.class);
                callback.onCallback(event);
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Adds a new event to the database.
     * @param event The Event object to add
     * @param callback The callback to be invoked with the result
     */
    public static void addEvent(Event event, AddEventCallback callback) {
        getEventCollection().document(event.getUuid().toString()).set(event)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Updates an existing event in the database.
     * @param event The Event object with updated data
     * @param callback The callback to be invoked with the result
     */
    public static void updateEvent(Event event, UpdateEventCallback callback) {
        getEventCollection().document(event.getUuid().toString()).set(event)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Deletes an event from the database.
     * @param eventId The UUID of the event to delete
     * @param callback The callback to be invoked with the result
     */
    public static void deleteEvent(String eventId, DeleteEventCallback callback) {
        getEventCollection().document(eventId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    /**
     * Retrieves all events from the database.
     * @param callback The callback to be invoked with the list of events
     */
    public static void getEvents(GetEventsCallback callback) {
        getEventCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Event> events = task.getResult().toObjects(Event.class);
                callback.onCallback(events);
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Attaches a snapshot listener to the events collection for real-time updates.
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
                List<Event> events = snapshots.toObjects(Event.class);
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
    public static void addToWaitlist(String eventId, String deviceId, AddToWaitlistCallback callback) {
        DocumentReference eventRef = getEventCollection().document(eventId);
        eventRef.update("waitingList", FieldValue.arrayUnion(deviceId))
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }
}
