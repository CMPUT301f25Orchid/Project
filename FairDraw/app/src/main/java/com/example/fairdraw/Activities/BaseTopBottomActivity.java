package com.example.fairdraw.Activities;

import android.content.Intent;
import android.view.View;

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
