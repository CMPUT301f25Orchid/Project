package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminPicturesPage extends BaseTopBottomActivity {

    private static final String TAG = "AdminPicturesPage";

    private LinearLayout pictureListContainer;

    /**
     * List of Firestore listener registrations for real-time event data updates.
     * Each event poster gets its own listener attached in displayPictures().
     * All listeners are removed in onStop() to avoid memory leaks and ensure
     * the listener lifecycle is tied to the activity's visible state.
     */
    private final List<ListenerRegistration> eventListeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_pictures_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initTopNav(BarType.ADMIN);
        initBottomNav(BarType.ADMIN, findViewById(R.id.admin_bottom_nav));

        pictureListContainer = findViewById(R.id.picture_list_container);
    }

    /**
     * Attach the Firestore listeners in onStart() so that the UI updates in real-time
     * whenever events are added, removed, or changed. This follows the established
     * pattern used by other activities (e.g., EntrantNotificationsActivity, EntrantMyEventsActivity)
     * for proper lifecycle management and avoiding memory leaks.
     */
    @Override
    protected void onStart() {
        super.onStart();
        loadPictures();
    }

    /**
     * Remove all Firestore listeners in onStop() to prevent memory leaks and
     * unnecessary background updates when the activity is no longer visible.
     * This mirrors the onStart() attachment and follows Android lifecycle best practices.
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Remove all listeners with null checks to prevent potential crashes
        removeAllListeners();
    }

    /**
     * Removes all attached Firestore listeners and clears the list.
     * Thread-safe removal with exception handling for each listener.
     */
    private void removeAllListeners() {
        for (ListenerRegistration listener : eventListeners) {
            if (listener != null) {
                try {
                    listener.remove();
                } catch (Exception e) {
                    Log.w(TAG, "Failed to remove event listener", e);
                }
            }
        }
        eventListeners.clear();
    }

    /**
     * Loads event posters from Firebase Storage and displays them.
     * Uses the callback-based listenToEventPosters method for consistent error handling
     * and a cleaner interface pattern matching other listener methods in the codebase.
     * For each poster, a real-time Firestore listener is attached for event data updates.
     */
    private void loadPictures() {
        FirebaseImageStorageService imageService = new FirebaseImageStorageService();

        // Use the callback-based method for fetching event posters
        imageService.listenToEventPosters(eventPosters -> {
            if (eventPosters != null) {
                displayPictures(eventPosters);
            } else {
                // Show a snack bar / error UI
                Log.e(TAG, "Failed to load pictures");
                Snackbar.make(findViewById(R.id.main), "Failed to load pictures", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void displayPictures(List<FirebaseImageStorageService.EventPosterInfo> eventPosters) {
        // Clear any existing listeners before adding new ones to prevent duplicates.
        // This is necessary because loadPictures() is async (Firebase Storage call) and
        // could potentially return after activity lifecycle changes, causing duplicate listeners.
        removeAllListeners();

        pictureListContainer.removeAllViews(); // Clear previous views to prevent duplicates
        LayoutInflater inflater = LayoutInflater.from(this);
        for (FirebaseImageStorageService.EventPosterInfo posterInfo : eventPosters) {
            CardView cardView = (CardView) inflater.inflate(R.layout.picture_card, pictureListContainer, false);
            ImageView posterImage = cardView.findViewById(R.id.event_picture);
            TextView eventTitleView = cardView.findViewById(R.id.event_picture_title);
            TextView organizerView = cardView.findViewById(R.id.event_picture_author);
            ImageButton deleteButton = cardView.findViewById(R.id.delete_image_button);

            deleteButton.setOnClickListener(v -> {
                FirebaseImageStorageService imageService = new FirebaseImageStorageService();
                imageService.deleteEventPoster(posterInfo.eventId).addOnSuccessListener(aVoid -> {
                    // Remove the card view from the container
                    pictureListContainer.removeView(cardView);
                    Snackbar.make(findViewById(R.id.main), "Picture deleted successfully", Snackbar.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    Snackbar.make(findViewById(R.id.main), "Failed to delete picture", Snackbar.LENGTH_LONG).show();
                });
            });

            // Attach a real-time listener for event data updates.
            // Store the registration in the list so it can be properly removed in onStop().
            ListenerRegistration listener = EventDB.listenToEvent(posterInfo.eventId, event -> {
                if (event != null) {
                    eventTitleView.setText(event.getTitle());
                    organizerView.setText("By: " + event.getOrganizer());
                } else {
                    eventTitleView.setText("Unknown Event");
                    organizerView.setText("");
                }
            });
            eventListeners.add(listener);

            // Load image using Glide
            Glide.with(this)
                    .load(posterInfo.downloadUri)
                    .placeholder(R.drawable.default_event_banner) // Optional placeholder
                    .error(R.drawable.default_event_banner) // Optional error image
                    .into(posterImage);

            pictureListContainer.addView(cardView);

        }
    }
}

