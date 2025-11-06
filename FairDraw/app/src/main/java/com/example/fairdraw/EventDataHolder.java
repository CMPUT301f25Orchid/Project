package com.example.fairdraw;

import android.widget.Toast;

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
            else{
                Toast.makeText(eventAdapter.getContext(), "Event added successfully", Toast.LENGTH_SHORT).show();
            }
        });
        dataList.add(event);
        eventAdapter.notifyDataSetChanged();
    }
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
