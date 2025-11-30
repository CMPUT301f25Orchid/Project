package com.example.fairdraw.Models;

import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.R;
import androidx.annotation.StringRes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Simple value object to hold an entrant's location when they joined the waitlist.
     * Firestore-friendly (no-arg constructor + getters/setters).
     */
    public static class EntrantLocation implements Serializable {
        private Double lat;
        private Double lng;
        // You can add fields later: city, postalCode, etc.

        // Required no-arg constructor for Firestore
        public EntrantLocation() {}

        public EntrantLocation(Double lat, Double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }
    }

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

    // Map from entrant deviceId -> their location when joining the waitlist
    // (can be extended later to include city/postal code)
    private Map<String, EntrantLocation> waitlistLocations;

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



    /**
     * Returns the unique identifier (UUID) for this event.
     *
     * @return event UUID string, or null if not set
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the unique identifier for this event. Typically used by deserialization
     * or when migrating events between systems.
     *
     * @param uuid UUID string to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns the event title.
     *
     * @return title string, or null if not set
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the event title.
     *
     * @param title title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the event description.
     *
     * @return description string, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the event description.
     *
     * @param description description text to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the event capacity (maximum number of attendees).
     *
     * @return capacity as Integer, or null if unset
     */
    public Integer getCapacity() {
        return capacity;
    }

    /**
     * Sets the maximum number of attendees for this event.
     *
     * @param capacity maximum attendees
     */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    /**
     * Returns the maximum waiting list size for this event, if configured.
     *
     * @return waiting list limit or null if not set
     */
    public Integer getWaitingListLimit(){return waitingListLimit;}
    /**
     * Sets the waiting list limit for this event.
     *
     * @param waitingListLimit integer limit to set (nullable)
     */
    public void setWaitingListLimit(Integer waitingListLimit) {this.waitingListLimit = waitingListLimit;}
    /**
     * Returns the (legacy) registration period date for this event.
     *
     * @return registration period date, or null if unset
     */
    public Date getRegPeriod() {
        return regPeriod;
    }

    /**
     * Sets the (legacy) registration period date for this event.
     *
     * @param regPeriod date to set
     */
    public void setRegPeriod(Date regPeriod) {
        this.regPeriod = regPeriod;
    }
    /**
     * Returns the registration open date.
     *
     * @return open registration date, or null if unset
     */
    public Date getEventOpenRegDate() {return openRegDate;}
    /**
     * Sets the registration open date.
     *
     * @param openRegDate date when registration opens
     */
    public void setEventOpenRegDate(Date openRegDate) {this.openRegDate = openRegDate;}
    /**
     * Returns the registration close date.
     *
     * @return close registration date, or null if unset
     */
    public Date getEventCloseRegDate() {return closeRegDate;}
    /**
     * Sets the registration close date.
     *
     * @param closeRegDate date when registration closes
     */
    public void setEventCloseRegDate(Date closeRegDate) {this.closeRegDate = closeRegDate;}

    /**
     * Returns whether geolocation is required for this event.
     *
     * @return true if geolocation required, false or null otherwise
     */
    public Boolean getGeolocation() {return geolocation;}
    /**
     * Sets the geolocation requirement flag for this event.
     *
     * @param geolocation boolean flag to set
     */
    public void setGeolocation(Boolean geolocation) {this.geolocation = geolocation;}
    /**
     * Returns the scheduled time for the event.
     *
     * @return event time, or null if unset
     */
    public Date getTime() {
        return time;
    }

    /**
     * Sets the scheduled time for the event.
     *
     * @param time date/time to set
     */
    public void setTime(Date time) {
        this.time = time;
    }
    /**
     * Returns the start date for multi-day events.
     *
     * @return start date, or null if unset
     */
    public Date getStartDate() {
        return startDate;
    }
    /**
     * Sets the start date for multi-day events.
     *
     * @param startDate start date to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    /**
     * Returns the end date for multi-day events.
     *
     * @return end date, or null if unset
     */
    public Date getEndDate() {
        return endDate;
    }
    /**
     * Sets the end date for multi-day events.
     *
     * @param endDate end date to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


    /**
     * Returns the textual location of the event.
     *
     * @return location string, or null if unset
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the textual location of the event.
     *
     * @param location location string to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the organizer identifier for this event.
     *
     * @return organizer id or name, or null if unset
     */
    public String getOrganizer() {
        return organizer;
    }

    /**
     * Sets the organizer identifier for this event.
     *
     * @param organizer organizer id or name to set
     */
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    /**
     * Returns the ticket price for this event.
     *
     * @return price as Float, or null for free/unset
     */
    public Float getPrice() {
        return price;
    }

    /**
     * Sets the ticket price for this event.
     *
     * @param price price to set (nullable)
     */
    public void setPrice(Float price) {
        this.price = price;
    }

    /**
     * Returns the poster path or URL for this event.
     *
     * @return poster path string, or null if unset
     */
    public String getPosterPath() {
        return posterPath;
    }

    /**
     * Sets the poster path or URL for this event.
     *
     * @param posterPath poster path or url to set
     */
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    /**
     * Returns the QR slug used for check-ins or short-links.
     *
     * @return qr slug string, or null if unset
     */
    public String getQrSlug() {
        return qrSlug;
    }

    /**
     * Sets the QR slug for the event.
     *
     * @param qrSlug qr slug string to set
     */
    public void setQrSlug(String qrSlug) {
        this.qrSlug = qrSlug;
    }

    /**
     * Returns the current state of the event.
     *
     * @return {@link EventState} enum value
     */
    public EventState getState() {
        return state;
    }

    /**
     * Sets the event state.
     *
     * @param state new state to set
     */
    public void setState(EventState state) {
        this.state = state;
    }

    /**
     * Returns the waiting list of device ids for this event.
     *
     * @return list of device id strings on the waiting list
     */
    public List<String> getWaitingList() {
        return waitingList;
    }

    /**
     * Replaces the waiting list with the provided list. Useful for deserialization.
     *
     * @param waitingList list of device ids
     */
    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList;
    }

    public Map<String, EntrantLocation> getWaitlistLocations() {
        if (waitlistLocations == null) {
            waitlistLocations = new HashMap<>();
        }
        return waitlistLocations;
    }

    public void setWaitlistLocations(Map<String, EntrantLocation> waitlistLocations) {
        this.waitlistLocations = waitlistLocations;
    }

    /**
     * Associate or update an entrant's location by deviceId.
     */
    public void putWaitlistLocation(String deviceId, EntrantLocation location) {
        if (waitlistLocations == null) {
            waitlistLocations = new HashMap<>();
        }
        waitlistLocations.put(deviceId, location);
    }

    /**
     * Get the stored location for a specific entrant on the waitlist, or null if none.
     */
    public EntrantLocation getWaitlistLocation(String deviceId) {
        if (waitlistLocations == null) return null;
        return waitlistLocations.get(deviceId);
    }

    /**
     * Returns the invited list (users who have been sent invitations).
     *
     * @return list of invited device ids
     */
    public List<String> getInvitedList() {
        return invitedList;
    }

    /**
     * Replaces the invited list. Useful for deserialization.
     *
     * @param invitedList list of device ids that are invited
     */
    public void setInvitedList(List<String> invitedList) {
        this.invitedList = invitedList;
    }

    /**
     * Returns the enrolled list (users who accepted and are confirmed attendees).
     *
     * @return list of enrolled device ids
     */
    public List<String> getEnrolledList() {
        return enrolledList;
    }

    /**
     * Replaces the enrolled list. Useful for deserialization.
     *
     * @param enrolledList list of device ids that are enrolled
     */
    public void setEnrolledList(List<String> enrolledList) {
        this.enrolledList = enrolledList;
    }

    /**
     * Returns the cancelled list (users who declined invitations or cancelled attendance).
     *
     * @return list of cancelled device ids
     */
    public List<String> getCancelledList() {
        return cancelledList;
    }

    /**
     * Replaces the cancelled list. Useful for deserialization.
     *
     * @param cancelledList list of device ids that are cancelled
     */
    public void setCancelledList(List<String> cancelledList) {
        this.cancelledList = cancelledList;
    }

    // --- New helper methods to support UI button state/text ---

    /**
     * Ensure internal lists are non-null so callers don't have to check.
     */
    private void ensureListsInitialized() {
        if (waitingList == null) waitingList = new ArrayList<>();
        if (invitedList == null) invitedList = new ArrayList<>();
        if (enrolledList == null) enrolledList = new ArrayList<>();
        if (cancelledList == null) cancelledList = new ArrayList<>();
    }

    /**
     * Returns true if the given deviceId is already enrolled (registered) for the event.
     */
    public boolean isEnrolled(String deviceId) {
        ensureListsInitialized();
        if (deviceId == null) return false;
        return enrolledList.contains(deviceId);
    }

    /**
     * Returns true if the given deviceId has been invited (won the lottery) for the event.
     */
    public boolean isInvited(String deviceId) {
        ensureListsInitialized();
        if (deviceId == null) return false;
        return invitedList.contains(deviceId);
    }

    /**
     * Returns true if the given deviceId is already on the waiting list.
     */
    public boolean isOnWaitingList(String deviceId) {
        ensureListsInitialized();
        if (deviceId == null) return false;
        return waitingList.contains(deviceId);
    }

    /**
     * Returns whether the provided deviceId is allowed to join the lottery waiting list.
     * False if the user is already enrolled, already invited, already on the waiting list,
     * the waiting list limit has been reached (when configured), or the provided id is null.
     */
    public boolean canJoinWaitingList(String deviceId) {
        ensureListsInitialized();
        if (deviceId == null) return false;
        if (isEnrolled(deviceId) || isInvited(deviceId) || isOnWaitingList(deviceId)) return false;
        if (waitingListLimit != null && waitingListLimit >= 0 && waitingList.size() >= waitingListLimit) return false;
        return true;
    }

    /**
     * Returns the appropriate string resource id to display on the Join Lottery Waitlist button for the
     * given deviceId. Resource ids map to localized UI strings.
     */
    @StringRes
    public int getJoinWaitlistButtonText(String deviceId) {
        ensureListsInitialized();
        if (deviceId == null) return R.string.unavailable;
        if (isEnrolled(deviceId)) return R.string.already_registered;
        if (isInvited(deviceId)) return R.string.invitation_sent;
        if (isOnWaitingList(deviceId)) return R.string.on_waitlist;
        if (waitingListLimit != null && waitingListLimit >= 0 && waitingList.size() >= waitingListLimit) return R.string.waitlist_full;
        return R.string.join_lottery_waitlist;
    }

    /**
     * Convenience method for UI: whether the Join Lottery Waitlist button should be enabled
     * for the provided deviceId.
     */
    public boolean isJoinWaitlistButtonEnabled(String deviceId) {
        return canJoinWaitingList(deviceId);
    }

    /**
     * Selects new lottery winners from the waiting list to fill the event up to its capacity.
     * Existing invitees are preserved.
     * @return A list of the new winners' device ids.
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

        List<String> newWinners = Collections.emptyList();
        if (numToDraw > 0) {
            // Shuffle the waiting list to ensure fairness.
            Collections.shuffle(waitingList, random);

            // Take the new winners from the top of the shuffled list.
            newWinners = new ArrayList<>(waitingList.subList(0, numToDraw));

            // Add the new winners to the invited list.
            invitedList.addAll(newWinners);

            // Remove the new winners from the waiting list.
            // Create a new list for the remaining waiting list participants.
            List<String> remainingWaiting = new ArrayList<>(waitingList.subList(numToDraw, waitingList.size()));
            waitingList.clear();
            waitingList.addAll(remainingWaiting);
        }

        // Return a copy of the new winners so we can send notifications.
        return new ArrayList<>(newWinners);
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
     * Cancels an invited winner by moving them from the invited list to the cancelled list.
     * If the provided deviceId is not in the invited list this method does nothing.
     *
     * @param deviceId device id of the invited user to cancel
     */
    public void cancelLotteryWinner(String deviceId) {
        if (invitedList.remove(deviceId)) {
            cancelledList.add(deviceId);
        }
    }

    /**
     * Accepts an invited winner by moving them from the invited list to the enrolled list.
     * If the provided deviceId is not in the invited list this method does nothing.
     *
     * @param deviceId device id of the invited user to accept
     */
    public void acceptLotteryWinner(String deviceId) {
        if (invitedList.remove(deviceId)) {
            enrolledList.add(deviceId);
        }
    }
}
