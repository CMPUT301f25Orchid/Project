package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.example.fairdraw.FilterEventsDialogFragment;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EntrantHomeActivity extends BaseTopBottomActivity {

    private LinearLayout eventListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_home);

        initBottomNav(BarType.ENTRANT, findViewById(R.id.home_bottom_nav_bar));
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

        // Fetch all events from Firestore
        EventDB.getEvents(events -> {
            if (events != null && !events.isEmpty()) {
                displayEvents(events);
            } else {
                showNoEventsMessage();
            }
        });

        Button filterBtn = findViewById(R.id.filterEventsBtn);
        filterBtn.setOnClickListener(v -> {
            FilterEventsDialogFragment dialog = new FilterEventsDialogFragment();
            dialog.setFilterListener(new FilterEventsDialogFragment.FilterListener() {
                @Override
                public void onFiltersApplied(String status, String interest, String availability) {
                    // TODO: actually filter events here
                    Toast.makeText(EntrantHomeActivity.this,
                            "Filters applied: " + status + ", " + interest + ", " + availability,
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFiltersCleared() {
                    Toast.makeText(EntrantHomeActivity.this, "Filters cleared", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show(getSupportFragmentManager(), "filter_dialog");
        });

    }

    /**
     * Adds eventscard.xml for each Event
     */
    private void displayEvents(List<Event> events) {
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Event event : events) {
            // Inflate the card layout
            CardView cardView = (CardView) inflater.inflate(R.layout.eventscard, eventListContainer, false);

            // Bind views
            TextView titleView = cardView.findViewById(R.id.eventTitle);
            TextView locationView = cardView.findViewById(R.id.eventLocation);
            TextView dateView = cardView.findViewById(R.id.eventDate);
            TextView capacityView = cardView.findViewById(R.id.eventCapacity);
            TextView priceView = cardView.findViewById(R.id.eventPrice);
            TextView statusView = cardView.findViewById(R.id.eventStatus);
            Button joinBtn = cardView.findViewById(R.id.viewDetailsButton);
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
            joinBtn.setOnClickListener(v ->
                    Toast.makeText(this, "Joined waiting list for " + event.getTitle(), Toast.LENGTH_SHORT).show()
            );

            // Add card to layout
            eventListContainer.addView(cardView);
        }
    }

    private void showNoEventsMessage() {
        TextView emptyView = new TextView(this);
        emptyView.setText("No events are currently open for registration.");
        emptyView.setTextSize(16);
        emptyView.setPadding(16, 16, 16, 16);
        eventListContainer.addView(emptyView);
    }
}
