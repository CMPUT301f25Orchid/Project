package com.example.fairdraw.Activities;

import android.content.Intent;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Base activity that provides common top/bottom navigation wiring used by entrant screens.
 * <p>
 * Subclasses can call {@link #initBottomNav(BarType, BottomNavigationView)} to attach
 * click handlers which route to other entrant activities (home, scan, notifications, etc.).
 */
public class BaseTopBottomActivity extends AppCompatActivity {
    /**
     * Initialize the bottom navigation click listeners for a given bar type.
     * <p>
     * Currently only the {@link BarType#ENTRANT} bar type is supported. Clicks will start the
     * corresponding activities (EntrantHomeActivity, EntrantScan, EntrantNotificationsActivity).
     *
     * @param barType the type of navigation bar to initialise (e.g. ENTRANT)
     * @param root the inflated BottomNavigationView container that holds the nav item views
     */
    protected void initBottomNav(BarType barType, BottomNavigationView root) {
        if (barType == BarType.ENTRANT) {
            View home = root.findViewById(R.id.home_activity);
            View events = root.findViewById(R.id.events_activity);
            View scan = root.findViewById(R.id.scan_activity);
            View notifications = root.findViewById(R.id.notifications_activity);

            home.setOnClickListener(v -> {
                // Send to EntrantHomeActivity
                Intent intent = new Intent(this, EntrantHomeActivity.class);
                startActivity(intent);
            });

            events.setOnClickListener(v -> {
                // Send to EntrantEventsActivity
                Intent intent = new Intent(this, EntrantMyEventsActivity.class);
                startActivity(intent);
            });

            scan.setOnClickListener(v -> {
                // Send to EntrantScan
                Intent intent = new Intent(this, EntrantScan.class);
                startActivity(intent);
            });

            notifications.setOnClickListener(v -> {
                // Send to EntrantNotificationsActivity
                Intent intent = new Intent(this, EntrantNotificationsActivity.class);
                startActivity(intent);
            });
        } else if (barType == BarType.ORGANIZER) {
            // Organizer bottom nav uses ids: home_activity, create_activity, settings_activity (scan), notifications_activity
            View home = root.findViewById(R.id.home_activity);
            View create = root.findViewById(R.id.create_activity);
            View scan = root.findViewById(R.id.settings_activity);
            View notifications = root.findViewById(R.id.notifications_activity);

            home.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrganizerMainPage.class);
                startActivity(intent);
            });

            create.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateEventPage.class);
                startActivity(intent);
            });

            scan.setOnClickListener(v -> {
                // Leave empty
            });

            notifications.setOnClickListener(v -> {
                // Reuse entrant notifications screen for now
                Intent intent = new Intent(this, EntrantNotificationsActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Initialize the top bar common buttons used across Entrant activities.
     * This wires the Entrant and Organizer buttons.
     *
     * @param currentBar the BarType representing which role the current activity is for. If
     *                   the user clicks the button for the role they're already in, we show a
     *                   toast. Otherwise we navigate to the other role's main activity.
     */
    protected void initTopNav(BarType currentBar) {
         View entrantBtn = findViewById(R.id.btnEntrant);
         if (entrantBtn != null) {
             entrantBtn.setOnClickListener(v -> {
                 if (currentBar == BarType.ENTRANT) {
                    // Already an entrant
                    Snackbar.make(findViewById(android.R.id.content), "You are already an entrant.", Snackbar.LENGTH_SHORT).show();
                 } else {
                     // Navigate to Entrant main/home activity
                     startActivity(new Intent(this, EntrantHomeActivity.class));
                 }
             });
         }

         View organizerBtn = findViewById(R.id.btnOrganizer);
         if (organizerBtn != null) {
             organizerBtn.setOnClickListener(v -> {
                 if (currentBar == BarType.ORGANIZER) {
                    // Already an organizer
                    Snackbar.make(findViewById(android.R.id.content), "You are already an organizer.", Snackbar.LENGTH_SHORT).show();
                 } else {
                     // Navigate to Organizer main page
                     startActivity(new Intent(this, OrganizerMainPage.class));
                 }
             });
         }
     }
 }
