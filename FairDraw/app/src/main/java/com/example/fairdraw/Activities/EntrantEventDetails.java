package com.example.fairdraw.Activities;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Fragments.QrCodeFragment;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.GeoService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity that displays detailed information for a single Event to an entrant.
 * <p>
 * It listens to real-time updates for the event document and allows the user to
 * join or leave the lottery/waitlist for the event.
 */
public class EntrantEventDetails extends BaseTopBottomActivity {

    // --- Views ---
    private TextView tvTitle;
    private TextView tvSummary;
    private ImageView heroImage;
    private MaterialButton btnViewQrCode;
    private MaterialButton btnWaitlist;

    // ---- Date & money formatters ----
    private final SimpleDateFormat dayFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final NumberFormat moneyFmt = NumberFormat.getCurrencyInstance(Locale.getDefault());

    private ListenerRegistration eventListener;
    FirebaseImageStorageService storageService;
    final boolean[] onWaitlist = {false};
    // Keep a reference to the last received Event so click handlers can consult helper methods immediately
    private Event currentEvent;
    private GeoService geoService;
    private String eventId;
    private boolean pendingJoinAfterPermission = false;


    private void bindCell(int includeId, int iconRes, String title, String subtitle) {
        View cell = findViewById(includeId);
        ((ImageView) cell.findViewById(R.id.icon)).setImageResource(iconRes);
        ((TextView) cell.findViewById(R.id.title)).setText(title);
        ((TextView) cell.findViewById(R.id.subtitle)).setText(subtitle);
    }

    /**
     * Activity lifecycle entry point. Reads the event_id extra, subscribes to realtime updates
     * and initializes UI controls including the waitlist button.
     *
     * Expected Intent extras:
     *  - "event_id": String UUID of the event document
     *
     * @param savedInstanceState saved state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_event_details);

        // Initialize common top and bottom navigation using BaseTopBottomActivity helpers
        initTopNav(com.example.fairdraw.Others.BarType.ENTRANT);
        initBottomNav(com.example.fairdraw.Others.BarType.ENTRANT, findViewById(R.id.home_bottom_nav_bar));

        // Ensure the correct bottom tab is selected (events/details)
        BottomNavigationView bottomNav = findViewById(R.id.home_bottom_nav_bar);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.events_activity);

        geoService = new GeoService(this);

        // grab views
        tvTitle     = findViewById(R.id.tvTitle);
        tvSummary   = findViewById(R.id.tvSummary);
        heroImage   = findViewById(R.id.heroImage);
        btnViewQrCode = findViewById(R.id.btnViewQrCode);
        btnWaitlist = findViewById(R.id.btnWaitlist);

        // 1) get the eventId from intent
        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null || eventId.trim().isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Missing event id", Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2) fetch Event and bind, and setup listener for realtime updates
        eventListener = EventDB.listenToEvent(eventId, event -> {
            if (event == null) {
                Snackbar.make(findViewById(android.R.id.content), "Unable to load event.", Snackbar.LENGTH_LONG).show();
                return;
            }
            // Cache the latest event for use in click callbacks
            currentEvent = event;
            // Determine device id and use Event helper methods to set button state/text
            String deviceId = DevicePrefsManager.getDeviceId(this);

            // Track whether we're on the waiting list so the click handler can toggle correctly
            onWaitlist[0] = event.isOnWaitingList(deviceId);

            // If user is on the waiting list we want to allow them to leave, so enable the button in that case.
            if (onWaitlist[0]) {
                // When on the waiting list we present a leave action (enabled)
                btnWaitlist.setText(getString(R.string.leave_lottery_waitlist));
                btnWaitlist.setEnabled(true);
            } else {
                // Use the Event helpers for other states (enrolled/invited/full/available)
                int resId = event.getJoinWaitlistButtonText(deviceId);
                btnWaitlist.setText(getString(resId));
                btnWaitlist.setEnabled(event.isJoinWaitlistButtonEnabled(deviceId));
            }

            bindEvent(event);

            setupQrButton(event);
        });

        storageService = new FirebaseImageStorageService();

        // 3) setup waitlist button
        btnWaitlist.setOnClickListener(v -> {
            // Guard: don't run if button is disabled (e.g., already enrolled/invited and not allowed)
            if (!btnWaitlist.isEnabled()) return;

            if (onWaitlist[0]) {
                // Remove from waitlist
                EventDB.removeFromWaitlist(eventId, DevicePrefsManager.getDeviceId(this), success -> {
                    if (success) {
                        onWaitlist[0] = false;
                        // After removal, try to use the cached event to determine the correct label/state
                        String deviceId = DevicePrefsManager.getDeviceId(this);
                        if (currentEvent != null) {
                            int newRes = currentEvent.getJoinWaitlistButtonText(deviceId);
                            btnWaitlist.setText(getString(newRes));
                            btnWaitlist.setEnabled(currentEvent.isJoinWaitlistButtonEnabled(deviceId));
                        } else {
                            // Fallback until realtime listener refreshes
                            btnWaitlist.setText(getString(R.string.join_lottery_waitlist));
                            btnWaitlist.setEnabled(true);
                        }
                        Snackbar.make(findViewById(android.R.id.content), "Removed from waitlist", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to remove from waitlist", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                // Add to waitlist
                handleJoinWaitlistClick();

            }
        });
    }

    /**
     * Called when the user taps "Join waitlist".
     * If the event requires geolocation, show a consent dialog.
     * Otherwise, just join directly.
     */
    private void handleJoinWaitlistClick() {
        if (currentEvent != null && Boolean.TRUE.equals(currentEvent.getGeolocation())) {
            // This event requires geolocation -> show consent dialog
            showGeoConsentDialog();
        } else {
            // No geolocation requirement -> just join without location
            joinWaitlistInternal();
        }
    }

    /**
     * Small popup to inform the user that this event uses their location.
     */
    private void showGeoConsentDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Use your location?")
                .setMessage("This event uses your approximate location to help the organizer " +
                        "understand where entrants are joining from. Your location will be " +
                        "stored with your waitlist entry.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Agree", (dialog, which) -> {
                    dialog.dismiss();
                    startJoinWithLocation();
                })
                .show();
    }

    /**
     * Ensure we have permission and then fetch location via GeoService.
     */
    private void startJoinWithLocation() {
        if (!geoService.hasLocationPermission()) {
            // Ask for permission, and mark that we should continue join afterwards.
            pendingJoinAfterPermission = true;
            geoService.requestLocationPermission(this);
            return;
        }

        // Permission granted, get last known location
        geoService.getLastKnownLocation(new GeoService.GeoCallback() {
            @Override
            public void onLocationResult(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                Log.d("EntrantEventDetails", "Location for waitlist join: " + lat + ", " + lng);

                // Build the Event.EntrantLocation object and join
                Event.EntrantLocation loc = new Event.EntrantLocation(lat, lng);

                joinWaitlistInternal(loc);
            }

            @Override
            public void onLocationError(String message) {
                Log.w("EntrantEventDetails", "Location error: " + message);
                // Do NOT join without location when event requires geolocation. Offer retry instead.
                Snackbar.make(findViewById(android.R.id.content),
                        "Could not get location. This event requires geolocation so you can't join without it.",
                        Snackbar.LENGTH_LONG).show();

                new MaterialAlertDialogBuilder(EntrantEventDetails.this)
                        .setTitle("Location unavailable")
                        .setMessage("We couldn't obtain your location. You can retry or cancel joining the waitlist.")
                        .setPositiveButton("Retry", (dialog, which) -> {
                            dialog.dismiss();
                            // Try again (will request permission if needed)
                            startJoinWithLocation();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
    }

    /**
     * The original "add to waitlist" logic, extracted into a helper.
     */
    private void joinWaitlistInternal() {
        joinWaitlistInternal(null);
    }

    private void joinWaitlistInternal(@Nullable Event.EntrantLocation location) {
        String deviceId = DevicePrefsManager.getDeviceId(this);

        // If the event requires geolocation, disallow joining without a provided location.
        if (currentEvent != null && Boolean.TRUE.equals(currentEvent.getGeolocation()) && location == null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Location required")
                    .setMessage("This event requires that you share your location to join. Please agree to share your location to continue.")
                    .setPositiveButton("Retry", (dialog, which) -> {
                        dialog.dismiss();
                        showGeoConsentDialog();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        // New overloaded DB method that accepts a location
        EventDB.addToWaitlist(eventId, deviceId, location, success -> {
            if (success) {
                onWaitlist[0] = true;
                btnWaitlist.setText(getString(R.string.leave_lottery_waitlist));
                btnWaitlist.setEnabled(true);
                Snackbar.make(findViewById(android.R.id.content), "Added to waitlist", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Failed to add to waitlist", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == GeoService.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingJoinAfterPermission) {
                    pendingJoinAfterPermission = false;
                    // Now that we have permission, actually grab location and join
                    startJoinWithLocation();
                }
            } else {
                // Permission denied
                if (pendingJoinAfterPermission) {
                    pendingJoinAfterPermission = false;
                }
                Snackbar.make(findViewById(android.R.id.content),
                        "Location permission is required for this event's geolocation feature.",
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    private static String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }

    private static int nz(@Nullable Integer i) {
        return i == null ? 0 : i;
    }

    void setupQrButton(Event event) {
        btnViewQrCode.setOnClickListener(v -> {
            QrCodeFragment qrFragment =
                    QrCodeFragment.newInstance(eventId, event.getTitle());
            qrFragment.show(getSupportFragmentManager(), "QrCodeFragment");
        });
    }

    private void bindEvent(Event e) {
        // title / summary
        setTextOrHide(tvTitle,  safe(e.getTitle(), "Event"));
        setTextOrHide(tvSummary, safe(e.getDescription(), ""));

        // cells (use the same include IDs you added)
        bindCell(R.id.cell_location,
                R.drawable.ic_location_on_24,
                "Location",
                safe(e.getLocation(), "—"));

        bindCell(R.id.cell_geolocation,
                R.drawable.ic_place_24,
                "Geolocation Requirement",
                (e.getGeolocation() != null && e.getGeolocation()) ? "Required" : "Not Required");

        bindCell(R.id.cell_capacity,
                R.drawable.ic_people_24,
                "Capacity & Waitlist",
                buildCapacityText(e));

        bindCell(R.id.cell_reg_period,
                R.drawable.ic_event_24,
                "Registration Period",
                buildDateRange(e.getEventOpenRegDate(), e.getEventCloseRegDate()));

        bindCell(R.id.cell_price,
                R.drawable.ic_attach_money_24,
                "Price",
                buildPrice(e));

        bindCell(R.id.cell_organizer,
                R.drawable.ic_person_24,
                "Organizer",
                safe(e.getOrganizer(), "—"));

        bindCell(R.id.cell_schedule,
                R.drawable.ic_schedule_24,
                "Schedule",
                safe((e.getTime() == null ? "—" : e.getTime().toString()), "—"));

        // Add a placeholder bitmap saying loading for event poster
        heroImage.setImageResource(R.drawable.loading);

        // Fetch the event poster
        storageService.getEventPosterDownloadUrl(e.getUuid()).addOnCompleteListener(urlTask -> {
            if (urlTask.isSuccessful()) {
                String url = urlTask.getResult().toString();
                Glide.with(this)
                        .load(url)
                        .into(heroImage);
            } else {
                // Handle error
                Snackbar.make(findViewById(android.R.id.content), "Failed to load event poster", Snackbar.LENGTH_SHORT).show();
                Log.e("EntrantEventDetails", "Error loading event poster", urlTask.getException());
            }
        });
    }

    private String buildCapacityText(Event e) {
        // expected fields: totalSpots, waitlistCount, confirmedCount
        // fallbacks to 0 if null
        int total  = nz(e.getCapacity());
        int wait   = e.getWaitingList().size();
        int conf   = e.getEnrolledList().size();
        int avail  = Math.max(total - conf, 0);

        return avail + " spots • " + wait + " on waitlist • " + conf + " confirmed";
    }

    private String buildDateRange(Object start, Object end) {
        Date s = toDate(start);
        Date t = toDate(end);
        if (s == null && t == null) return "—";
        if (s == null) return "until " + dayFmt.format(t);
        if (t == null) return dayFmt.format(s);
        return dayFmt.format(s) + " to " + dayFmt.format(t);
    }

    /**
     * Accepts java.util.Date, com.google.firebase.Timestamp, or null.
     */
    @Nullable
    private static Date toDate(@Nullable Object obj) {
        if (obj == null) return null;
        if (obj instanceof Date) return (Date) obj;
        try {
            // Firestore Timestamp
            com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) obj;
            return ts.toDate();
        } catch (Throwable ignore) {
            return null;
        }
    }

    private String buildPrice(Event e) {
        // handle numeric or string
        if (e.getPrice() == null) return "—";
        try {
            return moneyFmt.format(e.getPrice().doubleValue());
        } catch (Exception ex) {
            return e.getPrice().toString();
        }
    }

    private void setTextOrHide(TextView textView, String string) {
        if (string == null || string.trim().isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(string);
        }
    }
}
