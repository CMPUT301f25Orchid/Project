// java
// File: `app/src/androidTest/java/com/example/fairdraw/EntrantHomeActivityTest.java`


package com.example.fairdraw;
import com.example.fairdraw.DBs.OrganizerDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.DBs.EventDB;



import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import com.example.fairdraw.Activities.EntrantHomeActivity;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(AndroidJUnit4.class)
public class EntrantHomeActivityTest {

    @Test
    public void displayEvents_addsEventCards() {
        // Create a mocked Event with the getters used by displayEvents
        Event fake = mock(Event.class);
        when(fake.getTitle()).thenReturn("Test Event");
        when(fake.getLocation()).thenReturn("Test Location");
        when(fake.getTime()).thenReturn(new Date());
        when(fake.getEnrolledList()).thenReturn(new ArrayList<>());
        when(fake.getCapacity()).thenReturn(10);
        when(fake.getWaitingList()).thenReturn(new ArrayList<>());
        when(fake.getPrice()).thenReturn(0.0F);
        when(fake.getState()).thenReturn(EventState.PUBLISHED);
        when(fake.getUuid()).thenReturn("test-id");
        when(fake.getDescription()).thenReturn("test description");

        // Launch activity and call displayEvents on the UI thread
        try (ActivityScenario<EntrantHomeActivity> scenario = ActivityScenario.launch(EntrantHomeActivity.class)) {
            scenario.onActivity(activity -> {
                // Clear the container first
                android.widget.LinearLayout container = activity.findViewById(R.id.event_list_container);
                if (container != null) {
                    container.removeAllViews();
                }

                // Call the method under test
                activity.displayEvents(Arrays.asList(fake));

                // Assert that at least one child (card) was added
                int childCount = container != null ? container.getChildCount() : 0;
                assertTrue("Expected at least one event card to be displayed", childCount > 0);
            });
        }
    }
}
