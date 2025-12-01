package com.example.fairdraw;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.fairdraw.Activities.EditProfileActivity;
import com.example.fairdraw.Activities.EntrantHomeActivity;
import com.example.fairdraw.Activities.ProfileActivity;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Models.User;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for the ProfileActivity.
 * These tests verify the UI and functionality of the user profile screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileActivityTest {

    // Mock data for our test user
    private static String testDeviceId;
    private static final String MOCK_FCM_TOKEN = "test-fcm-token";
    private User mockUser;

    /**
     * This rule defines how the Activity under test is launched.
     * The Intent creation is deferred until the @Before method, after the deviceId is set up.
     */
    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule = new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), ProfileActivity.class));

    /**
     * This method runs before each test. It sets up the test environment.
     */
    @Before
    public void setUp() {
        // 1. Initialize Espresso-Intents to allow for intent verification.
        Intents.init();

        // 2. Get the device ID using the existing manager. This ensures an ID is generated and saved
        // to SharedPreferences, which the Activity will then read on launch.
        Context context = ApplicationProvider.getApplicationContext();
        testDeviceId = DevicePrefsManager.getDeviceId(context);

        // 3. Create a mock user with the generated device ID.
        mockUser = new User("John Doe", "john.doe@example.com", "123-456-7890", testDeviceId, MOCK_FCM_TOKEN);

        // 4. Add our mock user to Firestore. This ensures the activity has data to display.
        UserDB.upsertUser(mockUser, (ok, e) -> {});

        // Add a brief pause to allow Firestore data to be written and read by the activity.
        try {
            Thread.sleep(1500); // Increased slightly for reliability
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method runs after each test. It cleans up the test environment.
     */
    @After
    public void tearDown() {
        // 1. Release Espresso-Intents.
        Intents.release();
        // 2. Delete the mock user from Firestore to ensure a clean state for the next test.
        if (testDeviceId != null) {
            UserDB.deleteUser(testDeviceId, (ok, e) -> {});
        }
    }

    /**
     * Tests if the user's profile data (name, email, phone) is correctly displayed upon launch.
     */
    @Test
    public void test_profileDataIsCorrectlyDisplayed() {
        onView(withId(R.id.etName)).check(matches(withText("John Doe")));
        onView(withId(R.id.etEmail)).check(matches(withText("john.doe@example.com")));
        onView(withId(R.id.etPhone)).check(matches(withText("123-456-7890")));
    }

    /**
     * Tests the real-time update functionality.
     * It verifies that the UI automatically updates when the user's data changes in Firestore.
     */
    @Test
    public void test_realtimeUpdateChangesDisplayedData() throws InterruptedException {
        // 1. Verify the initial name is correct.
        onView(withId(R.id.etName)).check(matches(withText("John Doe")));

        // 2. Update the user's name in our local mock object.
        mockUser.setName("Jane Doe");

        // 3. Push the updated user object to Firestore. This will trigger the onSnapshot listener in ProfileActivity.
        UserDB.upsertUser(mockUser, (ok, e) -> {});

        // 4. Wait for the snapshot listener to fire and the UI to update.
        Thread.sleep(1000);

        // 5. Verify that the name displayed in the UI has changed.
        onView(withId(R.id.etName)).check(matches(withText("Jane Doe")));
    }

    /**
     * Tests if clicking the 'Edit' button correctly launches the EditProfileActivity
     * and passes the correct device ID.
     */
    @Test
    public void test_editButtonNavigatesToEditProfileActivity() {
        onView(withId(R.id.btnEdit)).perform(click());

        // Verify that an Intent was sent with two conditions:
        // 1. It targets the EditProfileActivity.
        // 2. It contains an extra with the key "deviceId" and the correct test device ID value.
        intended(hasComponent(EditProfileActivity.class.getName()));
        intended(hasExtra("deviceId", testDeviceId));
    }

    /**
     * Tests if clicking the 'Return Home' button navigates to the EntrantHomeActivity.
     */
    @Test
    public void test_returnButtonNavigatesToHome() {
        onView(withId(R.id.btnReturnHome)).perform(click());
        intended(hasComponent(EntrantHomeActivity.class.getName()));
    }

//    /**
//     * Tests that clicking the 'Delete' button removes the user's profile information
//     * from the view, simulating deletion.
//     * Tests user story 01.02.04
//     */
//    @Test
//    public void test_deleteButtonClearsProfileAndNavigatesAway() throws InterruptedException {
//        onView(withId(R.id.btnDelete)).perform(click());
//
//        // Wait for async deletion and for the activity to potentially finish.
//        Thread.sleep(2000);
//
//        // After deletion and the listener fires, your activity should close.
//        // We can check this by asserting that its main views are no longer displayed.
//        onView(withId(R.id.etName)).check(matches(not(isDisplayed())));
//        onView(withId(R.id.etEmail)).check(matches(not(isDisplayed())));
//        onView(withId(R.id.etPhone)).check(matches(not(isDisplayed())));
//    }
//      UNCOMMENT WHEN DELETE ACC BUTTON FUNCTIONALITY COMPLETE
}
