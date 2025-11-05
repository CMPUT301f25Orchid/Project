package com.example.fairdraw;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrganizerMainPage extends AppCompatActivity {
    BottomNavigationView bottomNav;
    FrameLayout fragmentContainer;
    ListView eventList;
    EventArrayAdapter eventAdapter;

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
        // Move to Create Event Page
        bottomNav = findViewById(R.id.home_bottom_nav_bar);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.create_activity) {

                Intent intent = new Intent(OrganizerMainPage.this, CreateEventPage.class);
                startActivity(intent);
            }
            return true;
        });
        // Populate event list with filler data
        dataList = new ArrayList<>();
        dataList.add(new Event("Event 1", "Description 1", 100,
                new Date("10/10/2023"), new Date("11/10/2023"), "Location 1", "Organizer 1",
                10.0f, "poster_path_1", "qr_slug_1"));
        dataList.add(new Event("Event 2", "Description 2", 200, new Date("11/10/2023"), new Date("12/10/2023"),
                "Location 2", "Organizer 2", 20.0f, "poster_path_2", "qr_slug_2"));
        dataList.add(new Event("Event 3", "Description 3", 300, new Date("12/10/2023"), new Date("13/10/2023"),
                "Location 3", "Organizer 3", 30.0f, "poster_path_3", "qr_slug_3"));

        // Set up event list view
        eventList = findViewById(R.id.event_list);
        eventAdapter = new EventArrayAdapter(this, dataList);
        eventList.setAdapter(eventAdapter);
        //Open an event to edit
        eventList.setOnItemClickListener((parent, view, position, id) -> {
            openFragment(dataList.get(position));
        });



    }

    // Define how to open a the even edit Fragment
    void openFragment(Event event){
        EditEventPage fragment = new EditEventPage();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.bringToFront();
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        fragment.setArguments(bundle);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}