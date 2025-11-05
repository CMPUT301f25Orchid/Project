package com.example.fairdraw;

import java.util.ArrayList;
import java.util.Date;

public class EventDataHolder {
    private static ArrayList<Event> dataList = new ArrayList<>();
    public static EventArrayAdapter eventAdapter = null;
    public static ArrayList<Event> getDataList() {
        return dataList;
    }
    public static void setDataList(ArrayList<Event> dataList) {
        EventDataHolder.dataList = dataList;
    }
    public static void setEventAdapter(EventArrayAdapter eventAdapter) {
        EventDataHolder.eventAdapter = eventAdapter;
    }
    public static EventArrayAdapter getEventAdapter() {
        return eventAdapter;
    }
    public static void addEvent(Event event) {
        EventDB.addEvent(event, success -> {
            if (!success) {
                System.out.println("Failed to add event");
            }
        });
        dataList.add(event);
        eventAdapter.notifyDataSetChanged();
    }
    public static void updateEvent(Event event , int index) {
        EventDB.updateEvent(event, success -> {
            if (!success) {
                System.out.println("Failed to update event");
            }
        });
        dataList.set(index, event);
        eventAdapter.notifyDataSetChanged();
    }
    public static void removeEvent(Event event) {
        EventDB.deleteEvent(event.getUuid().toString(), success -> {
            if (!success) {
                System.out.println("Failed to delete event");
            }
        });
        dataList.remove(event);
        eventAdapter.notifyDataSetChanged();
    }
}
