package com.example.fairdraw.Activities;

import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Base activity class that provides common functionality for activities with top and bottom navigation bars.
 * This class initializes click listeners for navigation items and handles navigation between different
 * sections of the app based on the user's role (Entrant, Organizer, or Admin).
 */
public class BaseTopBottomActivity extends AppCompatActivity {
    /**
     * Initializes the bottom navigation bar with click listeners based on the user's role.
     * 
     * @param barType The type of navigation bar (ENTRANT, ORGANIZER, or ADMIN)
     * @param root The BottomNavigationView to initialize
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
        }
    }
}
