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
import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Adapters.EntrantListArrayAdapter;
import com.example.fairdraw.Models.Entrant;
import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.Others.NotificationType;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.example.fairdraw.Others.ListItemEntrant;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;
import com.example.fairdraw.SendNotificationDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;

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

        // If the user is not the organizer of the event, finish the activity
        EventDB.getEventCollection().document(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    event = document.toObject(Event.class);
                    if (event == null || !event.getOrganizer().equals(DevicePrefsManager.getDeviceId(this))) {
                        Log.d("OrganizerManageEvent", "User is not the organizer of the event");
                        finish();
                        return;
                    }
                } else {
                    Log.d("OrganizerManageEvent", "Event document does not exist");
                    finish();
                    return;
                }
            }});

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
                event = snapshot.toObject(Event.class);
                assert event != null;
                bindEvent(event);
            } else {
                Log.d("OrganizerManageEvent", "Current data: null");
            }
        });

        btnDrawAndInvite.setOnClickListener(v -> {
            event.drawLotteryWinners();
            EventDB.updateEvent(event, new EventDB.UpdateEventCallback() {
                @Override
                public void onCallback(boolean success) {
                    Log.d("OrganizerManageEvent", "Event updated");
                    if (success) {
                        // Send a notification to the entrants which is just pushing a notification
                        // to the Entrant object
                        // First, go through the invited list and send a notification to each entrant
                        for (String entrantId : event.getInvitedList()) {
                            EntrantDB.getEntrant(entrantId, new EntrantDB.GetEntrantCallback() {
                                @Override
                                public void onCallback(Entrant entrant) {
                                    if (entrant == null) return;
                                    EntrantNotification notification =
                                            new EntrantNotification(NotificationType.WIN, eventId,
                                                    "You won!");
                                    EntrantDB.pushNotificationToUser(entrantId, notification, null);
                                }
                            });
                        }
                    }
                }
            });
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
        buildInvitedRecycler(rvInvited, e.getInvitedList());

        // Cancelled list
        buildReadOnlyRecycler(rvCancelled, e.getCancelledList());

        // Registered list
        buildReadOnlyRecycler(rvRegistered, e.getEnrolledList());

        // Waiting list
        buildReadOnlyRecycler(rvWaiting, e.getWaitingList());

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

    private void buildReadOnlyRecycler(RecyclerView rv, List<String> ids) {
        List<ListItemEntrant> list = new ArrayList<>();
        for (String id : ids) list.add(new ListItemEntrant(id));
        EntrantListArrayAdapter adapter = new EntrantListArrayAdapter(this, list, true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        rv.setAdapter(adapter);
    }


    private void buildInvitedRecycler(RecyclerView rv, List<String> invited) {
        List<ListItemEntrant> entrantList = new ArrayList<>();
        for (String id : invited) entrantList.add(new ListItemEntrant(id));

        EntrantListArrayAdapter adapter = new EntrantListArrayAdapter(this, entrantList, false);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        rv.setAdapter(adapter);

        adapter.setOnCloseClickListener((entrantId, position) -> {
            if (event == null) return;

            boolean moved = event.moveInvitedToCancelled(entrantId);
            if (!moved) {
                Toast.makeText(this, "Entrant not in invited list.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Persist — use your signature; if you have a callback, handle error/rollback if needed
            EventDB.updateEvent(event, new EventDB.UpdateEventCallback(){
                @Override
                public void onCallback(boolean success) {
                    if (success) {
                        adapter.notifyItemRemoved(position);
                    } else {
                        Toast.makeText(OrganizerManageEvent.this, "Failed to update event.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

}
