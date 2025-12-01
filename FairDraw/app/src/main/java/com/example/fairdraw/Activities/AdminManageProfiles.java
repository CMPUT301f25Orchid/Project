package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Models.Entrant;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Models.User;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class AdminManageProfiles extends BaseTopBottomActivity {
    private LinearLayout profileListContainer;
    private ListenerRegistration profileListener;
    private List<User> allUsers;  // store full list
    private EditText searchBar;

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

//        setupSearchBar();

        // Fetch all events from Firestore, listen for real-time updates and display them
        profileListener = UserDB.listenToUsers(users -> {
            if (users != null) {
                allUsers = users; // ✅ store the full list
                displayProfiles(allUsers);
            } else {
                Log.w("AdminEventsPage", "No profiles found in the database.");
//                showNoProfilesMessage();
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

    private void showNoEventsMessage() {
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

        }
    }
}