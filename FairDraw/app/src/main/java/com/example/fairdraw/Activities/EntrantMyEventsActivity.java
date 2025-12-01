package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Models.Entrant;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.Others.EntrantEventStatus;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;

import com.google.android.material.card.MaterialCardView;
import android.graphics.Color;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EntrantMyEventsActivity extends BaseTopBottomActivity {

    private ListenerRegistration reg;
    private LinearLayout historyContainer;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_my_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize shared top and bottom navigation using BaseTopBottomActivity
        initTopNav(BarType.ENTRANT);
        initBottomNav(BarType.ENTRANT, findViewById(R.id.home_bottom_nav_bar));

        BottomNavigationView bottomNav = findViewById(R.id.home_bottom_nav_bar);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.events_activity);
        findViewById(R.id.imgAvatar).setOnClickListener(v -> {
            // Send to ProfileActivity
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        historyContainer = findViewById(R.id.historyContainer);
        inflater = LayoutInflater.from(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String deviceId = DevicePrefsManager.getDeviceId(this);
        if (deviceId == null) {
            // nothing to listen to
            populateEmpty();
            return;
        }

        reg = EntrantDB.listenToEntrant(deviceId, entrant -> {
            if (entrant == null) {
                populateEmpty();
                return;
            }

            List<String> history = entrant.getEventHistory();
            Map<String, Map<String, Object>> statusMap = entrant.getEventHistoryStatus();

            // Print debug info to logcat
            Log.d("EntrantMyEventsActivity", "Fetched entrant event history: " + history);
            Log.d("EntrantMyEventsActivity", "Fetched entrant event history status map: " + statusMap);

            if (history == null || history.isEmpty()) {
                populateEmpty();
                return;
            }

            // Clear previous rows
            historyContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);

            // For each event id, inflate a row and populate it (async fetch of Event)
            for (String eventId : history) {
                final View row = inflater.inflate(R.layout.history_card, historyContainer, false);

                TextView tvTitle = row.findViewById(R.id.tvTitle);
                TextView tvDate = row.findViewById(R.id.tvDate);
                MaterialCardView statusStrip = row.findViewById(R.id.statusStrip);

                // show placeholder until event loads
                tvTitle.setText(eventId);
                tvDate.setText("Loading...");
                statusStrip.setCardBackgroundColor(Color.parseColor("#BDBDBD"));

                historyContainer.addView(row);

                // Fetch event details and update the row when available
                EventDB.getEvent(eventId, event -> {
                    runOnUiThread(() -> {
                        if (event != null) {
                            String title = event.getTitle() == null ? eventId : event.getTitle();
                            tvTitle.setText(title);

                            Map<String, Object> statusObj = statusMap.get(eventId);
                            assert statusObj != null;
                            String entrantStatus = (String) statusObj.get("status");
                            Timestamp lastUpdated = (Timestamp) statusObj.get("lastUpdated");

                            if (lastUpdated != null) {
                                assert entrantStatus != null;
                                tvDate.setText(String.format("%s · %s", entrantStatus.toUpperCase(), lastUpdated.toDate().toString()));
                            } else {
                                tvDate.setText("");
                            }

                            if (entrantStatus != null) {
                                statusStrip.setCardBackgroundColor(
                                        EntrantEventStatus.colorForStatus(this, entrantStatus)
                                );
                            }
                        } else {
                            tvTitle.setText(eventId);
                            tvDate.setText("(event not found)");
                            statusStrip.setCardBackgroundColor(Color.parseColor("#E6E6E6"));
                        }
                    });
                });
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (reg != null) { reg.remove(); reg = null; }
    }

    private void populateEmpty() {
        historyContainer.removeAllViews();
        TextView t = new TextView(this);
        t.setText("No event history");
        t.setTextSize(16);
        t.setPadding(8, 16, 8, 16);
        historyContainer.addView(t);
    }
}