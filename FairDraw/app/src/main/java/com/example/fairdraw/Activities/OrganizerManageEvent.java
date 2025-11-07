package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Adapters.EntrantListArrayAdapter;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.example.fairdraw.Others.ListItemEntrant;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;
import com.example.fairdraw.SendNotificationDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class OrganizerManageEvent extends AppCompatActivity {

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

    public void bindEvent(Event e) {
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
            });

            dialog.show(getSupportFragmentManager(), "SendNotificationDialog");
        });
    }

    private void buildEntrantItemRecyclerView(RecyclerView recyclerView, List<String> stringList, Boolean hideCloseButton) {
        List<ListItemEntrant> entrantList = new ArrayList<>();
        for (String s : stringList) {
            entrantList.add(new ListItemEntrant(s));
        }
        EntrantListArrayAdapter adapter = new EntrantListArrayAdapter(this, entrantList, hideCloseButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
