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
 * Represents an event in the FairDraw application.
 * An event is a lottery-based registration system where entrants can join a waiting list,
 * be selected through a lottery draw, accept or decline invitations, and become enrolled attendees.
 * This class implements Serializable for Android intent compatibility and Firestore serialization.
 */
public class Event implements Serializable {

    /** Unique identifier for this event */
    private String uuid;
    /** The title of the event */
    private String title;
    /** Detailed description of the event */
    private String description;
    /** Maximum number of attendees allowed */
    private Integer capacity;
    /** Maximum number of entrants allowed on the waiting list */
    private Integer waitingListLimit;
    /** Date when registration opens */
    private Date openRegDate;
    /** Date when registration closes */
    private Date closeRegDate;
    /** Registration period end date */
    private Date regPeriod;
    /** Event time/date */
    private Date time;
    /** Whether geolocation tracking is enabled for this event */
    private Boolean geolocation;
    /** Event start date */
    private Date startDate;
    /** Event end date */
    private Date endDate;
    /** Physical location of the event */
    private String location;
    /** Device ID of the organizer who created this event */
    private String organizer;
    /** Entry price for the event */
    private Float price;
    /** Path to the event poster image in storage */
    private String posterPath;
    /** Unique slug for QR code deep links */
    private String qrSlug;
    /** Current state of the event (DRAFT, PUBLISHED, CLOSED) */
    private EventState state = EventState.DRAFT;
    /** Random number generator for lottery selection */
    private final Random random = new Random();

    /** List of user IDs who are on the waiting list for the lottery */
    private List<String> waitingList;

    /** List of user IDs who have won the lottery and been sent an invitation */
    private List<String> invitedList;

    /** List of user IDs who have accepted their invitation and are confirmed attendees */
    private List<String> enrolledList;

    /** List of user IDs who declined their invitation or cancelled their attendance */
    private List<String> cancelledList;

    /**
     * Constructs a new Event with basic details.
     * Initializes all participant lists and generates a unique UUID.
     * 
     * @param title The event title
     * @param description The event description
     * @param capacity Maximum number of attendees
     * @param regPeriod Registration period end date
     * @param time Event time/date
     * @param location Physical location of the event
     * @param organizer Device ID of the organizer
     * @param price Entry price
     * @param posterPath Path to poster image
     * @param qrSlug Unique QR code slug
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
     * Constructs a new Event with comprehensive details including dates and geolocation.
     * Calls the basic constructor and sets additional properties.
     * 
     * @param title The event title
     * @param description The event description
     * @param capacity Maximum number of attendees
     * @param waitingListLimit Maximum waiting list size
     * @param regPeriod Registration period end date
     * @param openRegDate Registration opening date
     * @param closeRegDate Registration closing date
     * @param time Event time/date
     * @param startDate Event start date
     * @param endDate Event end date
     * @param location Physical location
     * @param organizer Device ID of organizer
     * @param price Entry price
     * @param geolocation Whether geolocation is enabled
     * @param posterPath Path to poster image
     * @param qrSlug Unique QR code slug
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
     * Default constructor required for Firestore deserialization.
     */
    public Event() {
        // Required for Firestore deserialization
    }



    /**
     * Gets the unique event identifier.
     * @return The event UUID
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the unique event identifier.
     * @param uuid The event UUID
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the event title.
     * @return The event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the event title.
     * @param title The event title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the event description.
     * @return The event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the event description.
     * @param description The event description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the event capacity.
     * @return Maximum number of attendees allowed
     */
    public Integer getCapacity() {
        return capacity;
    }

    /**
     * Sets the event capacity.
     * @param capacity Maximum number of attendees allowed
     */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    /**
     * Gets the waiting list limit.
     * @return Maximum number of entrants allowed on waiting list
     */
    public Integer getWaitingListLimit(){return waitingListLimit;}
    
    /**
     * Sets the waiting list limit.
     * @param waitingListLimit Maximum number of entrants allowed on waiting list
     */
    public void setWaitingListLimit(Integer waitingListLimit) {this.waitingListLimit = waitingListLimit;}
    
    /**
     * Gets the registration period end date.
     * @return The registration period end date
     */
    public Date getRegPeriod() {
        return regPeriod;
    }

    /**
     * Sets the registration period end date.
     * @param regPeriod The registration period end date
     */
    public void setRegPeriod(Date regPeriod) {
        this.regPeriod = regPeriod;
    }
    
    /**
     * Gets the registration opening date.
     * @return The date when registration opens
     */
    public Date getEventOpenRegDate() {return openRegDate;}
    
    /**
     * Sets the registration opening date.
     * @param openRegDate The date when registration opens
     */
    public void setEventOpenRegDate(Date openRegDate) {this.openRegDate = openRegDate;}
    
    /**
     * Gets the registration closing date.
     * @return The date when registration closes
     */
    public Date getEventCloseRegDate() {return closeRegDate;}
    
    /**
     * Sets the registration closing date.
     * @param closeRegDate The date when registration closes
     */
    public void setEventCloseRegDate(Date closeRegDate) {this.closeRegDate = closeRegDate;}

    /**
     * Gets whether geolocation tracking is enabled.
     * @return True if geolocation is enabled, false otherwise
     */
    public Boolean getGeolocation() {return geolocation;}
    
    /**
     * Sets whether geolocation tracking is enabled.
     * @param geolocation True to enable geolocation, false to disable
     */
    public void setGeolocation(Boolean geolocation) {this.geolocation = geolocation;}
    
    /**
     * Gets the event time.
     * @return The event time/date
     */
    public Date getTime() {
        return time;
    }

    /**
     * Sets the event time.
     * @param time The event time/date
     */
    public void setTime(Date time) {
        this.time = time;
    }
    
    /**
     * Gets the event start date.
     * @return The event start date
     */
    public Date getStartDate() {
        return startDate;
    }
    
    /**
     * Sets the event start date.
     * @param startDate The event start date
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    /**
     * Gets the event end date.
     * @return The event end date
     */
    public Date getEndDate() {
        return endDate;
    }
    
    /**
     * Sets the event end date.
     * @param endDate The event end date
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


    /**
     * Gets the event location.
     * @return The physical location of the event
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the event location.
     * @param location The physical location of the event
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the organizer device ID.
     * @return The device ID of the event organizer
     */
    public String getOrganizer() {
        return organizer;
    }

    /**
     * Sets the organizer device ID.
     * @param organizer The device ID of the event organizer
     */
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    /**
     * Gets the event price.
     * @return The entry price for the event
     */
    public Float getPrice() {
        return price;
    }

    /**
     * Sets the event price.
     * @param price The entry price for the event
     */
    public void setPrice(Float price) {
        this.price = price;
    }

    /**
     * Gets the poster path.
     * @return Path to the event poster image in storage
     */
    public String getPosterPath() {
        return posterPath;
    }

    /**
     * Sets the poster path.
     * @param posterPath Path to the event poster image in storage
     */
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    /**
     * Gets the QR code slug.
     * @return Unique slug for QR code deep links
     */
    public String getQrSlug() {
        return qrSlug;
    }

    /**
     * Sets the QR code slug.
     * @param qrSlug Unique slug for QR code deep links
     */
    public void setQrSlug(String qrSlug) {
        this.qrSlug = qrSlug;
    }

    /**
     * Gets the event state.
     * @return Current state of the event (DRAFT, PUBLISHED, or CLOSED)
     */
    public EventState getState() {
        return state;
    }

    /**
     * Sets the event state.
     * @param state New state for the event (DRAFT, PUBLISHED, or CLOSED)
     */
    public void setState(EventState state) {
        this.state = state;
    }

    /**
     * Gets the waiting list.
     * @return List of device IDs on the waiting list
     */
    public List<String> getWaitingList() {
        return waitingList;
    }

    /**
     * Sets the waiting list.
     * @param waitingList List of device IDs on the waiting list
     */
    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList;
    }

    /**
     * Gets the invited list.
     * @return List of device IDs who have been invited
     */
    public List<String> getInvitedList() {
        return invitedList;
    }

    /**
     * Sets the invited list.
     * @param invitedList List of device IDs who have been invited
     */
    public void setInvitedList(List<String> invitedList) {
        this.invitedList = invitedList;
    }

    /**
     * Gets the enrolled list.
     * @return List of device IDs of confirmed attendees
     */
    public List<String> getEnrolledList() {
        return enrolledList;
    }

    /**
     * Sets the enrolled list.
     * @param enrolledList List of device IDs of confirmed attendees
     */
    public void setEnrolledList(List<String> enrolledList) {
        this.enrolledList = enrolledList;
    }

    /**
     * Gets the cancelled list.
     * @return List of device IDs who cancelled or declined
     */
    public List<String> getCancelledList() {
        return cancelledList;
    }

    /**
     * Sets the cancelled list.
     * @param cancelledList List of device IDs who cancelled or declined
     */
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

    /**
     * Cancels a lottery winner's invitation and moves them to the cancelled list.
     * If the device ID is found in the invited list, it is removed and added to the cancelled list.
     * 
     * @param deviceId The device ID of the winner to cancel
     */
    public void cancelLotteryWinner(String deviceId) {
        if (invitedList.remove(deviceId)) {
            cancelledList.add(deviceId);
        }
    }
    
    /**
     * Accepts a lottery winner's invitation and enrolls them as an attendee.
     * If the device ID is found in the invited list, it is removed and added to the enrolled list.
     * 
     * @param deviceId The device ID of the winner accepting the invitation
     */
    public void acceptLotteryWinner(String deviceId) {
        if (invitedList.remove(deviceId)) {
            enrolledList.add(deviceId);
        }
    }
}
