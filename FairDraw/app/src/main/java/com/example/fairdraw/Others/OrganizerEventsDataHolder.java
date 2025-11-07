package com.example.fairdraw.Others;

import android.widget.Toast;

import com.example.fairdraw.Adapters.EventArrayAdapter;
import com.example.fairdraw.DBs.EventDB;

import com.example.fairdraw.Models.Event;
import java.util.ArrayList;

/**
 * A singleton-like holder for the organizer's events and the shared adapter used to
 * display them in the UI.
 * <p>
 * Provides simple utility methods to add, update, and remove events while keeping
 * the local list, the UI adapter, and the remote database in sync.
 */
public class OrganizerEventsDataHolder {
    private static ArrayList<Event> dataList = new ArrayList<>();
    public static EventArrayAdapter eventAdapter = null;

    /**
     * Returns the current in-memory list of events for the organizer.
     *
     * @return mutable list of Event objects backing the UI
     */
    public static ArrayList<Event> getDataList() {
        return dataList;
    }

    /**
     * Set the data list for the app. Replaces any existing list reference.
     *
     * @param dataList This is the list of organizer events that will be displayed in the app.
     */
    public static void setDataList(ArrayList<Event> dataList) {
        OrganizerEventsDataHolder.dataList = dataList;
    }

    /**
     * Set the EventArrayAdapter used to display the events in the UI.
     *
     * @param eventAdapter adapter instance to use for rendering the list
     */
    public static void setEventAdapter(EventArrayAdapter eventAdapter) {
        OrganizerEventsDataHolder.eventAdapter = eventAdapter;
    }

    /**
     * Get the currently set EventArrayAdapter used by the UI.
     *
     * @return the EventArrayAdapter or null if none has been set
     */
    public static EventArrayAdapter getEventAdapter() {
        return eventAdapter;
    }

    /**
     * Add an event to the data list and persist it to Firestore.
     * <p>
     * The method updates the remote database asynchronously and updates the
     * local list and the adapter immediately. Callers should ensure an adapter
     * is set before invoking this method to avoid NPEs.
     *
     * @param event The new event that needs to be added
     */
    public static void addEvent(Event event) {
        EventDB.addEvent(event, success -> {
            if (!success) {
                System.out.println("Failed to add event");
            }
            else{
                Toast.makeText(eventAdapter.getContext(), "Event added successfully", Toast.LENGTH_SHORT).show();
            }
        });
        dataList.add(event);
        eventAdapter.notifyDataSetChanged();
    }

    /**
     * Update an event in the data list and persist the change to Firestore.
     *
     * @param event The event that is replacing the old event
     * @param index The index of the event that needs to be updated
     */
    public static void updateEvent(Event event , int index) {
        EventDB.updateEvent(event, success -> {
            if (!success) {
                System.out.println("Failed to update event");
            } else{
                Toast.makeText(eventAdapter.getContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
        dataList.set(index, event);
        eventAdapter.notifyDataSetChanged();
    }

    /**
     * Remove an event from the data list, delete it from Firestore, and update the UI.
     *
     * @param event The event that needs to be removed
     */
    public static void removeEvent(Event event) {
        EventDB.deleteEvent(event.getUuid().toString(), success -> {
            if (!success) {
                System.out.println("Failed to delete event");
            }else{
                Toast.makeText(eventAdapter.getContext(), "Event successfully removed", Toast.LENGTH_SHORT).show();
            }
        });
        dataList.remove(event);
        eventAdapter.notifyDataSetChanged();
    }
}
