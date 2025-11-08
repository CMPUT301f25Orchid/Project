package com.example.fairdraw.Models;

import com.example.fairdraw.Others.EventState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents an event created in the application. An Event contains metadata such as title,
 * description, capacity, registration windows and lists that manage the lottery/waiting
 * list workflow (waitingList, invitedList, enrolledList, cancelledList).
 * <p>
 * The class provides utility methods to draw lottery winners, replace winners when they
 * decline, and accept or cancel winners.
 */
public class Event implements Serializable {

    private String uuid;
    private String title;
    private String description;
    private Integer capacity;
    private Integer waitingListLimit;
    private Date openRegDate;
    private Date closeRegDate;
    private Date regPeriod;
    private Date time;
    private Boolean geolocation;
    private Date startDate;
    private Date endDate;
    private String location; // Mark Location as transient
    private String organizer;
    private Float price;
    private String posterPath;
    private String qrSlug;
    private EventState state = EventState.DRAFT;
    private final Random random = new Random();

    // List of user IDs who are on the waiting list for the lottery.
    private List<String> waitingList;

    // List of user IDs who have won the lottery and been sent an invitation.
    private List<String> invitedList;

    // List of user IDs who have accepted their invitation and are confirmed attendees.
    private List<String> enrolledList;

    // List of user IDs who declined their invitation or cancelled their attendance.
    private List<String> cancelledList;

    /**
     * Creates a new Event with the required fields. A UUID will be generated for the event.
     *
     * @param title      human-readable title of the event
     * @param description brief description of the event
     * @param capacity   maximum number of attendees allowed
     * @param regPeriod  generic registration period date (legacy field)
     * @param time       scheduled time of the event
     * @param location   textual location of the event
     * @param organizer  organizer id or name
     * @param price      ticket price (nullable for free events)
     * @param posterPath path or url to the event poster image
     * @param qrSlug     identifier used for QR check-ins or short-links
     */
    public Event(String title, String description, Integer capacity, Date regPeriod,
                 Date time, String location, String organizer, Float price,
                 String posterPath, String qrSlug) {
        this.uuid = UUID.randomUUID().toString();
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
        this.waitingList = new ArrayList<>();
        this.invitedList = new ArrayList<>();
        this.enrolledList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
    }

    /**
     * Extended constructor supporting optional scheduling, registration windows and geolocation.
     *
     * @param title            title of the event
     * @param description      description of the event
     * @param capacity         maximum attendees
     * @param waitingListLimit maximum waiting list size (nullable)
     * @param regPeriod        generic registration period date
     * @param openRegDate      registration open date
     * @param closeRegDate     registration close date
     * @param time             scheduled time
     * @param startDate        event start date
     * @param endDate          event end date
     * @param location         event location string
     * @param organizer        event organizer id or name
     * @param price            ticket price
     * @param geolocation      whether geolocation is required
     * @param posterPath       poster image path or url
     * @param qrSlug           QR slug for check-in
     */
    public Event(String title, String description, Integer capacity, Integer waitingListLimit, Date regPeriod,
                 Date openRegDate, Date closeRegDate, Date time, Date startDate, Date endDate,
                 String location, String organizer, Float price, Boolean geolocation,
                 String posterPath, String qrSlug){
        this(title, description, capacity, regPeriod, time, location, organizer, price, posterPath, qrSlug);
        this.openRegDate = openRegDate;
        this.closeRegDate = closeRegDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.geolocation = geolocation;
        this.waitingListLimit = waitingListLimit;
    }
    /**
     * No-argument constructor required for Firestore deserialization.
     */
    public Event() {
        // Required for Firestore deserialization
    }



    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    public Integer getWaitingListLimit(){return waitingListLimit;}
    public void setWaitingListLimit(Integer waitingListLimit) {this.waitingListLimit = waitingListLimit;}
    public Date getRegPeriod() {
        return regPeriod;
    }

    public void setRegPeriod(Date regPeriod) {
        this.regPeriod = regPeriod;
    }
    public Date getEventOpenRegDate() {return openRegDate;}
    public void setEventOpenRegDate(Date openRegDate) {this.openRegDate = openRegDate;}
    public Date getEventCloseRegDate() {return closeRegDate;}
    public void setEventCloseRegDate(Date closeRegDate) {this.closeRegDate = closeRegDate;}

    public Boolean getGeolocation() {return geolocation;}
    public void setGeolocation(Boolean geolocation) {this.geolocation = geolocation;}
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    /**
     * Selects new lottery winners from the waiting list to fill the event up to its capacity.
     * Existing invitees are preserved.
     * @return A list containing all invited users (existing and new).
     */
    public List<String> drawLotteryWinners() {
        // Calculate how many new winners we need to draw.
        int spotsToFill = capacity - enrolledList.size() - invitedList.size();

        // If there are no spots to fill, do nothing.
        if (spotsToFill <= 0) {
            return new ArrayList<>(invitedList); // Return the current list of invitees
        }

        // Ensure we don't try to draw more people than are on the waiting list.
        int numToDraw = Math.min(spotsToFill, waitingList.size());

        if (numToDraw > 0) {
            // Shuffle the waiting list to ensure fairness.
            Collections.shuffle(waitingList, random);

            // Take the new winners from the top of the shuffled list.
            List<String> newWinners = waitingList.subList(0, numToDraw);

            // Add the new winners to the invited list.
            invitedList.addAll(newWinners);

            // Remove the new winners from the waiting list.
            // Create a new list for the remaining waiting list participants.
            List<String> remainingWaiting = new ArrayList<>(waitingList.subList(numToDraw, waitingList.size()));
            waitingList.clear();
            waitingList.addAll(remainingWaiting);
        }

        // Return a copy of the complete invited list.
        return new ArrayList<>(invitedList);
    }


    /**
     * Replaces an existing winner with a new one from the waiting list cause the oldWinner
     * rejected their invitation.
     * Removes the oldWinner from the invited list and adds them to the cancelled list.
     * @param oldWinner The winner to be replaced.
     * @return The new winner's deviceId, or null if no replacement is possible.
     */
    public String replaceLotteryWinner(String oldWinner) {
        if (!invitedList.remove(oldWinner)) {
            // The person was not in the invited list; do nothing.
            return null;
        }

        // Find candidates for replacement: people on the waiting list who are not already invited.
        // This prevents picking someone who is already a winner.
        List<String> replacementCandidates = waitingList.stream()
                .filter(person -> !invitedList.contains(person))
                .collect(Collectors.toList());

        if (replacementCandidates.isEmpty()) {
            // No one on the waiting list to replace the winner.
            return null;
        }

        // Pick a random new winner from the valid candidates.
        int randomIndex = random.nextInt(replacementCandidates.size());
        String newWinner = replacementCandidates.get(randomIndex);

        // Update the lists
        invitedList.add(newWinner);
        waitingList.remove(newWinner);

        // Add to cancelled list
        cancelledList.add(oldWinner);

        return newWinner;
    }

    /* This function should collect the device id and move it from the invited list to the
     * cancelled list
     */
    public void cancelLotteryWinner(String deviceId) {
        if (invitedList.remove(deviceId)) {
            cancelledList.add(deviceId);
        }
    }
    /* This function should collect the device id and move it from the invited list to the
     * enrolled list
     */
    public void acceptLotteryWinner(String deviceId) {
        if (invitedList.remove(deviceId)) {
            enrolledList.add(deviceId);
        }
    }
}
