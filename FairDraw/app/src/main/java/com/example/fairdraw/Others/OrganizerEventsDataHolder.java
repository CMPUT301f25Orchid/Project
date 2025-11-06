package com.example.fairdraw.Others;

import android.widget.Toast;

import com.example.fairdraw.Adapters.EventArrayAdapter;
import com.example.fairdraw.DBs.EventDB;

import com.example.fairdraw.Models.Event;
import java.util.ArrayList;

/**
 * A singleton class that holds the data for the app.
 */
public class OrganizerEventsDataHolder {
    private static ArrayList<Event> dataList = new ArrayList<>();
    public static EventArrayAdapter eventAdapter = null;
    public static ArrayList<Event> getDataList() {
        return dataList;
    }

    /**
     * Set the data list for the app.
     * @param dataList
     *      This is the list of organizer events that will be displayed in the app.
     */
    public static void setDataList(ArrayList<Event> dataList) {
        OrganizerEventsDataHolder.dataList = dataList;
    }

    /**
     * Set the event adapter for the app.
     * @param eventAdapter
     *      This is the adapter that will be used to display the events in the app.
     */
    public static void setEventAdapter(EventArrayAdapter eventAdapter) {
        OrganizerEventsDataHolder.eventAdapter = eventAdapter;
    }

    /**
     * Get the event adapter for the app.
     * @return
     *      Returns the event adapter that will be used to display the events in the app.
     */
    public static EventArrayAdapter getEventAdapter() {
        return eventAdapter;
    }

    /**
     * Add an event to the data list and add it to the database.
     * @param event
     *      The new event that needs to be added
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
     * Update an event in the data list and update it in the database.
     * @param event
     *      The event that is replacing the old event
     * @param index
     *      The index of the event that needs to be updated
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
     * Remove an event from the data list, remove it from the database, and update the event display.
     * @param event
     *      The event that needs to be removed
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
