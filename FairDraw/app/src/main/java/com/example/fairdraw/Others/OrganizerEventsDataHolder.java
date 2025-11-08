package com.example.fairdraw.Others;

import android.util.Log;

import com.example.fairdraw.Adapters.EventArrayAdapter;
import com.example.fairdraw.Models.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A singleton class that holds the event data for the organizer.
 */
public final class OrganizerEventsDataHolder {
    private static final String TAG = "OrganizerEventsDataHolder";
    private static final List<Event> events = Collections.synchronizedList(new ArrayList<>());
    private static EventArrayAdapter adapter;

    private OrganizerEventsDataHolder() {}

    /**
     * Get the list of events.
     * @return
     *      A copy of the list of events.
     */
    public static List<Event> getEvents() {
        // return a copy to avoid external modification
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    /**
     * Backwards-compatible: original code expects an ArrayList (getDataList()).
     * Return the internal list reference as an ArrayList for existing callers that rely on direct modification.
     * NOTE: callers who modify the returned list should be careful; prefer getEvents() for a safe copy.
     */
    public static ArrayList<Event> getDataList() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    /**
     * Backwards-compatible setter used by OrganizerMainPage to set the data list reference.
     * We'll copy elements into our internal synchronized list to preserve thread-safety.
     */
    public static void setDataList(ArrayList<Event> dataList) {
        synchronized (events) {
            events.clear();
            if (dataList != null) events.addAll(dataList);
        }
        // notify adapter if available
        if (adapter != null) {
            try { adapter.notifyDataSetChanged(); } catch (Exception ex) { Log.e(TAG, "Error notifying adapter", ex); }
        }
    }

    /**
     * Set the event adapter for the app.
     * @param a This is the adapter that will be used to display the events in the app.
     */
    public static void setAdapter(EventArrayAdapter a) {
        adapter = a;
    }

    /**
     * Backwards-compatible name used in codebase: setEventAdapter
     */
    public static void setEventAdapter(EventArrayAdapter a) {
        setAdapter(a);
    }

    /**
     * Add an event to the data list.
     * @param event The new event that needs to be added
     */
    public static void addEvent(Event event) {
        if (event == null) return;
        events.add(event);

        if (adapter != null) {
            try {
                adapter.notifyDataSetChanged();
            } catch (Exception ex) {
                Log.e(TAG, "Error notifying adapter", ex);
            }
        } else {
            Log.w(TAG, "Adapter is null; event added to list only");
        }
    }

    /**
     * Update an existing event at the provided index. Backwards-compatible with existing callers.
     */
    public static void updateEvent(Event event, int index) {
        if (event == null) return;
        synchronized (events) {
            if (index < 0 || index >= events.size()) {
                Log.w(TAG, "updateEvent: index out of bounds: " + index);
                return;
            }
            events.set(index, event);
        }
        if (adapter != null) {
            try { adapter.notifyDataSetChanged(); } catch (Exception ex) { Log.e(TAG, "Error notifying adapter", ex); }
        }
    }

    /**
     * Clear all events from the data list.
     */
    public static void clear() {
        events.clear();
        if (adapter != null) {
            try { adapter.notifyDataSetChanged(); } catch (Exception ex) { Log.e(TAG, "Error notifying adapter", ex); }
        }
    }
}
