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
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.Others.OrganizerEventsDataHolder;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
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
        // Get User DeviceID
        final String deviceId = DevicePrefsManager.getDeviceId(this);

        // Initialize shared top and bottom navigation
        initTopNav(BarType.ORGANIZER);
        initBottomNav(BarType.ORGANIZER, findViewById(R.id.home_bottom_nav_bar));
        // Select the organizer home tab
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavView = findViewById(R.id.home_bottom_nav_bar);
        if (bottomNavView != null) bottomNavView.setSelectedItemId(R.id.home_activity);


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

        // bottom nav handled by BaseTopBottomActivity.initBottomNav
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
}