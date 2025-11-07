package com.example.fairdraw.Others;

/**
 * Represents the types of notifications that can be sent to entrants in the FairDraw application.
 * Each notification type has an associated title message that is displayed to the user.
 */
public enum NotificationType {
    /** Notification that the entrant won the lottery and received an invitation */
    WIN,
    /** Notification that the entrant lost the lottery and was not selected */
    LOSE,
    /** Notification that the entrant was added to the waitlist */
    WAITLIST,
    /** Notification that the entrant replaced another winner who declined */
    REPLACE,
    /** Generic notification type for other purposes */
    OTHER;

    /**
     * Generates a user-friendly notification title based on the type and event title.
     * 
     * @param eventTitle The title of the event this notification is about
     * @return A formatted notification message string
     */
    public String title(String eventTitle) {
        switch (this) {
            case WIN:
                return "Congratulations! You won " + eventTitle + "!";
            case LOSE:
                return "Sorry! You lost " + eventTitle + ".";
            case WAITLIST:
                return "You have been added to the waitlist for " + eventTitle + ".";
            case REPLACE:
                return "You have been replaced in the waitlist for " + eventTitle + ".";
            case OTHER:
                return "Notification";
            default:
                return "Notification";
        }
    }

}
