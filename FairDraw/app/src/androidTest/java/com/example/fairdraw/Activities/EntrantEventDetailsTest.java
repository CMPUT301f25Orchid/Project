package com.example.fairdraw.Activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.os.SystemClock;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fairdraw.Fragments.QrCodeFragment;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for EntrantEventDetails.
 *
 * Covers:
 *  - US 02.01.01 – Tap the QR button to open QrCodeFragment.
 */
@RunWith(AndroidJUnit4.class)
public class EntrantEventDetailsTest {

    @Rule
    public ActivityScenarioRule<EntrantEventDetails> activityRule =
            new ActivityScenarioRule<>(
                    new Intent(
                            ApplicationProvider.getApplicationContext(),
                            EntrantEventDetails.class
                    ).putExtra("event_id", "test-event-123")
            );

    /**
     * US 02.01.01 – Create event & generate QR promotional code.
     *
     * Behaviour under test:
     *  - When the entrant taps the "View QR Code" button,
     *    a QrCodeFragment dialog is shown for this event.
     */
    @Test
    public void tappingQrButton_opensQrCodeFragment() {
        // Arrange: simulate a loaded Event and wire the QR button using the real helper
        activityRule.getScenario().onActivity(activity -> {
            Event fakeEvent = new Event();
            fakeEvent.setUuid("test-event-123");
            fakeEvent.setTitle("Test Event QR");

            // Use the same logic production code uses to attach the QR listener
            activity.setupQrButton(fakeEvent);
        });

        // Act: click the QR button (scrollTo just in case it's off-screen)
        onView(withId(R.id.btnViewQrCode))
                .perform(scrollTo(), click());

        // Wait a moment for the fragment transaction to complete
        SystemClock.sleep(2000);

        // Assert: QrCodeFragment has been added with the expected tag
        activityRule.getScenario().onActivity(activity -> {
            FragmentManager fm = activity.getSupportFragmentManager();
            Fragment fragment = fm.findFragmentByTag("QrCodeFragment");
            assertNotNull("QrCodeFragment should be shown after QR button click", fragment);
            assertTrue("Fragment should be an instance of QrCodeFragment",
                    fragment instanceof QrCodeFragment);
        });
    }
}
