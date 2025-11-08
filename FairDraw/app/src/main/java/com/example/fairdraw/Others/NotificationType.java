package com.example.fairdraw.Others;

/**
 * Enumeration of notification types that can be sent to entrants.
 * <p>
 * Provides a helper {@link #title(String)} to generate a user-facing title based on
 * the event's title.
 */
public enum NotificationType {
    WIN,
    LOSE,
    WAITLIST,
    REPLACE,
    OTHER;

    /**
     * Generate a short title message for this notification type referencing the event.
     *
     * @param eventTitle the title of the event to include in the notification
     * @return a user-facing notification title string
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
