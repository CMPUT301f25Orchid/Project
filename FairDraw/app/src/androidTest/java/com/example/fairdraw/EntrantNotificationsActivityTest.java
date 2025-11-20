package com.example.fairdraw;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fairdraw.Activities.EntrantHomeActivity;
import com.example.fairdraw.Activities.EntrantNotificationsActivity;
import com.example.fairdraw.Activities.EntrantScan;
import com.example.fairdraw.Adapters.EntrantNotificationAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation tests for EntrantNotificationsActivity.
 *
 * <p>These tests launch the {@link EntrantNotificationsActivity} and verify its key UI
 * elements and navigation behavior. Espresso Intents is used to stub outgoing
 * Intents so tests don't actually start the target Activities.</p>
 */
@RunWith(AndroidJUnit4.class)
public class EntrantNotificationsActivityTest {

    /**
     * Prepare the test environment before each test.
     *
     * <p>Initializes Espresso Intents and stubs outgoing Intents to
     * {@link EntrantHomeActivity} and {@link EntrantScan} so that intent-based
     * navigation can be asserted without launching real Activities.</p>
     */
    @Before
    public void setUp() {
        Intents.init();

        // Stub the outgoing intents so tests don't actually launch the targets
        intending(hasComponent(EntrantHomeActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
        intending(hasComponent(EntrantScan.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    /**
     * Clean up the test environment after each test.
     *
     * <p>Releases Espresso Intents to avoid leaking state between tests.</p>
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Launches {@link EntrantNotificationsActivity} and asserts that:
     * <ul>
     *   <li>The notifications RecyclerView (R.id.rvNotifications) is visible.</li>
     *   <li>The bottom navigation view (R.id.home_bottom_nav_bar) is visible.</li>
     *   <li>The RecyclerView has an adapter instance of
     *       {@link EntrantNotificationAdapter}.</li>
     * </ul>
     *
     * <p>This verifies basic UI wiring for the notifications screen and
     * ensures the adapter has been attached during onCreate/onStart.</p>
     */
    @Test
    public void onLaunch_initialUiStateIsCorrect() {
        try (ActivityScenario<EntrantNotificationsActivity> sc = ActivityScenario.launch(EntrantNotificationsActivity.class)) {
            // Visible widgets
            onView(withId(R.id.rvNotifications)).check(matches(isDisplayed()));
            onView(withId(R.id.home_bottom_nav_bar)).check(matches(isDisplayed()));

            // Sanity on adapter types
            sc.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.rvNotifications);
                RecyclerView.Adapter<?> adapter = rv.getAdapter();
                assertNotNull("RecyclerView should have an adapter set.", adapter);
                assertTrue("Adapter should be EntrantNotificationAdapter.",
                        adapter instanceof EntrantNotificationAdapter);

                BottomNavigationView bottomNav = activity.findViewById(R.id.home_bottom_nav_bar);
                assertNotNull("BottomNavigationView should be present.", bottomNav);
            });
        }
    }


}
