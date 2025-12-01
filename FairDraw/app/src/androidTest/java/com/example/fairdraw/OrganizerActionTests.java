package com.example.fairdraw;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.fairdraw.Activities.OrganizerMainPage;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.OrganizerEventsDataHolder;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class OrganizerActionTests {
    @Rule
    public ActivityScenarioRule<OrganizerMainPage> activityRule = new ActivityScenarioRule<>(OrganizerMainPage.class);


    // Create Event for each test
    @BeforeClass
    public static void setUp() {

        // Create new event and add to database
        String deviceId = DevicePrefsManager.getDeviceId(InstrumentationRegistry.getInstrumentation().getTargetContext());
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Event event = new Event();
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setCapacity(100);
        event.setLocation("Test Location");
        event.setPrice(1.01f);
        try {
            event.setStartDate(dateFormat.parse("12/12/2020"));
            event.setEndDate(dateFormat.parse("12/12/2020"));
            event.setEventOpenRegDate(dateFormat.parse("12/12/2020"));
            event.setEventCloseRegDate(dateFormat.parse("12/12/2020"));
        }
        catch (Exception ignored) {}
        event.setGeolocation(true);
        event.setWaitingListLimit(10);
        event.setUuid(deviceId);
        event.setOrganizer(deviceId);
        event.setInvitedList(new ArrayList<>());
        event.setCancelledList(new ArrayList<>());
        event.setEnrolledList(new ArrayList<>());
        event.setWaitingList(new ArrayList<>());
        EventDB.addEvent(event, success -> {});
        OrganizerEventsDataHolder.addEvent(event);
    }

    // Remove Created Event
    @AfterClass
    public static void tearDown() {
        final String deviceId = DevicePrefsManager.getDeviceId(InstrumentationRegistry.getInstrumentation().getTargetContext());
        // Remove test event
        EventDB.deleteEvent(deviceId, success -> {});

    }

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
        // Click the "Edit Event" button
        onView(withId(R.id.event_edit_button)).perform(click());

        //Ensure activity open is event edit page
        onView(withId(R.id.edit_event_input_layout)).check(matches(isDisplayed()));
    }

    //Test event edit page goes to organizer main page
    @Test
    public void testEventEditPageToOrganizerMainPage() {
        // Click the "Edit Event" button
        onView(withId(R.id.event_edit_button)).perform(click());

        // Click cancel button
        onView(withId(R.id.cancel_event_update)).perform(click());

        // Ensure activity open is organizer main page
        onView(withId(R.id.organizer_navigation_bar)).check(matches(isDisplayed()));
    }

    // Test to ensure created event is displayed in organizer main page
    @Test
    public void testCreatedEventDisplayedInOrganizerMainPage() {

        // Ensure event is displayed in organizer main page
        onView(withText("Test Event")).check(matches(isDisplayed()));

    }

    @Test
    public void testOrganizerMainPageToEventManagePage() {
        // Open Manage Page
        onView(withId(R.id.event_content_title)).perform(click());

        // Check if event manage page is displayed
        onView(withId(R.id.main)).check(matches(isDisplayed()));

    }

    @Test
    public void testEventManagePageToOrganizerMainPage() {
        // Open Manage Page
        onView(withId(R.id.event_content_title)).perform(click());

        // Click back button
        onView(withId(R.id.home_activity)).perform(click());

        // Check if organizer main page is displayed
        onView(withId(R.id.organizer_navigation_bar)).check(matches(isDisplayed()));
    }

    @Test
    public void testEventManagePageToCSVPage() {
        // Open Manage Page
        onView(withId(R.id.event_content_title)).perform(click());

        // Click CSV button
        onView(withId(R.id.btnReturn)).perform(scrollTo()).perform(click());

        // Check if CSV page is displayed
        onView(withId(R.id.final_page)).check(matches(isDisplayed()));
    }

    @Test
    public void testCSVPageToEventManagePage() {
        // Open Manage Page
        onView(withId(R.id.event_content_title)).perform(click());

        // Click CSV button
        onView(withId(R.id.btnReturn)).perform(scrollTo()).perform(click());

        // Click back button
        onView(withId(R.id.btnReturn)).perform(click());

        // Check if event manage page is displayed
        onView(withId(R.id.main)).check(matches(isDisplayed()));
    }

}
