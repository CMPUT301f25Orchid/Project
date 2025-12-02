package com.example.fairdraw.Activities;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

            home.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrganizerMainPage.class);
                startActivity(intent);
            });

            create.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateEventPage.class);
                startActivity(intent);
            });

        } else if (barType == BarType.ADMIN) {
            // Admin bottom nav uses ids: home_activity, create_activity, settings_activity (scan), notifications_activity
            View adminEvents = root.findViewById(R.id.admin_events_activity);
            View profiles = root.findViewById(R.id.profiles_activity);
            View pictures = root.findViewById(R.id.pictures_activity);
            View logs = root.findViewById(R.id.logs_activity);

            adminEvents.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminEventsPage.class);
                startActivity(intent);
            });

            profiles.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminManageProfiles.class);
                startActivity(intent);
            });

            pictures.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminPicturesPage.class);
                startActivity(intent);
            });

            logs.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminNotificationLogActivity.class);
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
        // Highlight correct role
        highlightSelectedRole(currentBar);

        View entrantBtn = findViewById(R.id.btnEntrant);
        if (entrantBtn != null) {
            entrantBtn.setOnClickListener(v -> {
                if (currentBar == BarType.ENTRANT) {
                    Snackbar.make(findViewById(android.R.id.content),
                            "You are already an entrant.", Snackbar.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, EntrantHomeActivity.class));
                }
            });
        }

        View organizerBtn = findViewById(R.id.btnOrganizer);
        if (organizerBtn != null) {
            organizerBtn.setOnClickListener(v -> {
                if (currentBar == BarType.ORGANIZER) {
                    Snackbar.make(findViewById(android.R.id.content),
                            "You are already an organizer.", Snackbar.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, OrganizerMainPage.class));
                }
            });
        }

        View adminBtn = findViewById(R.id.btnAdmin);
        if (adminBtn != null) {
            adminBtn.setOnClickListener(v -> {
                if (currentBar == BarType.ADMIN) {
                    Snackbar.make(findViewById(android.R.id.content),
                            "You are already an admin.", Snackbar.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, AdminEventsPage.class));
                }
            });
        }

        ShapeableImageView userProfileButton = findViewById(R.id.imgAvatar); // Use the correct ID 'imgAvatar'
        String deviceId = DevicePrefsManager.getDeviceId(this);

        // Load user avatar from Firestore ---
        if (deviceId != null && !deviceId.isEmpty()) {
            UserDB.getUserOrNull(deviceId, (user, e) -> {
                if (user != null && user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                    // User has a profile picture, load it
                    runOnUiThread(() -> {
                        Glide.with(this)
                                .load(Uri.parse(user.getProfilePicture()))
                                .circleCrop()
                                .into(userProfileButton);
                    });
                }
            });
        }

        userProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
     }

    private void highlightSelectedRole(BarType currentBar) {
        MaterialButton entrant = findViewById(R.id.btnEntrant);
        MaterialButton organizer = findViewById(R.id.btnOrganizer);
        MaterialButton admin = findViewById(R.id.btnAdmin);

        if (entrant == null || organizer == null || admin == null) return;

        // reset all
        entrant.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        entrant.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));

        organizer.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        organizer.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));

        admin.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        admin.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));

        // highlight selected
        MaterialButton selected = null;
        switch (currentBar) {
            case ENTRANT:
                selected = entrant;
                break;
            case ORGANIZER:
                selected = organizer;
                break;
            case ADMIN:
                selected = admin;
                break;
        }

        if (selected != null) {
            selected.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.brand_blue));
            selected.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }


}


