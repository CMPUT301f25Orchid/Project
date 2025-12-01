// OrganizerEventsDataHolder: refreshed to ensure IDE/compiler picks up changes
package com.example.fairdraw.Others;

import android.util.Log;

import com.example.fairdraw.Adapters.EventRecyclerAdapter;
import com.example.fairdraw.Models.Event;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A singleton holder for organizer events. Provides a thread-safe internal list
 * and notifies the attached RecyclerView ListAdapter (if any) through
 * submitList(...) when the data changes.
 */
public final class OrganizerEventsDataHolder {
    private static final String TAG = "OrganizerEventsDataHolder";
    private static final List<Event> events = Collections.synchronizedList(new ArrayList<>());
    private static WeakReference<EventRecyclerAdapter> recyclerAdapterRef = new WeakReference<>(null);

    private OrganizerEventsDataHolder() { }

    public static List<Event> getEvents() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    public static ArrayList<Event> getDataList() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    public static void setDataList(ArrayList<Event> dataList) {
        synchronized (events) {
            events.clear();
            if (dataList != null) events.addAll(dataList);
        }
        notifyAdapters();
    }

    private static void notifyAdapters() {
        EventRecyclerAdapter ra = recyclerAdapterRef.get();
        if (ra != null) {
            try {
                ra.submitList(getEvents());
            } catch (Exception ex) {
                Log.e(TAG, "Error submitting list to recycler adapter", ex);
            }
        }
    }

    public static void setAdapter(EventRecyclerAdapter a) {
        recyclerAdapterRef = new WeakReference<>(a);
        EventRecyclerAdapter ra = recyclerAdapterRef.get();
        if (ra != null) {
            try { ra.submitList(getEvents()); } catch (Exception ex) { Log.e(TAG, "Error submitting initial list to recyclerAdapter", ex); }
        }
    }

    /**
     * Backwards-compatible name used elsewhere in the codebase.
     */
    public static void setEventAdapter(EventRecyclerAdapter a) {
        setAdapter(a);
    }

    public static void addEvent(Event event) {
        if (event == null) return;
        events.add(event);
        EventRecyclerAdapter ra = recyclerAdapterRef.get();
        if (ra != null) {
            try { ra.submitList(getEvents()); } catch (Exception ex) { Log.e(TAG, "Error submitting list to recyclerAdapter", ex); }
        } else {
            Log.w(TAG, "No recycler adapter attached; event added to list only");
        }
    }

    public static void updateEvent(Event event, int index) {
        if (event == null) return;
        synchronized (events) {
            if (index < 0 || index >= events.size()) return;
            events.set(index, event);
        }
        notifyAdapters();
    }

    public static void clear() {
        events.clear();
        notifyAdapters();
    }
}
