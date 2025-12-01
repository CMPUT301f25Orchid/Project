package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.ListenerRegistration;

import com.google.android.material.card.MaterialCardView;
import android.graphics.Color;

import java.util.Collections;
import java.util.List;

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

        reg = EntrantDB.getEntrantCollection()
                .document(deviceId)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null || !snap.exists()) {
                        populateEmpty();
                        return;
                    }

                    Entrant entrant = snap.toObject(Entrant.class);
                    if (entrant == null) {
                        populateEmpty();
                        return;
                    }

                    List<String> history = entrant.getEventHistory();
                    if (history == null || history.isEmpty()) {
                        populateEmpty();
                        return;
                    }

                    // Clear previous rows
                    historyContainer.removeAllViews();

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
                                    if (event.getTime() != null) {
                                        tvDate.setText("Date · " + event.getTime().toString());
                                    } else {
                                        tvDate.setText("");
                                    }

                                    // Simple status color mapping
                                    if (event.getState() != null) {
                                        switch (event.getState()) {
                                            case PUBLISHED:
                                                statusStrip.setCardBackgroundColor(Color.parseColor("#BFF2A6"));
                                                break;
                                            case CLOSED:
                                                statusStrip.setCardBackgroundColor(Color.parseColor("#F2A6A6"));
                                                break;
                                            default:
                                                statusStrip.setCardBackgroundColor(Color.parseColor("#E6E6E6"));
                                        }
                                    } else {
                                        statusStrip.setCardBackgroundColor(Color.parseColor("#E6E6E6"));
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