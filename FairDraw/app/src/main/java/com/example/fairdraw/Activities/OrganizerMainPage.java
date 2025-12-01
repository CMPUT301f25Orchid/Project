package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairdraw.Adapters.EventRecyclerAdapter;
import com.example.fairdraw.Adapters.ItemSpacingDecoration;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Models.User;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.Others.OrganizerEventsDataHolder;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Activity for the organizer main page.
 */
public class OrganizerMainPage extends BaseTopBottomActivity {
    FrameLayout fragmentContainer;
    RecyclerView eventList;
    EventRecyclerAdapter eventAdapter;
    FirebaseFirestore db;
    CollectionReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_main_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.organizer_navigation_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize shared top and bottom navigation
        initTopNav(BarType.ORGANIZER);
        initBottomNav(BarType.ORGANIZER, findViewById(R.id.home_bottom_nav_bar));

        // Select the organizer home tab
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavView = findViewById(R.id.home_bottom_nav_bar);
        if (bottomNavView != null) bottomNavView.setSelectedItemId(R.id.home_activity);

        checkOrganizerAndInit();
    }

    // Define how to open a the even edit Fragment
    void openFragment(Integer index){
        EditEventPage fragment = new EditEventPage();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.bringToFront();
        // Holder already contains the current list and adapter; no need to reset it here
        Bundle bundle = new Bundle();
        bundle.putInt("position", index);
        fragment.setArguments(bundle);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void checkOrganizerAndInit() {
        String deviceId = DevicePrefsManager.getDeviceId(this);

        // Adjust to your actual UserDB API
        UserDB.getUserOrNull(deviceId, (user, exception) -> {
            if (user == null) {
                // Edge case: somehow no user? Just bail out or send to signup/profile.
                Log.w("OrganizerMainPage", "No user found for device " + deviceId);
                finish(); // or redirect to ProfileActivity/EntrantHome
                return;
            }

            if (user.getRoles() != null && user.getRoles().contains("organizer")) {
                // ✅ Already an organizer – continue as normal
                initOrganizerUi();
            } else {
                // ❌ Not an organizer yet – prompt them
                showNotOrganizerDialog(user);
            }
        });
    }

    private void showNotOrganizerDialog(User user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Organizer Access")
                .setMessage("You’re currently not an organizer.\n\nDo you want to sign up as an organizer so you can create and manage events?")
                .setCancelable(false)
                .setNegativeButton("Not now", (dialog, which) -> {
                    dialog.dismiss();
                    // You can finish the activity or send them back
                    finish(); // or startActivity(new Intent(this, EntrantHomeActivity.class));
                })
                .setPositiveButton("Become organizer", (dialog, which) -> {
                    promoteToOrganizer(user);
                })
                .show();
    }

    private void promoteToOrganizer(User user) {
        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }
        if (!user.getRoles().contains("organizer")) {
            user.getRoles().add("organizer");
        }

        UserDB.upsertUser(user, (ok, e) -> {
            if (!ok) {
                String msg = (e != null) ? e.getMessage() : "Unknown error";
                Snackbar.make(findViewById(android.R.id.content),
                        "Failed to update organizer role: " + msg,
                        Snackbar.LENGTH_LONG).show();
                return;
            }

            // ✅ Role saved, now load the normal organizer UI
            initOrganizerUi();
        });
    }


    private void initOrganizerUi() {
        // Get User DeviceID
        final String deviceId = DevicePrefsManager.getDeviceId(this);

        // Populate event list with database data
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        eventsRef.addSnapshotListener((value, e) -> {
            if (e != null) {
                Log.e("Firestore", e.toString());
            }
            if (value != null && !value.isEmpty()) {
                ArrayList<Event> filtered = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    Event event = doc.toObject(Event.class);
                    if (event.getOrganizer() == null){
                        continue;
                    }
                    if (event.getOrganizer().equals(deviceId)){
                        filtered.add(event);
                    }
                }
                // Update the shared holder which will notify adapters
                OrganizerEventsDataHolder.setDataList(filtered);
            } else {
                // If empty, clear the holder
                OrganizerEventsDataHolder.clear();
            }
        });

        // Set up RecyclerView
        eventList = findViewById(R.id.event_list);
        eventList.setLayoutManager(new LinearLayoutManager(this));

        // use custom spacing between MaterialCardView items
        int spacing = getResources().getDimensionPixelSize(R.dimen.event_item_spacing);
        eventList.addItemDecoration(new ItemSpacingDecoration(spacing));

        eventAdapter = new EventRecyclerAdapter(this::openFragment, position -> {
            // Use adapter as source of truth rather than a separate list
            Event e = eventAdapter.getEventAt(position);
            if (e == null) return;
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerManageEvent.class);
            intent.putExtra("eventId", e.getUuid());
            startActivity(intent);
        });
        eventList.setAdapter(eventAdapter);

        // inform holder about adapter so external code can update it
        OrganizerEventsDataHolder.setEventAdapter(eventAdapter);
    }
}