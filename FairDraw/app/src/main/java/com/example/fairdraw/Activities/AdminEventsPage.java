package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminEventsPage extends BaseTopBottomActivity {
    private LinearLayout eventListContainer;
    private ListenerRegistration eventListener;
    private List<Event> allEvents;  // store full list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_events_page);

        initTopNav(BarType.ADMIN);
        initBottomNav(BarType.ADMIN, findViewById(R.id.admin_bottom_nav));

        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.admin_events_activity);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        eventListContainer = findViewById(R.id.admin_event_list_container);

        // Fetch all events from Firestore, listen for real-time updates and display them
        eventListener = EventDB.listenToEvents(events -> {
            if (events != null) {
                allEvents = events; // ✅ store the full list
                displayEvents(events);
            } else {
                showNoEventsMessage();
            }
        });

    }
    private void showNoEventsMessage() {
        eventListContainer.removeAllViews(); // Clear previous views
        TextView emptyView = new TextView(this);
        emptyView.setText("No Events found");
        emptyView.setTextSize(16);
        emptyView.setPadding(16, 16, 16, 16);
        eventListContainer.addView(emptyView);
    }

    private void displayEvents(List<Event> events) {
        eventListContainer.removeAllViews(); // Clear previous views to prevent duplicates
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Event event : events) {
            // If event is null, skip it
            if (event == null) continue;

            // If event uuid is null, skip it
            if (event.getUuid() == null) {
                Log.w("AdminEventsPage", "Skipping event with null UUID with title: " + event.getTitle());
                continue;
            }

            // Inflate the card layout
            CardView cardView = (CardView) inflater.inflate(R.layout.admin_eventscard, eventListContainer, false);

            // Bind views
            TextView titleView = cardView.findViewById(R.id.event_title);
            TextView locationView = cardView.findViewById(R.id.event_location);
            TextView organizerNameView = cardView.findViewById(R.id.event_organizer_name);
            TextView eventIdView = cardView.findViewById(R.id.event_id);
            ImageButton deleteButton = cardView.findViewById(R.id.delete_button);
            ImageView eventImage = cardView.findViewById(R.id.event_image);

            // Set values
            titleView.setText(event.getTitle());
            locationView.setText(event.getLocation());
            organizerNameView.setText(event.getOrganizer());
            eventIdView.setText("id: " + event.getUuid());

            //Implementing Deleting the event by CLEO_SLAYS!!


            // Try to fetch the Bitmap for the event image
            FirebaseImageStorageService storageService = new FirebaseImageStorageService();
            storageService.getEventPosterDownloadUrl(event.getUuid()).addOnSuccessListener(uri -> {
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.swimming)
                        .into(eventImage);
            }).addOnFailureListener(e -> {
                Log.e("AdminEventsPage", "Failed to load image for event " + event.getUuid(), e);
            });

            // Add card to layout
            eventListContainer.addView(cardView);
        }
    }
}