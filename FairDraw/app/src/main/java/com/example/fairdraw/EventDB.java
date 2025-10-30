package com.example.fairdraw;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

/**
 * This class serves as a Firestore service provider for Event operations
 * */
public class EventDB {
    public static CollectionReference getEventCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("events");
    }

    public static Event getEvent(String eventId) {
        DocumentReference eventRef = EventDB.getEventCollection().document(eventId);

        // Check if the event exists in the database
        Task<DocumentSnapshot> task = eventRef.get();
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            return document.toObject(Event.class);
        }
        else
        {
            return null;
        }
    }

    public static Boolean addEvent(Event event) {
        // Add the document with the uuid specified
        Task<Void> task = EventDB.getEventCollection().document(event.getUuid().toString())
                .set(event);
        return task.isSuccessful();
    }

    public static Boolean updateEvent(Event event) {
        DocumentReference eventRef = EventDB.getEventCollection().document(event.getUuid().toString());
        return eventRef.set(event).isSuccessful();
    }

    public static Boolean deleteEvent(String eventId) {
        DocumentReference eventRef = EventDB.getEventCollection().document(eventId);
        return eventRef.delete().isSuccessful();
    }

    public static List<Event> getEvents() {
        Task<QuerySnapshot> task = EventDB.getEventCollection().get();

        if (task.isSuccessful()) {
            return task.getResult().toObjects(Event.class);
        } else {
            return null;
        }
    }
}
