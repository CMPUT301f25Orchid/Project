package com.example.fairdraw.Others;

/**
 * Represents a list item wrapper for an entrant ID.
 * This class is used in list views to display entrant information.
 */
public class ListItemEntrant {
    /** The unique ID of the entrant */
    private String entrantId;

    /**
     * Constructs a new ListItemEntrant with the specified entrant ID.
     * 
     * @param entrantId The unique ID of the entrant
     */
    public ListItemEntrant(String entrantId) {
        this.entrantId = entrantId;
    }

    /**
     * Gets the entrant ID.
     * @return The unique ID of the entrant
     */
    public String getEntrantId() {
        return entrantId;
    }

    /**
     * Sets the entrant ID.
     * @param entrantId The unique ID of the entrant
     */
    public void setEntrantId(String entrantId) {
        this.entrantId = entrantId;
    }
}
