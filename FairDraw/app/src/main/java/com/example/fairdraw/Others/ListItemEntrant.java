package com.example.fairdraw.Others;

/**
 * Simple model representing a list entry for an entrant.
 * <p>
 * Currently only stores the entrant's id and provides getters/setters.
 */
public class ListItemEntrant {
    private String entrantId;

    /**
     * Create a new list item for an entrant id.
     *
     * @param entrantId id of the entrant represented by this list item
     */
    public ListItemEntrant(String entrantId) {
        this.entrantId = entrantId;
    }

    /**
     * Get the entrant id associated with this list item.
     *
     * @return the entrant id string
     */
    public String getEntrantId() {
        return entrantId;
    }

    /**
     * Update the entrant id stored in this list item.
     *
     * @param entrantId new entrant id
     */
    public void setEntrantId(String entrantId) {
        this.entrantId = entrantId;
    }
}
