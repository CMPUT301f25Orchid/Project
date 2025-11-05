package com.example.fairdraw;

import java.util.ArrayList;
import java.util.Date;

public class EventDataHolder {
    private static ArrayList<Event> dataList = new ArrayList<>();
    public static ArrayList<Event> getDataList() {
        return dataList;
    }
    public static void setDataList(ArrayList<Event> dataList) {
        EventDataHolder.dataList = dataList;
    }
    public static void addEvent(Event event) {
        dataList.add(event);
    }
    public static void removeEvent(Event event) {
        dataList.remove(event);
    }
}
