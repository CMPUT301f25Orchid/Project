package com.example.fairdraw.Others;

public enum NotificationType {
    WIN,
    LOSE,
    WAITLIST,
    REPLACE;
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
            default:
                return "Notification";
        }
    }

}
