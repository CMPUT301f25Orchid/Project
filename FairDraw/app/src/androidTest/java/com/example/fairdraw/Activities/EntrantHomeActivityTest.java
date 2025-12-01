package com.example.fairdraw.Activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;

import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fairdraw.R;

import org.hamcrest.Matcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Section B: Browse events & open event details (Entrant).
 *
 * User stories (approx):
 *  - US 01.03.x: Entrant can browse community events.
 *  - US 01.04.x: Entrant can open event details from the list.
 *
 * This class focuses on:
 *  - Basic UI elements of the EntrantHome screen.
 *  - Navigation from an event card to EntrantEventDetails.
 */
@RunWith(AndroidJUnit4.class)
public class EntrantHomeActivityTest {

    @Rule
    public ActivityScenarioRule<EntrantHomeActivity> activityRule =
            new ActivityScenarioRule<>(EntrantHomeActivity.class);

    @Before
    public void setUp() {
        // Enable Espresso-Intents so we can verify navigation.
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // ---------------------------------------------------------------------
    // Helper matcher: "firstChildOf(parentMatcher)"
    // ---------------------------------------------------------------------

    /**
     * Returns a matcher for the first child of a view matched by parentMatcher.
     *
     * This is a simple way to "click the first card" inside event_list_container,
     * without having to know its exact id. The first direct child will be the
     * first event card you add programmatically.
     */
    private static Matcher<View> firstChildOf(final Matcher<View> parentMatcher) {
        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("with first child of parent matching: ");
                parentMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                if (!(parent instanceof ViewGroup)) {
                    return false;
                }

                ViewGroup parentGroup = (ViewGroup) parent;
                // Parent must match
                if (!parentMatcher.matches(parentGroup)) {
                    return false;
                }

                // View must be the first child in that parent
                return parentGroup.getChildCount() > 0 && parentGroup.getChildAt(0).equals(view);
            }
        };
    }

    /**
     * Basic smoke test:
     *  - "Community Events" title is visible.
     *  - "Filter Events" button is visible.
     *  - Bottom nav is visible.
     *
     * Maps to: browse events screen shows core UI for entrants.
     */
    @Test
    public void homeScreen_showsTitleFilterAndBottomNav() {
        // Title text
        onView(withText("Community Events"))
                .check(matches(isDisplayed()));

        // Filter button
        onView(withId(R.id.filterEventsBtn))
                .check(matches(isDisplayed()));

        // Bottom nav include
        onView(withId(R.id.bottom_nav))
                .check(matches(isDisplayed()));
    }

    /**
     * Browse events:
     *  - After EntrantHomeActivity loads, the event list container should be visible
     *    and contain at least one event card.
     *
     * This covers the "browse events" part of Section B.
     */
    @Test
    public void eventsList_showsAtLeastOneEventCardAfterLoad() {
        // Give Firestore / async loading a little time
        SystemClock.sleep(3000);

        // 1) The list container itself is visible
        onView(withId(R.id.event_list_container))
                .check(matches(isDisplayed()));

        // 2) The first child in the container (first event card) is visible
        onView(firstChildOf(withId(R.id.event_list_container)))
                .check(matches(isDisplayed()));

        // 3) Sanity check: that first card contains the "View Details" button
        onView(allOf(
                withId(R.id.view_details_button),
                isDescendantOfA(firstChildOf(withId(R.id.event_list_container)))
        )).check(matches(isDisplayed()));
    }

    /**
     * Happy path:
     *  - Wait for events list to be populated.
     *  - Tap the first visible event card.
     *  - Expect navigation to EntrantEventDetails.
     *
     * Maps to: "Browse events & open event details".
     *
     * NOTE: This assumes that:
     *  - the event card root view has id R.id.event_card_root, OR
     *  - you update the matcher in clickFirstEventCard() to match your actual card root.
     */
    @Test
    public void clickingEventCard_navigatesToEntrantEventDetails() {
        // Give Firestore or your async loading a moment.
        SystemClock.sleep(3000);

        // Confirm the event list container is displayed and has children
        onView(withId(R.id.event_list_container))
                .check(matches(isDisplayed()));

        onView(withId(R.id.event_list_container))
                .check(matches(withChild(withId(R.id.organizerEventCard))));

        // Click the "View Details" button on the FIRST card in the container
        onView(allOf(
                withId(R.id.view_details_button),
                isDescendantOfA(firstChildOf(withId(R.id.event_list_container)))
        )).perform(click());

        // Verify navigation to EntrantEventDetails
        intended(hasComponent(EntrantEventDetails.class.getName()));
    }
}
