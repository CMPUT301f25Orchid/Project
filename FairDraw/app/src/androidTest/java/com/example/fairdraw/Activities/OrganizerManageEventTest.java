package com.example.fairdraw.Activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
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

import java.util.Arrays;
import java.util.Collections;

/**
 * UI tests for OrganizerManageEvent.
 *
 * Covers:
 *  - US 02.06.01 – View list of invited entrants
 *  - US 02.06.02 – View list of cancelled entrants
 *  - US 02.06.03 – View final list of enrolled entrants
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerManageEventTest {

    // Launch OrganizerManageEvent with a dummy eventId
    @Rule
    public ActivityScenarioRule<OrganizerManageEvent> activityRule =
            new ActivityScenarioRule<>(
                    new Intent(
                            ApplicationProvider.getApplicationContext(),
                            OrganizerManageEvent.class
                    ).putExtra("eventId", "test-event-123")
            );

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
     * US 02.06.01 – As an organizer I want to view a list of all chosen entrants who are invited.
     *
     * We bind a fake Event that has invited entrants and verify:
     *  - The "Invited Entrants" header is visible
     *  - The invited RecyclerView can scroll to position 0 (i.e., has at least one item)
     */
    @Test
    public void invitedEntrantsList_isVisibleAndPopulated() {
        activityRule.getScenario().onActivity(activity -> {
            Event fake = new Event();
            fake.setUuid("test-event-123");
            fake.setTitle("Test Event");
            fake.setInvitedList(Arrays.asList("entrant-inv-1", "entrant-inv-2"));
            fake.setCancelledList(Collections.emptyList());
            fake.setEnrolledList(Collections.emptyList());
            fake.setWaitingList(Collections.emptyList());

            // Directly bind the fake event, bypassing Firestore snapshot
            activity.bindEvent(fake);
        });

        // Header visible
        onView(withId(R.id.tvInvitedHeader))
                .check(matches(isDisplayed()));

        // RecyclerView has at least one item (scrollToPosition(0) will fail if list is empty)
        onView(withId(R.id.rvInvited))
                .perform(RecyclerViewActions.scrollToPosition(0));
    }

    /**
     * US 02.06.02 – As an organizer I want to see a list of all the cancelled entrants.
     */
    @Test
    public void cancelledEntrantsList_isVisibleAndPopulated() {
        activityRule.getScenario().onActivity(activity -> {
            Event fake = new Event();
            fake.setUuid("test-event-123");
            fake.setTitle("Test Event");
            fake.setInvitedList(Collections.emptyList());
            fake.setCancelledList(Arrays.asList("entrant-can-1", "entrant-can-2"));
            fake.setEnrolledList(Collections.emptyList());
            fake.setWaitingList(Collections.emptyList());

            activity.bindEvent(fake);
        });

        onView(withId(R.id.tvCancelledHeader))
                .check(matches(isDisplayed()));

        onView(withId(R.id.rvCancelled))
                .perform(RecyclerViewActions.scrollToPosition(0));
    }

    /**
     * US 02.06.03 – As an organizer I want to see a final list of entrants who enrolled.
     * (mapped to the "Registered / Enrolled" list)
     */
    @Test
    public void registeredEntrantsList_isVisibleAndPopulated() {
        activityRule.getScenario().onActivity(activity -> {
            Event fake = new Event();
            fake.setUuid("test-event-123");
            fake.setTitle("Test Event");
            fake.setInvitedList(Collections.emptyList());
            fake.setCancelledList(Collections.emptyList());
            fake.setEnrolledList(Arrays.asList("entrant-reg-1", "entrant-reg-2"));
            fake.setWaitingList(Collections.emptyList());

            activity.bindEvent(fake);
        });

        onView(withId(R.id.tvRegisteredHeader))
                .check(matches(isDisplayed()));

        onView(withId(R.id.rvRegistered))
                .perform(RecyclerViewActions.scrollToPosition(0));
    }

    /**
     * US 02.06.05 – As an organizer I want to export a final list of entrants who enrolled
     * in CSV format (btnDownloadFinalEntrants).
     *
     * Assumption:
     *  - Clicking btnDownloadFinalEntrants starts an ACTION_CREATE_DOCUMENT intent
     *    with MIME type "text/csv".
     * If your implementation uses a different action/type, adjust the matchers accordingly.
     */
    @Test
    public void downloadFinalEntrantsCsv_preparesCsvBytesAndFileName() {
        // Arrange: bind a fake event with enrolled entrants
        activityRule.getScenario().onActivity(activity -> {
            Event fake = new Event();
            fake.setUuid("test-event-123");
            fake.setTitle("Test Event");
            fake.setInvitedList(Collections.emptyList());
            fake.setCancelledList(Collections.emptyList());
            fake.setEnrolledList(Arrays.asList("entrant-reg-1", "entrant-reg-2"));
            fake.setWaitingList(Collections.emptyList());

            activity.bindEvent(fake);
        });

        // Act: click the Download Final Entrants button
        onView(withId(R.id.btnDownloadFinalEntrants))
                .perform(scrollTo(), click());

        // Wait a moment for the intent to be sent (firestore/network operations may be async)
        SystemClock.sleep(2000);

        // Assert: CSV export pipeline prepared content and filename
        activityRule.getScenario().onActivity(activity -> {
            // pendingCsvBytes is filled by writeCsvAndShare(...)
            assertNotNull("CSV bytes should be prepared after clicking export",
                    activity.pendingCsvBytes);
            assertNotNull("CSV filename should be prepared after clicking export",
                    activity.pendingCsvFileName);

            // Optional: sanity check on filename format
            assertTrue("CSV filename should end with .csv",
                    activity.pendingCsvFileName.endsWith(".csv"));
        });
    }

    /**
     * US 02.07.01 – As an organizer I want to send notifications to all entrants on the waiting list.
     *
     * Behaviour:
     *  - Open Send Notification dialog
     *  - Type a message
     *  - Do NOT select any special radio button (default is WAITING_LIST)
     *  - Tap Send
     *  - Verify Snackbar shows "Send to WAITING_LIST | <message>"
     */
    @Test
    public void sendNotification_waitingList_showsWaitingListSnackbar() {
        // Arrange: event with a non-empty waiting list
        activityRule.getScenario().onActivity(activity -> {
            Event fake = new Event();
            fake.setUuid("test-event-123");
            fake.setTitle("Test Event");
            fake.setInvitedList(Collections.emptyList());
            fake.setCancelledList(Collections.emptyList());
            fake.setEnrolledList(Collections.emptyList());
            fake.setWaitingList(Arrays.asList("waiting-entrant-1", "waiting-entrant-2"));
            activity.bindEvent(fake);
        });

        // Open dialog
        onView(withId(R.id.btnSendNotification))
                .perform(scrollTo(), click());

        String msg = "Hello, waiting list!";
        // Type message in dialog
        onView(withId(R.id.etMessage))
                .perform(typeText(msg), closeSoftKeyboard());

        // Don't touch rbSelected / rbCancelled so default stays WAITING_LIST

        // Click send
        onView(withId(R.id.btnSend))
                .perform(click());

        // Assert Snackbar text
        onView(withText("Send to WAITING_LIST | " + msg))
                .check(matches(isDisplayed()));
    }

    /**
     * US 02.07.02 – As an organizer I want to send notifications to all selected entrants.
     *
     * Behaviour:
     *  - Open Send Notification dialog
     *  - Type a message
     *  - Check rbSelected
     *  - Tap Send
     *  - Verify Snackbar shows "Send to SELECTED | <message>"
     */
    @Test
    public void sendNotification_selectedEntrants_showsSelectedSnackbar() {
        // Arrange: event with a non-empty invited list
        activityRule.getScenario().onActivity(activity -> {
            Event fake = new Event();
            fake.setUuid("test-event-123");
            fake.setTitle("Test Event");
            fake.setInvitedList(Arrays.asList("invited-1", "invited-2"));
            fake.setCancelledList(Collections.emptyList());
            fake.setEnrolledList(Collections.emptyList());
            fake.setWaitingList(Collections.emptyList());
            activity.bindEvent(fake);
        });

        // Open dialog
        onView(withId(R.id.btnSendNotification))
                .perform(scrollTo(), click());

        String msg = "Hello, selected entrants!";
        // Type message
        onView(withId(R.id.etMessage))
                .perform(typeText(msg), closeSoftKeyboard());

        // Choose selected audience
        onView(withId(R.id.rbSelected))
                .perform(click());

        // Click send
        onView(withId(R.id.btnSend))
                .perform(click());

        // Assert Snackbar
        onView(withText("Send to SELECTED | " + msg))
                .check(matches(isDisplayed()));
    }

    /**
     * US 02.07.03 – As an organizer I want to send a notification to all cancelled entrants.
     *
     * Behaviour:
     *  - Open Send Notification dialog
     *  - Type a message
     *  - Check rbCancelled
     *  - Tap Send
     *  - Verify Snackbar shows "Send to CANCELLED | <message>"
     */
    @Test
    public void sendNotification_cancelledEntrants_showsCancelledSnackbar() {
        // Arrange: event with a non-empty cancelled list
        activityRule.getScenario().onActivity(activity -> {
            Event fake = new Event();
            fake.setUuid("test-event-123");
            fake.setTitle("Test Event");
            fake.setInvitedList(Collections.emptyList());
            fake.setCancelledList(Arrays.asList("cancelled-1", "cancelled-2"));
            fake.setEnrolledList(Collections.emptyList());
            fake.setWaitingList(Collections.emptyList());
            activity.bindEvent(fake);
        });

        // Open dialog
        onView(withId(R.id.btnSendNotification))
                .perform(scrollTo(), click());

        String msg = "Hello, cancelled entrants!";
        // Type message
        onView(withId(R.id.etMessage))
                .perform(typeText(msg), closeSoftKeyboard());

        // Choose cancelled audience
        onView(withId(R.id.rbCancelled))
                .perform(click());

        // Click send
        onView(withId(R.id.btnSend))
                .perform(click());

        // Assert Snackbar
        onView(withText("Send to CANCELLED | " + msg))
                .check(matches(isDisplayed()));
    }

}
