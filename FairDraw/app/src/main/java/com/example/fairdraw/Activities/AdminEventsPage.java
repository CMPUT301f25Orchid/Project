package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Models.User;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminEventsPage extends BaseTopBottomActivity {
    private LinearLayout eventListContainer;
    private ListenerRegistration eventListener;
    private List<Event> allEvents;  // store full list
    private EditText searchBar;


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
        searchBar = findViewById(R.id.admin_search_bar);

        // setup search filtering
        setupSearchBar();

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

    // Remove listener when activity is destroyed to avoid leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            try {
                eventListener.remove();
            } catch (Exception e) {
                Log.w("AdminEventsPage", "Failed to remove event listener", e);
            }
            eventListener = null;
        }
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
            UserDB.getUserOrNull(event.getOrganizer(), new UserDB.GetUserCallback() {
                @Override
                public void onCallback(@Nullable User user, @Nullable Exception e) {
                    if(user != null) {
                        organizerNameView.setText(user.getName());
                    }
                    else {
                        organizerNameView.setText("Unknown Organizer");
                        Log.e("AdminEventsPage", "Failed to load organizer for event " + event.getUuid());
                    }
                }
            });
            eventIdView.setText("id: " + event.getUuid());

            //On click listener for deleting events
            deleteButton.setOnClickListener(v -> {
                // Show a confirmation dialog before deleting
                new AlertDialog.Builder(AdminEventsPage.this)
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to permanently delete '" + event.getTitle() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // User confirmed deletion
                            EventDB.deleteEvent(event.getUuid(), success -> {
                                if (success) {
                                    Snackbar.make(findViewById(R.id.main), "Event deleted successfully", Snackbar.LENGTH_LONG).show();
                                } else {
                                    Snackbar.make(findViewById(R.id.main), "Failed to delete event", Snackbar.LENGTH_LONG).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null) // Do nothing if cancelled
                        .show();
            });

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

    // New helper: set up a TextWatcher on the search bar to filter events
    private void setupSearchBar() {
        if (searchBar == null) return;
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString();
                filterAndDisplayEvents(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Filter the cached `allEvents` by the query and display results. Matches title, location, or uuid.
    private void filterAndDisplayEvents(String query) {
        if (allEvents == null) {
            // If we haven't loaded events yet, nothing to filter
            return;
        }
        String input = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (input.isEmpty()) {
            displayEvents(allEvents);
            return;
        }

        List<Event> filtered = new ArrayList<>();
        for (Event event : allEvents) {
            if (event == null) continue;
            String title = event.getTitle() == null ? "" : event.getTitle().toLowerCase(Locale.ROOT);
            String location = event.getLocation() == null ? "" : event.getLocation().toLowerCase(Locale.ROOT);
            String id = event.getUuid() == null ? "" : event.getUuid().toLowerCase(Locale.ROOT);

            if (title.contains(input) || location.contains(input) || id.contains(input)) {
                filtered.add(event);
            }
        }

        if (filtered.isEmpty()) {
            showNoEventsMessage();
        } else {
            displayEvents(filtered);
        }
    }
}