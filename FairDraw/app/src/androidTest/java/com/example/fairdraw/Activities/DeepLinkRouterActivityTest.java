package com.example.fairdraw.Activities;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fairdraw.ServiceUtility.DeepLinkUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Section C – Deep-link / QR route into event details.
 *
 * This test simulates the flow used when a QR code is scanned:
 *  - Create a deep link URI using DeepLinkUtil (same as CreateEventPage).
 *  - Launch DeepLinkRouterActivity with ACTION_VIEW.
 *  - Verify that it forwards to EntrantEventDetails with the correct event_id.
 *
 * US 01.06.01 — “View event details by scanning the promotional QR code.”
 */
@RunWith(AndroidJUnit4.class)
public class DeepLinkRouterActivityTest {
    private static final String TEST_EVENT_ID = "6ddb9527-c996-48c4-8d3e-39415586ca50";

    @Before
    public void setUp() {
        // Enable Espresso-Intents to capture outgoing startActivity() calls
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Happy path:
     *  - Build a deep link to EntrantEventDetails with an event_id extra.
     *  - Launch DeepLinkRouterActivity with ACTION_VIEW and that URI.
     *  - Expect navigation to EntrantEventDetails with the same event_id.
     *
     * Maps to: Section C — Deep-link / QR route into event details.
     */
    @Test
    public void deepLink_toEventDetails_routesToEntrantEventDetails_withEventId() {
        // --- Arrange: build the same deep link CreateEventPage would build ---
        Bundle extras = new Bundle();
        extras.putString("event_id", TEST_EVENT_ID);

        // Uses your production deep link builder
        Uri deepLinkUri = DeepLinkUtil.buildLink(EntrantEventDetails.class, extras);

        Intent viewIntent = new Intent(Intent.ACTION_VIEW, deepLinkUri);

        // --- Act: launch DeepLinkRouterActivity with the deep link intent ---
        try (ActivityScenario<DeepLinkRouterActivity> scenario =
                     ActivityScenario.launch(viewIntent)) {

            // Wait for a brief moment to allow forwarding to occur
            SystemClock.sleep(1000);

            // --- Assert: router forwards to EntrantEventDetails with correct extra ---
            intended(hasComponent(EntrantEventDetails.class.getName()));
            intended(hasExtra("event_id", TEST_EVENT_ID));
        }
    }
}
