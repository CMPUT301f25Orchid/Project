package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Fragments.QrCodeFragment;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
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

        // grab views
        tvTitle     = findViewById(R.id.tvTitle);
        tvSummary   = findViewById(R.id.tvSummary);
        heroImage   = findViewById(R.id.heroImage);
        btnViewQrCode = findViewById(R.id.btnViewQrCode);
        btnWaitlist = findViewById(R.id.btnWaitlist);

        // 1) get the eventId from intent
        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2) fetch Event and bind, and setup listener for realtime updates
        eventListener = EventDB.listenToEvent(eventId, event -> {
            if (event == null) {
                Toast.makeText(this, "Unable to load event.", Toast.LENGTH_LONG).show();
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

            btnViewQrCode.setOnClickListener(v -> {
                QrCodeFragment qrFragment = QrCodeFragment.newInstance(eventId,event.getTitle());
                qrFragment.show(getSupportFragmentManager(), "QrCodeFragment");
            });
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
                        Toast.makeText(this, "Removed from waitlist", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to remove from waitlist", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                // Add to waitlist
                EventDB.addToWaitlist(eventId, DevicePrefsManager.getDeviceId(this), success -> {
                    if (success) {
                        onWaitlist[0] = true;
                        // After adding, use cached event if available to determine label/state.
                        if (currentEvent != null) {
                            // The cached event won't yet reflect the addition; present a leave affordance.
                            btnWaitlist.setText(getString(R.string.leave_lottery_waitlist));
                            btnWaitlist.setEnabled(true);
                        } else {
                            btnWaitlist.setText(getString(R.string.leave_lottery_waitlist));
                            btnWaitlist.setEnabled(true);
                        }
                        Toast.makeText(this, "Added to waitlist", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add to waitlist", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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
                Toast.makeText(this, "Failed to load event poster", Toast.LENGTH_SHORT).show();
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
