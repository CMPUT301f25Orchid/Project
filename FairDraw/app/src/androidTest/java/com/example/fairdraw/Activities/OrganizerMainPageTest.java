package com.example.fairdraw.Activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * UI/intent tests for OrganizerMainPage.
 * Covers: US 02.02.01 – View entrants for an event (via manage event).
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerMainPageTest {
    @Rule
    public ActivityScenarioRule<OrganizerMainPage> activityRule =
            new ActivityScenarioRule<>(OrganizerMainPage.class);

    @Before
    public void setUp() {
        // Initialize Espresso-Intents so we can assert on started activities
        Intents.init();
    }

    @After
    public void tearDown() {
        // Clean up Intents after each test
        Intents.release();
    }

    /**
     * US 02.02.01
     * When the organizer taps an event card in the list, the app should open
     * OrganizerManageEvent with the correct eventId extra.
     */
    @Test
    public void clickEventCard_opensOrganizerManageEvent_withEventId() {
        final String testEventId = "testEventId-123";

        // Arrange: inject a fake event into the adapter so the RecyclerView has at least one item
        activityRule.getScenario().onActivity(activity -> {
            // Assumes Event has a no-arg constructor and setters.
            Event dummyEvent = new Event();
            dummyEvent.setUuid(testEventId);
            dummyEvent.setTitle("Test Event");
            dummyEvent.setLocation("Test Location");

            List<Event> events = new ArrayList<>();
            events.add(dummyEvent);

            // eventAdapter is package-private in OrganizerMainPage,
            // so this works as long as the test is in the same package.
            activity.eventAdapter.submitList(events);
        });

        // Act: click the first event card in the list
        onView(withId(R.id.event_list))
                .perform(actionOnItemAtPosition(0, click()));

        // Assert: OrganizerManageEvent is launched with the correct eventId extra
        intended(allOf(
                hasComponent(OrganizerManageEvent.class.getName()),
                hasExtra("eventId", testEventId)
        ));
    }
}
