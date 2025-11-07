package com.example.fairdraw;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerActionTests {
    @Rule
    public ActivityScenarioRule<OrganizerMainPage> activityRule = new ActivityScenarioRule<>(OrganizerMainPage.class);
    @Test
    public void testOrganizerMainPageToCreateEventPage() {
        // Click the "Create Event" button
        onView(withId(R.id.create_activity)).perform(click());

        // Ensure activity open is create event page
        onView(withId(R.id.event_creation_input_layout)).check(matches(isDisplayed()));
    }
    @Test
    public void testCreateEventPageToOrganizerMainPage() {
        // Click the "Create Event" button
        onView(withId(R.id.create_activity)).perform(click());

        // Click the Home Button
        onView(withId(R.id.home_activity)).perform(click());

        // Ensure activity open is organizer main page
        onView(withId(R.id.organizer_navigation_bar)).check(matches(isDisplayed()));
    }

}
