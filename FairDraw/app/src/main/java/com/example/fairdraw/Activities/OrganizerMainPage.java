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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Activity for the organizer main page.
 *
 * Displays a list of events the organizer can manage. The activity listens for changes to the
 * Firestore "events" collection and updates the UI using an {@link EventArrayAdapter}.
 * Selecting an item opens an edit fragment ( {@link com.example.fairdraw.Activities.EditEventPage}).
 * The bottom navigation offers quick access to create a new event which launches
 * {@link com.example.fairdraw.Activities.CreateEventPage}.
 */
public class OrganizerMainPage extends AppCompatActivity {
    BottomNavigationView bottomNav;
    FrameLayout fragmentContainer;
    ListView eventList;
    EventArrayAdapter eventAdapter;
    FirebaseFirestore db;
    CollectionReference eventsRef;
    Integer index;

    ArrayList<Event> dataList;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Activity lifecycle entry point. Sets up the list view, adapter and a realtime listener
     * on the Firestore "events" collection.
     * <p>
     * This method also wires the bottom navigation item that launches the create-event flow.
     *
     * @param savedInstanceState previous saved state or null
     */
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
                    dataList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }
        });

        // Set up event list view
        eventList = findViewById(R.id.event_list);
        eventAdapter = new EventArrayAdapter(this, dataList);
        eventList.setAdapter(eventAdapter);

        //Open an event to edit
        eventList.setOnItemClickListener((parent, view, position, id) -> {
            openFragment(dataList.get(position));
        });

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

    /**
     * Opens the {@link EditEventPage} fragment for the provided event.
     * The fragment receives a Bundle argument named "position" indicating the index of the
     * supplied event within the current data list so it can read/update the correct model.
     *
     * @param event the Event to edit (must be present in the current dataList)
     */
    void openFragment(Event event){
        EditEventPage fragment = new EditEventPage();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.bringToFront();
        OrganizerEventsDataHolder.setDataList(dataList);
        OrganizerEventsDataHolder.setEventAdapter(eventAdapter);
        Bundle bundle = new Bundle();
        bundle.putInt("position", dataList.indexOf(event));
        fragment.setArguments(bundle);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}