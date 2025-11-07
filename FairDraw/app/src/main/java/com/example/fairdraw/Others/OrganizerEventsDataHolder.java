package com.example.fairdraw.Others;

import android.widget.Toast;

import com.example.fairdraw.Adapters.EventArrayAdapter;
import com.example.fairdraw.DBs.EventDB;

import com.example.fairdraw.Models.Event;
import java.util.ArrayList;

/**
 * A singleton class that holds and manages event data for organizers in the application.
 * This class maintains a centralized list of events and provides methods to add, update,
 * and remove events while synchronizing with the database and UI adapter.
 */
public class OrganizerEventsDataHolder {
    /** The list of events managed by organizers */
    private static ArrayList<Event> dataList = new ArrayList<>();
    /** The adapter used to display events in the UI */
    public static EventArrayAdapter eventAdapter = null;
    
    /**
     * Gets the list of organizer events.
     * @return The ArrayList of Event objects
     */
    public static ArrayList<Event> getDataList() {
        return dataList;
    }

    /**
     * Sets the data list for organizer events.
     * @param dataList The list of organizer events to be displayed in the app
     */
    public static void setDataList(ArrayList<Event> dataList) {
        OrganizerEventsDataHolder.dataList = dataList;
    }

    /**
     * Sets the event adapter for the app.
     * @param eventAdapter The adapter used to display events in the UI
     */
    public static void setEventAdapter(EventArrayAdapter eventAdapter) {
        OrganizerEventsDataHolder.eventAdapter = eventAdapter;
    }

    /**
     * Gets the event adapter for the app.
     * @return The event adapter used to display events in the UI
     */
    public static EventArrayAdapter getEventAdapter() {
        return eventAdapter;
    }

    /**
     * Adds an event to the data list and persists it to the database.
     * Notifies the adapter to refresh the UI display.
     * @param event The new event to be added
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
     * Updates an event in the data list and persists the changes to the database.
     * Notifies the adapter to refresh the UI display.
     * @param event The event with updated data
     * @param index The index of the event to be updated in the data list
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
     * Removes an event from the data list and deletes it from the database.
     * Notifies the adapter to refresh the UI display.
     * @param event The event to be removed
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
