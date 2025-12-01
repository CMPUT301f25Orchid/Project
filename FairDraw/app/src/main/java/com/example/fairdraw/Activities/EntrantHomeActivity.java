package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Fragments.FilterEventsDialogFragment;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.Others.FilterUtils;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * EntrantHomeActivity displays the home screen for entrants, showing a list of events
 * and allowing navigation to other parts of the app such as the organizer page, scan page,
 * and notifications. It also handles filtering of events based on user-selected criteria.
 */
public class EntrantHomeActivity extends BaseTopBottomActivity {

    private LinearLayout eventListContainer;
    private ListenerRegistration eventListener;

    private List<Event> allEvents;  // store full list

    // Store current filter state
    private String currentStatusFilter = "All";
    private String currentInterestFilter = "All";
    private int currentAvailabilityFilter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_home);

        // Initialize shared top and bottom navigation using BaseTopBottomActivity
        initTopNav(BarType.ENTRANT);
        initBottomNav(BarType.ENTRANT, findViewById(R.id.home_bottom_nav_bar));

        BottomNavigationView bottomNav = findViewById(R.id.home_bottom_nav_bar);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.home_activity);
        findViewById(R.id.imgAvatar).setOnClickListener(v -> {
            // Send to ProfileActivity
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventListContainer = findViewById(R.id.event_list_container);

        // Fetch all events from Firestore and listen for real-time updates
        eventListener = EventDB.listenToEvents(events -> {
            if (events != null) {
                allEvents = events;       // ✅ store the full list
                // Re-apply the current filters to the updated list
                applyFilters(currentStatusFilter, currentInterestFilter, currentAvailabilityFilter);
            } else {
                showNoEventsMessage();
            }
        });


        Button filterBtn = findViewById(R.id.filterEventsBtn);
        filterBtn.setOnClickListener(v -> {
            // ✅ Use the newInstance method to pass the current filter state
            FilterEventsDialogFragment dialog = FilterEventsDialogFragment.newInstance(
                    currentStatusFilter,
                    currentInterestFilter,
                    currentAvailabilityFilter
            );
            dialog.setFilterListener(new FilterEventsDialogFragment.FilterListener() {

                @Override
                public void onFiltersApplied(String status, String interest, int availability) {
                    // Save the new filter state
                    currentStatusFilter = status;
                    currentInterestFilter = interest;
                    currentAvailabilityFilter = availability;
                    applyFilters(status, interest, availability);
                }

                @Override
                public void onFiltersCleared() {
                    // Reset filter state to default
                    currentStatusFilter = "All";
                    currentInterestFilter = "All";
                    currentAvailabilityFilter = 0;
                    if (allEvents != null) {
                        displayEvents(allEvents);
                    }
                }
            });
            dialog.show(getSupportFragmentManager(), "filter_dialog");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener to prevent memory leaks
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    /**
     * Adds eventscard.xml for each Event
     */
    public void displayEvents(List<Event> events) {
        eventListContainer.removeAllViews(); // Clear previous views to prevent duplicates
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Event event : events) {
            // If event is null, skip it
            if (event == null) continue;

            // If event uuid is null, skip it
            if (event.getUuid() == null) {
                Log.w("EntrantHomeActivity", "Skipping event with null UUID with title: " + event.getTitle());
                continue;
            }

            // Inflate the card layout
            CardView cardView = (CardView) inflater.inflate(R.layout.eventscard, eventListContainer, false);

            // Bind views
            TextView titleView = cardView.findViewById(R.id.event_content_title);
            TextView locationView = cardView.findViewById(R.id.event_content_location);
            TextView dateView = cardView.findViewById(R.id.event_content_date);
            TextView capacityView = cardView.findViewById(R.id.event_content_capacity);
            TextView priceView = cardView.findViewById(R.id.event_content_price);
            TextView statusView = cardView.findViewById(R.id.eventStatus);
            Button joinBtn = cardView.findViewById(R.id.event_edit_button);
            Button viewDetailsButton = cardView.findViewById(R.id.view_details_button);
            ImageView eventImage = cardView.findViewById(R.id.eventImage);

            // Set values
            titleView.setText(event.getTitle());
            locationView.setText(event.getLocation());
            if (event.getTime() != null) {
                dateView.setText(dateFormat.format(event.getTime()));
            } else {
                dateView.setText("Date not available");
            }

            capacityView.setText(String.format(Locale.getDefault(),
                    "%d/%d confirmed - %d on waiting list",
                    event.getEnrolledList() != null ? event.getEnrolledList().size() : 0,
                    event.getCapacity(),
                    event.getWaitingList() != null ? event.getWaitingList().size() : 0
            ));
            priceView.setText("$" + event.getPrice());

            // Status
            statusView.setText(event.getState().toString());
            if (event.getState() == EventState.PUBLISHED) {
                statusView.setBackgroundColor(getColor(android.R.color.holo_green_dark));
            } else if (event.getState() == EventState.CLOSED) {
                statusView.setBackgroundColor(getColor(android.R.color.holo_red_dark));
            } else {
                statusView.setBackgroundColor(getColor(android.R.color.darker_gray));
            }

            // Set button click
            viewDetailsButton.setOnClickListener(v -> {
                // TODO: navigate to event details
                Intent intent = new Intent(EntrantHomeActivity.this, EntrantEventDetails.class);
                intent.putExtra("event_id", event.getUuid());
                startActivity(intent);
            });

            // Try to fetch the Bitmap for the event image
            FirebaseImageStorageService storageService = new FirebaseImageStorageService();
            storageService.getEventPosterDownloadUrl(event.getUuid()).addOnSuccessListener(uri -> {
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.swimming)
                        .into(eventImage);
            }).addOnFailureListener(e -> {
                Log.e("EntrantHomeActivity", "Failed to load image for event " + event.getUuid(), e);
            });

            // Add card to layout
            eventListContainer.addView(cardView);
        }
    }

    /**
     * Applies the given filters to the events list and updates the UI.
     *
     * @param status       the status filter ("All", "Open", "Closed", "Draft")
     * @param interest     the interest/tag filter (case-insensitive exact match, or "All")
     * @param availability the availability filter (0=All, 1=HasFreeSpots, 2=Full, 3=HasWaitingList)
     */
    private void applyFilters(String status, String interest, int availability) {
        if (allEvents == null) return;

        // Use FilterUtils for robust filtering
        List<Event> filtered = FilterUtils.applyFilters(allEvents, status, interest, availability);

        // Update UI
        if (filtered.isEmpty()) {
            showNoEventsMessage();
        } else {
            displayEvents(filtered);
        }
    }

    private void showNoEventsMessage() {
        eventListContainer.removeAllViews(); // Clear previous views
        TextView emptyView = new TextView(this);
        emptyView.setText("No events match your filter criteria.");
        emptyView.setTextSize(16);
        emptyView.setPadding(16, 16, 16, 16);
        eventListContainer.addView(emptyView);
    }
}
