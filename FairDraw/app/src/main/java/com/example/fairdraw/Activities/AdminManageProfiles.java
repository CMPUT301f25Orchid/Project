package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.DBs.AdminDB;
import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.DBs.OrganizerDB;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Models.Entrant;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Models.User;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminManageProfiles extends BaseTopBottomActivity {
    private LinearLayout profileListContainer;
    private ListenerRegistration profileListener;
    private List<User> allUsers;  // store full list
    private EditText searchBar;
     String TAG = "AdminManageProfiles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_manage_profiles);

        initTopNav(BarType.ADMIN);
        initBottomNav(BarType.ADMIN, findViewById(R.id.admin_bottom_nav));

        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.profiles_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileListContainer = findViewById(R.id.admin_profile_list_container);
        searchBar = findViewById(R.id.admin_search_bar);

        setupSearchBar();

        // Fetch all events from Firestore, listen for real-time updates and display them
        profileListener = UserDB.listenToUsers(users -> {
            if (users != null) {
                allUsers = users; // ✅ store the full list
                displayProfiles(allUsers);
            } else {
                Log.w("AdminEventsPage", "No profiles found in the database.");
                showNoProfilesMessage();
            }
        });
    }

    // Remove listener when activity is destroyed to avoid leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) {
            try {
                profileListener.remove();
            } catch (Exception e) {
                Log.w("AdminEventsPage", "Failed to remove event listener", e);
            }
            profileListener = null;
        }
    }

    private void showNoProfilesMessage() {
        profileListContainer.removeAllViews(); // Clear previous views
        TextView emptyView = new TextView(this);
        emptyView.setText("No Profiles found");
        emptyView.setTextSize(16);
        emptyView.setPadding(16, 16, 16, 16);
        profileListContainer.addView(emptyView);
    }

    private void displayProfiles(List<User> users) {
        profileListContainer.removeAllViews(); // Clear previous views to prevent duplicates
        LayoutInflater inflater = LayoutInflater.from(this);

        for (User user : users) {
            // If user is null, skip it
            if (user == null) continue;

            // If event uuid is null, skip it
            if (user.getDeviceId() == null) {
                Log.w("AdminManageProfiles", "Skipping user with null DeviceId");
                continue;
            }

            // Inflate the card layout
            CardView cardView = (CardView) inflater.inflate(R.layout.admin_profilecard, profileListContainer, false);

            // Bind views
            TextView nameView = cardView.findViewById(R.id.user_name);
            TextView emailView = cardView.findViewById(R.id.user_email);
            TextView profileIdView = cardView.findViewById(R.id.user_profile_id);
            TextView dateJoinedView = cardView.findViewById(R.id.date_joined);
            ImageButton deleteButton = cardView.findViewById(R.id.delete_user_button);
            ImageView eventImage = cardView.findViewById(R.id.user_profile_icon);

            // Set values
            nameView.setText(user.getName());
            emailView.setText(user.getEmail());
            profileIdView.setText("User Id: " + user.getDeviceId());
            Date dateJoined = user.getDateJoined();
            if (dateJoined != null) {
                dateJoinedView.setText("User since: " + dateJoined.toString());
            } else {
                dateJoinedView.setText("User since: N/A");
            }

            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete your account? This will also affect all events you may have organized. This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // User confirmed, start the deletion process
                            Snackbar.make(findViewById(android.R.id.content), "Deleting account...", Snackbar.LENGTH_SHORT).show();
                            performCascadingDelete(user.getDeviceId());
                        })
                        .setNegativeButton("Cancel", null) // Do nothing if canceled
                        .show();
            });

            // Add card to layout
            profileListContainer.addView(cardView);
        }
    }
    private void performCascadingDelete(String deviceId) {
        // Delete user from Entrant, Organiser and Admin collections.
        EntrantDB.deleteEntrant(deviceId, ok -> {
            if (!ok) {
                Log.e(TAG, "Failed to delete from Entrants, but continuing deletion process.");
            }
        });
        OrganizerDB.deleteOrganizer(deviceId, ok -> {
            if (!ok) {
                Log.e(TAG, "Failed to delete from Organizers, but continuing deletion process.");
            }
        });

        AdminDB.deleteAdmin(deviceId, ok -> {
            if (!ok) {
                Log.e(TAG, "Failed to delete from Admins, but continuing deletion process.");
            }
        });

        // After a brief delay to allow role deletion to start, delete the main user document.
        UserDB.deleteUser(deviceId, (finalOk, finalE) -> {
            if (finalOk) {
                Log.d(TAG, "User account deleted successfully.");
                Snackbar.make(findViewById(android.R.id.content), "Account deleted.", Snackbar.LENGTH_SHORT).show();


                // Navigate back to the main sign-up/entry activity
                Intent intent = new Intent(AdminManageProfiles.this, SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                handleDeleteError(finalE);
            }
        });
    }

    private void handleDeleteError(Exception e) {
        Log.e(TAG, "Failed to delete user account.", e);
        Snackbar.make(findViewById(android.R.id.content), "Failed to delete account.", Snackbar.LENGTH_SHORT).show();
    }



    private void setupSearchBar() {
        if (searchBar == null) return;
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString();
                filterAndDisplayProfiles(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Filter the cached `allEvents` by the query and display results. Matches title, location, or uuid.
    private void filterAndDisplayProfiles(String query) {
        if (allUsers == null) {
            // If we haven't loaded profiles yet, nothing to filter
            return;
        }
        String input = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (input.isEmpty()) {
            displayProfiles(allUsers);
            return;
        }

        List<User> filtered = new ArrayList<>();
        for (User user : allUsers) {
            if (user == null) continue;
            String name = user.getName() == null ? "" : user.getName().toLowerCase(Locale.ROOT);
            String email = user.getEmail() == null ? "" : user.getEmail().toLowerCase(Locale.ROOT);
            String id = user.getDeviceId() == null ? "" : user.getDeviceId().toLowerCase(Locale.ROOT);


            if (name.contains(input) || email.contains(input) || id.contains(input)) {
                filtered.add(user);
            }
        }

        if (filtered.isEmpty()) {
            showNoProfilesMessage();
        } else {
            displayProfiles(filtered);
        }

    }

}