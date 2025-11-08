package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Fragments.FilterEventsDialogFragment;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.R;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
public class EntrantHomeActivity extends AppCompatActivity {

    private LinearLayout eventListContainer;
    private ListenerRegistration eventListener;

    private List<Event> allEvents;  // store full list

    // Store current filter state
    private String currentStatusFilter = "All";
    private String currentInterestFilter = "All";
    private int currentAvailabilityFilter = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_home);

        // Initialize top and bottom navigation bars
        // For now do top nav manually here
        View entrantBtn = findViewById(R.id.btnEntrant);
        entrantBtn.setOnClickListener(v ->{
            Toast.makeText(this, "You are already on the Entrant Home page.", Toast.LENGTH_SHORT).show();
        });

        View organizerBtn = findViewById(R.id.btnOrganizer);
        organizerBtn.setOnClickListener(v ->{
            Log.d("OrganizerMainPage", "Organizer button clicked");
            Intent intent = new Intent(this, OrganizerMainPage.class);
            startActivity(intent);
        });

        View home = findViewById(R.id.home_activity);
        View myEvents = findViewById(R.id.events_activity);
        View scan = findViewById(R.id.scan_activity);
        View notifications = findViewById(R.id.notifications_activity);

        home.setOnClickListener(v -> {
            // Send to EntrantHomeActivity
            Intent intent = new Intent(this, EntrantHomeActivity.class);
            startActivity(intent);
        });

        myEvents.setOnClickListener(v -> {
            // TODO: Send to EntrantEventsActivity
        });

        scan.setOnClickListener(v -> {
            // Send to EntrantScan
            Intent intent = new Intent(this, EntrantScan.class);
            startActivity(intent);
        });

        notifications.setOnClickListener(v -> {
            // Send to EntrantNotificationsActivity
            Intent intent = new Intent(this, EntrantNotificationsActivity.class);
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
                    currentAvailabilityFilter = -1;
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
    private void displayEvents(List<Event> events) {
        eventListContainer.removeAllViews(); // Clear previous views to prevent duplicates
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Event event : events) {
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

            // Add card to layout
            eventListContainer.addView(cardView);
        }
    }

    private void applyFilters(String status, String interest, int availability) {
        if (allEvents == null) return;

        List<Event> filtered = new java.util.ArrayList<>(allEvents);

        // ✅ Filter by status
        if (!status.equals("All")) {
            filtered.removeIf(event -> {
                if (status.equals("Open")) {
                    return event.getState() != EventState.PUBLISHED;
                } else if (status.equals("Closed")) {
                    return event.getState() != EventState.CLOSED;
                }
                return false;
            });
        }

        // ✅ Filter by interest (if stored in description)
        if (!interest.equals("All")) {
            filtered.removeIf(event ->
                    !event.getDescription().toLowerCase()
                            .contains(interest.toLowerCase())
            );
        }

        // ✅ Filter by availability (Monday, Tuesday etc.)
        if (availability != -1) {
            filtered.removeIf(event -> {
                if (event.getTime() == null) return true; // Remove if no date

                Calendar cal = Calendar.getInstance();
                cal.setTime(event.getTime());

                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

                // Compare the integer day of the week
                return dayOfWeek != availability;
            });
        }


        // ✅ Update UI
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
