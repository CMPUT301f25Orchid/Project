package com.example.fairdraw;

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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class OrganizerMainPage extends AppCompatActivity {
    BottomNavigationView bottomNav;
    FrameLayout fragmentContainer;
    ListView eventList;
    EventArrayAdapter eventAdapter;
    FirebaseFirestore db;
    CollectionReference eventsRef;

    ArrayList<Event> dataList;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");




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

        // Set up event list view
        eventList = findViewById(R.id.event_list);
        eventAdapter = new EventArrayAdapter(this, dataList);
        eventList.setAdapter(eventAdapter);

        // Populate event list with database data
        db = FirebaseFirestore.getInstance();
        dataList = EventDataHolder.getDataList();
        eventsRef = db.collection("events");
        eventsRef.addSnapshotListener((value, e) -> {
            if (e != null) {
                Log.e("Firestore", e.toString());
            }
            if (value != null && !value.isEmpty()) {
                dataList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Event event = doc.toObject(Event.class);
                    dataList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }
        });


        //Open an event to edit
        eventList.setOnItemClickListener((parent, view, position, id) -> {
            openFragment(dataList.get(position));
        });

        // Move to Create Event Page
        bottomNav = findViewById(R.id.home_bottom_nav_bar);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.create_activity) {
                EventDataHolder.setDataList(dataList);
                EventDataHolder.setEventAdapter(eventAdapter);
                Intent intent = new Intent(OrganizerMainPage.this, CreateEventPage.class);
                startActivity(intent);
            }
            return true;
        });
    }

    // Define how to open a the even edit Fragment
    void openFragment(Event event){
        EditEventPage fragment = new EditEventPage();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.bringToFront();
        EventDataHolder.setDataList(dataList);
        EventDataHolder.setEventAdapter(eventAdapter);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}