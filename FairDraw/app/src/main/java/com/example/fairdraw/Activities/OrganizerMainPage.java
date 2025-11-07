package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.fairdraw.Adapters.EventArrayAdapter;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.OrganizerEventsDataHolder;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Activity for the organizer main page.
 */
public class OrganizerMainPage extends AppCompatActivity {
    BottomNavigationView bottomNav;
    FrameLayout fragmentContainer;
    ListView eventList;
    EventArrayAdapter eventAdapter;
    FirebaseFirestore db;
    CollectionReference eventsRef;

    ArrayList<Event> dataList;





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

        // Populate event list with database data
        db = FirebaseFirestore.getInstance();
        dataList = OrganizerEventsDataHolder.getDataList();
        eventsRef = db.collection("events");
        eventsRef.addSnapshotListener((value, e) -> {
            if (e != null) {
                Log.e("Firestore", e.toString());
            }
            if (value != null && !value.isEmpty()) {
                dataList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Event event = doc.toObject(Event.class);
                    if (event.getOrganizer() == null){
                        continue;
                    }
                    if (event.getOrganizer().equals(deviceId)){
                        dataList.add(event);
                    }
                }
                eventAdapter.notifyDataSetChanged();
            }
        });

        // Set up event list view
        eventList = findViewById(R.id.event_list);
        eventAdapter = new EventArrayAdapter(this, dataList, this::openFragment, position -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerManageEvent.class);
            intent.putExtra("eventId", dataList.get(position).getUuid());
            startActivity(intent);
        });
        eventList.setAdapter(eventAdapter);

        // Move to Create Event Page
        bottomNav = findViewById(R.id.home_bottom_nav_bar);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.create_activity) {
                OrganizerEventsDataHolder.setDataList(dataList);
                OrganizerEventsDataHolder.setEventAdapter(eventAdapter);
                Intent intent = new Intent(OrganizerMainPage.this, CreateEventPage.class);
                startActivity(intent);
            }
            return true;
        });
    }

    // Define how to open a the even edit Fragment
    void openFragment(Integer index){
        EditEventPage fragment = new EditEventPage();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.bringToFront();
        OrganizerEventsDataHolder.setDataList(dataList);
        OrganizerEventsDataHolder.setEventAdapter(eventAdapter);
        Bundle bundle = new Bundle();
        bundle.putInt("position", index);
        fragment.setArguments(bundle);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}