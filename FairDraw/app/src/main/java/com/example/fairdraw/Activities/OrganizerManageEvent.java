package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Adapters.EntrantListArrayAdapter;
import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.Others.NotificationType;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.example.fairdraw.Others.ListItemEntrant;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;
import com.example.fairdraw.Fragments.SendNotificationDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * OrganizerManageEvent class allows organizers to manage event details,
 * view and manage entrants, and send notifications.
 */
public class OrganizerManageEvent extends BaseTopBottomActivity {

    String eventId;
    Event event;
    ImageView ivHeroImage;
    TextView tvTitle;
    TextView tvRegStatus;
    TextView tvDescription;
    Button btnDrawAndInvite;
    RecyclerView rvInvited;
    RecyclerView rvCancelled;
    RecyclerView rvRegistered;
    RecyclerView rvWaiting;
    Button btnSendNotification;
    Button btnSeeFinalEntrants;
    Button btnSeeWaitingMap;
    FirebaseImageStorageService storageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_manage_event);
        eventId = getIntent().getStringExtra("eventId");

        // Setup the top and bottom edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.top_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize shared top and bottom navigation
        initTopNav(com.example.fairdraw.Others.BarType.ORGANIZER);
        initBottomNav(com.example.fairdraw.Others.BarType.ORGANIZER, findViewById(R.id.home_bottom_nav_bar));
        // Select the organizer home tab (manager pages belong to organizer section)
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.home_bottom_nav_bar);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.home_activity);

        // If no event id is provided, finish the activity
        if (eventId == null || eventId.isEmpty()) {
            Log.d("OrganizerManageEvent", "No event id provided");
            finish();
            return;
        }

        storageService = new FirebaseImageStorageService();

        // Initialize views
        ivHeroImage = findViewById(R.id.heroImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvRegStatus = findViewById(R.id.tvStatusPill);
        tvDescription = findViewById(R.id.tvDescription);
        btnDrawAndInvite = findViewById(R.id.btnDrawAndInvite);
        rvInvited = findViewById(R.id.rvInvited);
        rvCancelled = findViewById(R.id.rvCancelled);
        rvRegistered = findViewById(R.id.rvRegistered);
        rvWaiting = findViewById(R.id.rvWaiting);
        btnSendNotification = findViewById(R.id.btnSendNotification);
        btnSeeFinalEntrants = findViewById(R.id.btnSeeFinalEntrants);
        btnSeeWaitingMap = findViewById(R.id.btnSeeWaitingMap);

        EventDB.getEventCollection().document(eventId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("OrganizerManageEvent", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Event event1 = snapshot.toObject(Event.class);
                assert event1 != null;
                bindEvent(event1);
            } else {
                Log.d("OrganizerManageEvent", "Current data: null");
            }
        });
    }

    /**
     * Binds the event data to the UI components.
     *
     * @param e The event object containing event details.
     */
    public void bindEvent(Event e) {
        event = e;

        // Show hero image
        storageService.getEventPosterDownloadUrl(eventId).addOnCompleteListener(urlTask -> {
            if (urlTask.isSuccessful()) {
                String downloadUrl = urlTask.getResult().toString();
                Glide.with(this).load(downloadUrl).into(ivHeroImage);
            }
        });

        // Show title
        tvTitle.setText((e.getTitle() == null || e.getTitle().isEmpty()) ? "Untitled Event" : e.getTitle());

        // Show registration status
        tvRegStatus.setText(e.getState().toString());

        // Show description
        tvDescription.setText((e.getDescription() == null || e.getDescription().isEmpty()) ? "No description provided" : e.getDescription());

        // Draw and invite button logic later
        btnDrawAndInvite.setOnClickListener(v -> {
            Toast.makeText(this, "Draw and Invite clicked for event: " + eventId, Toast.LENGTH_SHORT).show();
            EventDB.getEvent(eventId, (event) -> {
                if (event != null) {
                    // Perform draw and invite logic here
                    Toast.makeText(this, "Drawing and inviting entrants...", Toast.LENGTH_SHORT).show();
                    List<String> invitedEntrants = event.drawLotteryWinners();

                    // Update the event in the database
                    EventDB.updateEvent(event, success -> {
                        if (success) {
                            Toast.makeText(OrganizerManageEvent.this, "Entrants drawn and updated!", Toast.LENGTH_SHORT).show();
                            // now send the win notifications to the invited entrants
                            for (String entrantId : invitedEntrants) {
                                EntrantDB.pushNotificationToUser(entrantId, new EntrantNotification(NotificationType.WIN, eventId, event.getTitle()), (ok, e1) -> {
                                    if (ok) {
                                        Log.d("OrganizerManageEvent", "Notification sent to entrant ID: " + entrantId);
                                    } else {
                                        Log.d("OrganizerManageEvent", "Failed to send notification to entrant ID: " + entrantId + " | " + (e1 != null ? e1.getMessage() : "Unknown error"));
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(OrganizerManageEvent.this, "Failed to update event after drawing.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Failed to retrieve event for drawing: ", Toast.LENGTH_LONG).show();
                }
            });
        });

        // Show invited list
        buildEntrantItemRecyclerView(rvInvited, e.getInvitedList(), false);

        // Cancelled list
        buildEntrantItemRecyclerView(rvCancelled, e.getCancelledList(), true);

        // Registered list
        buildEntrantItemRecyclerView(rvRegistered, e.getEnrolledList(), true);

        // Waiting list
        buildEntrantItemRecyclerView(rvWaiting, e.getWaitingList(), true);

        // Link action buttons later
        btnSendNotification.setOnClickListener(v -> {
            // Open the Dialog
            SendNotificationDialogFragment dialog = SendNotificationDialogFragment.newInstance(eventId);
            dialog.setListener((eventId1, audience, message) -> {
                Toast.makeText(this,
                        "Send to " + audience + " | " + message, Toast.LENGTH_SHORT).show();

                List<String> targetEntrants;
                switch (audience) {
                    case SELECTED:
                        targetEntrants = e.getInvitedList();
                        break;
                    case CANCELLED:
                        targetEntrants = e.getCancelledList();
                        break;
                    case WAITING_LIST:
                    default:
                        targetEntrants = e.getWaitingList();
                        break;
                }
                EntrantNotification notification = new EntrantNotification(NotificationType.OTHER, eventId1, "No title");
                notification.message = message;
                for (String entrantId : targetEntrants) {
                    EntrantDB.pushNotificationToUser(entrantId, notification, (ok, ex) -> {
                        if (ok) {
                            Log.d("OrganizerManageEvent", "Custom notification sent to entrant ID: " + entrantId);
                        } else {
                            Log.d("OrganizerManageEvent", "Failed to send custom notification to entrant ID: " + entrantId + " | " + (ex != null ? ex.getMessage() : "Unknown error"));
                        }
                    });
                }
            });

            dialog.show(getSupportFragmentManager(), "SendNotificationDialog");
        });
    }

    private void buildEntrantItemRecyclerView(RecyclerView recyclerView, List<String> stringList, Boolean hideCloseButton) {
        List<ListItemEntrant> entrantList = new ArrayList<>();
        for (String s : stringList) {
            entrantList.add(new ListItemEntrant(s));
        }
        EntrantListArrayAdapter adapter = new EntrantListArrayAdapter(this, entrantList, hideCloseButton, event.getUuid());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
