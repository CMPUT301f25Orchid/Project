package com.example.fairdraw.Others;

/**
 * Type of bottom navigation bar used by the app screens.
 * <p>
 * Used to configure which navigation items are shown/active for different user roles.
 * </p>
 * <p>
 * Each enum constant represents a navigation layout for a specific user role:
 * <ul>
 *     <li><code>ENTRANT</code>: Navigation layout for entrants (regular users).</li>
 *     <li><code>ORGANIZER</code>: Navigation layout for organizers (event owners).</li>
 *     <li><code>ADMIN</code>: Navigation layout for admin users.</li>
 * </ul>
 * </p>
 */
public enum BarType {
    /**
     * Navigation layout for entrants (regular users).
     */
    ENTRANT,
    /**
     * Navigation layout for organizers (event owners).
     */
    ORGANIZER,
    /**
     * Navigation layout for admin users.
     */
    ADMIN
}
