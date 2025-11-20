package com.example.fairdraw;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fairdraw.Activities.OrganizerMainPage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.fairdraw.Activities.OrganizerMainPage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerActionTests {
    @Rule
    public ActivityScenarioRule<OrganizerMainPage> activityRule = new ActivityScenarioRule<>(OrganizerMainPage.class);

    // Test organizer main page goes to create event page
    @Test
    public void testOrganizerMainPageToCreateEventPage() {
        // Click the "Create Event" button
        onView(withId(R.id.create_activity)).perform(click());

        // Ensure activity open is create event page
        onView(withId(R.id.event_creation_input_layout)).check(matches(isDisplayed()));
    }

    // Test create event page goes to organizer main page
    @Test
    public void testCreateEventPageToOrganizerMainPage() {
        // Click the "Create Event" button
        onView(withId(R.id.create_activity)).perform(click());

        // Click the Home Button
        onView(withId(R.id.home_activity)).perform(click());

        // Ensure activity open is organizer main page
        onView(withId(R.id.organizer_navigation_bar)).check(matches(isDisplayed()));
    }

    // Test organizer main page goes to event edit page
    @Test
    public void testOrganizerMainPageToEventEditPage() {
        // Click the "Create Event" button
        onView(withId(R.id.create_activity)).perform(click());

        // Enter event details excluding poster
        onView(withId(R.id.event_title)).perform(typeText("Test Event"));
        onView(withId(R.id.event_description)).perform(typeText("Test Description"));
        onView(withId(R.id.event_capacity)).perform(typeText("100"));
        onView(withId(R.id.event_location)).perform(typeText("Test Location"));
        onView(withId(R.id.event_price)).perform(typeText("1.01"));
        onView(withId(R.id.event_start_date)).perform(typeText("01/03/2023"));
        onView(withId(R.id.event_end_date)).perform(typeText("01/04/2023"));
        onView(withId(R.id.event_registration_start_date)).perform(typeText("01/01/2023"));
        onView(withId(R.id.event_registration_end_date)).perform(typeText("01/02/2023"));
        onView(withId(R.id.geo_yes)).perform(click());
        onView(withId(R.id.event_limit)).perform(typeText("10"));
        onView(withId(R.id.confirm_create_event)).perform(click());

        //Return to organizer home page
        onView(withId(R.id.home_activity)).perform(click());

        // Click the event
        onView(withText("Test Event")).perform(click());

        //Ensure activity open is event edit page
        onView(withId(R.id.event_creation_input_layout)).check(matches(isDisplayed()));
    }

    //Test event edit page goes to organizer main page
    @Test
    public void testEventEditPageToOrganizerMainPage() {
        // TODO create the event edit page test

    }

    // Test to ensure created event is displayed in organizer main page
    @Test
    public void testCreatedEventDisplayedInOrganizerMainPage() {
        // Click the "Create Event" button
        onView(withId(R.id.create_activity)).perform(click());

        // Enter event details excluding poster
        onView(withId(R.id.event_title)).perform(typeText("Test Event"));
        onView(withId(R.id.event_description)).perform(typeText("Test Description"));
        onView(withId(R.id.event_capacity)).perform(typeText("100"));
        onView(withId(R.id.event_location)).perform(typeText("Test Location"));
        onView(withId(R.id.event_price)).perform(typeText("1.01"));
        onView(withId(R.id.event_start_date)).perform(typeText("01/03/2023"));
        onView(withId(R.id.event_end_date)).perform(typeText("01/04/2023"));
        onView(withId(R.id.event_registration_start_date)).perform(typeText("01/01/2023"));
        onView(withId(R.id.event_registration_end_date)).perform(typeText("01/02/2023"));
        onView(withId(R.id.geo_yes)).perform(click());
        onView(withId(R.id.event_limit)).perform(typeText("10"));
        onView(withId(R.id.confirm_create_event)).perform(click());

        //Return to organizer home page
        onView(withId(R.id.home_activity)).perform(click());

        // Ensure event is displayed in organizer main page
        onView(withText("Test Event")).check(matches(isDisplayed()));

        // Remove test event
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query deleteQuery = db.collection("events").whereEqualTo("title", "Test Event").limit(1);
        deleteQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().delete();
                }
            }
        });
    }

    // Test to ensure event edit page is prefilled with information
    @Test
    public void testEventEditPageIsPrefilled() {
        // Click the "Create Event" button
        onView(withId(R.id.create_activity)).perform(click());

        // Enter event details excluding poster
        onView(withId(R.id.event_title)).perform(typeText("Test Event"));
        onView(withId(R.id.event_description)).perform(typeText("Test Description"));
        onView(withId(R.id.event_capacity)).perform(typeText("100"));
        onView(withId(R.id.event_location)).perform(typeText("Test Location"));
        onView(withId(R.id.event_price)).perform(typeText("1.01"));
        onView(withId(R.id.event_start_date)).perform(typeText("01/03/2023"));
        onView(withId(R.id.event_end_date)).perform(typeText("01/04/2023"));
        onView(withId(R.id.event_registration_start_date)).perform(typeText("01/01/2023"));
        onView(withId(R.id.event_registration_end_date)).perform(typeText("01/02/2023"));
        onView(withId(R.id.geo_yes)).perform(click());
        onView(withId(R.id.event_limit)).perform(typeText("10"));
        onView(withId(R.id.confirm_create_event)).perform(click());

        //Return to organizer home page
        onView(withId(R.id.home_activity)).perform(click());

        // Click the event
        onView(withText("Test Event")).perform(click());

        // Ensure event is displayed with correct information
        onView(withId(R.id.event_title)).check(matches(withText("Test Event")));
        onView(withId(R.id.event_description)).check(matches(withText("Test Description")));
        onView(withId(R.id.event_capacity)).check(matches(withText("100")));
        onView(withId(R.id.event_location)).check(matches(withText("Test Location")));
        onView(withId(R.id.event_price)).check(matches(withText("1.01")));
        onView(withId(R.id.event_start_date)).check(matches(withText("01/03/2023")));
        onView(withId(R.id.event_end_date)).check(matches(withText("01/04/2023")));
        onView(withId(R.id.event_registration_start_date)).check(matches(withText("01/01/2023")));
        onView(withId(R.id.event_registration_end_date)).check(matches(withText("01/02/2023")));
        onView(withId(R.id.geo_yes)).check(matches(isSelected()));
        onView(withId(R.id.event_limit)).check(matches(withText("10")));

        // Remove test event
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query deleteQuery = db.collection("events").whereEqualTo("title", "Test Event").limit(1);
        deleteQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().delete();
                }
            }
        });
    }

    // Test to ensure event can be edited properly
    @Test
    public void testEventCanBeEdited() {
        // Click the "Create Event" button
        onView(withId(R.id.create_activity)).perform(click());

        // Enter event details excluding poster
        onView(withId(R.id.event_title)).perform(typeText("Test Event"));
        onView(withId(R.id.event_description)).perform(typeText("Test Description"));
        onView(withId(R.id.event_capacity)).perform(typeText("100"));
        onView(withId(R.id.event_location)).perform(typeText("Test Location"));
        onView(withId(R.id.event_price)).perform(typeText("1.01"));
        onView(withId(R.id.event_start_date)).perform(typeText("01/03/2023"));
        onView(withId(R.id.event_end_date)).perform(typeText("01/04/2023"));
        onView(withId(R.id.event_registration_start_date)).perform(typeText("01/01/2023"));
        onView(withId(R.id.event_registration_end_date)).perform(typeText("01/02/2023"));
        onView(withId(R.id.geo_yes)).perform(click());
        onView(withId(R.id.event_limit)).perform(typeText("10"));
        onView(withId(R.id.confirm_create_event)).perform(click());

        //Return to organizer home page
        onView(withId(R.id.home_activity)).perform(click());

        // Click the event
        onView(withText("Test Event")).perform(click());

        // Edit event details
        onView(withId(R.id.event_title)).perform(typeText("Test Event Edited"));
        onView(withId(R.id.event_description)).perform(typeText("Test Description Edited"));

        // Save changes
        onView(withId(R.id.confirm_event_edit)).perform(click());

        // Ensure event is displayed with correct information
        onView(withId(R.id.event_title)).check(matches(withText("Test Event Edited")));

        // Remove test event
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query deleteQuery = db.collection("events").whereEqualTo("title", "Test Event").limit(1);
        deleteQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().delete();
                }
            }
        });
    }
}
