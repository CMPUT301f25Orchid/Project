package com.example.fairdraw;

import android.location.Location;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

enum EventState {
    DRAFT {
        @NonNull
        @Override
        public String toString() {
            return "Draft";
        }
    },
    PUBLISHED {
        @NonNull
        @Override
        public String toString() {
            return "Published";
        }
    },
    CLOSED {
        @NonNull
        @Override
        public String toString() {
            return "Closed";
        }
    }
}

public class Event implements Serializable {

    private UUID uuid;
    private String title;
    private String description;
    private Integer capacity;
    private Date regPeriod;
    private Date time;
    private String location; // Mark Location as transient
    private String organizer;
    private Float price;
    private String posterPath;
    private String qrSlug;
    private EventState state;

    // List of user ids that are in waiting list
    private List<String> waitingList;

    // List of user ids that are in invited list
    private List<String> invitedList;

    // List of user ids that are enrolled
    private List<String> enrolledList;

    // List of users who cancelled attending the event
    private List<String> cancelledList;

    public Event(String title, String description, Integer capacity, Date regPeriod, Date time,
                 String location, String organizer, Float price, String posterPath,
                 String qrSlug) {
        this.uuid = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.regPeriod = regPeriod;
        this.time = time;
        this.location = location;
        this.organizer = organizer;
        this.price = price;
        this.posterPath = posterPath;
        this.qrSlug = qrSlug;
        this.state = EventState.DRAFT;
        this.waitingList = new ArrayList<>();
        this.invitedList = new ArrayList<>();
        this.enrolledList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
    }

    public Event() {
        // Required for Firestore deserialization
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Date getRegPeriod() {
        return regPeriod;
    }

    public void setRegPeriod(Date regPeriod) {
        this.regPeriod = regPeriod;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getQrSlug() {
        return qrSlug;
    }

    public void setQrSlug(String qrSlug) {
        this.qrSlug = qrSlug;
    }

    public EventState getState() {
        return state;
    }

    public void setState(EventState state) {
        this.state = state;
    }

    public List<String> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList;
    }

    public List<String> getInvitedList() {
        return invitedList;
    }

    public void setInvitedList(List<String> invitedList) {
        this.invitedList = invitedList;
    }

    public List<String> getEnrolledList() {
        return enrolledList;
    }

    public void setEnrolledList(List<String> enrolledList) {
        this.enrolledList = enrolledList;
    }

    public List<String> getCancelledList() {
        return cancelledList;
    }

    public void setCancelledList(List<String> cancelledList) {
        this.cancelledList = cancelledList;
    }
}
