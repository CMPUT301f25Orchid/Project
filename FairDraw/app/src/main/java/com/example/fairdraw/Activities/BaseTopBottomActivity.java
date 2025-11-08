package com.example.fairdraw.Activities;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** This is base class gives the functionality to initialize the onclick listener for the common
 * top and bottom navigation bar in each activity that has them.
 */
public class BaseTopBottomActivity extends AppCompatActivity {
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
                // TODO
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
                // Reuse the entrant scanner for now
                Intent intent = new Intent(this, EntrantScan.class);
                startActivity(intent);
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
                    Toast.makeText(this, "You are already an entrant.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "You are already an organizer.", Toast.LENGTH_SHORT).show();
                } else {
                    // Navigate to Organizer main page
                    startActivity(new Intent(this, OrganizerMainPage.class));
                }
            });
        }
    }
}
