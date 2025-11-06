package com.example.fairdraw.DBs;

import com.example.fairdraw.Models.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * This class serves as a Firestore service provider for Event operations
 */
public class EventDB {

    /**
     * Callback for when an Event is retrieved from the database.
     */
    public interface GetEventCallback {
        void onCallback(Event event);
    }

    /**
     * Callback for when a list of Events is retrieved from the database.
     */
    public interface GetEventsCallback {
        void onCallback(List<Event> events);
    }

    /**
     * Callback for when an Event is added to the database.
     */
    public interface AddEventCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Event is updated in the database.
     */
    public interface UpdateEventCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for when an Event is deleted from the database.
     */
    public interface DeleteEventCallback {
        void onCallback(boolean success);
    }

    public static CollectionReference getEventCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("events");
    }

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

    public static void addEvent(Event event, AddEventCallback callback) {
        getEventCollection().document(event.getUuid().toString()).set(event)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void updateEvent(Event event, UpdateEventCallback callback) {
        getEventCollection().document(event.getUuid().toString()).set(event)
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

    public static void deleteEvent(String eventId, DeleteEventCallback callback) {
        getEventCollection().document(eventId).delete()
                .addOnCompleteListener(task -> callback.onCallback(task.isSuccessful()));
    }

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
}
