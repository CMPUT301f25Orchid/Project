package com.example.fairdraw.Activities;

import android.content.Intent;
import android.view.View;

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
            View scan = root.findViewById(R.id.settings_activity);
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
