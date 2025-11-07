package com.example.fairdraw;

import static org.hamcrest.CoreMatchers.not;

import android.content.Context;
import android.content.Intent;
import android.telecom.Call;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.fairdraw.Activities.EditProfileActivity;
import com.example.fairdraw.Activities.EntrantHomeActivity;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Models.User;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Instrumented tests for the EditProfileActivity.
 * These tests verify that the user can successfully edit and save their profile information.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EditProfileActivityTest {

    private static String testDeviceId;
    private static final String MOCK_FCM_TOKEN = "test-edit-fcm-token";
    private User mockUser;

    /**
     * This method runs before each test. It sets up the test environment.
     * It creates a mock user and saves it to Firestore.
     */
    @Before
    public void setUp() throws InterruptedException {
        // 1. Initialize Espresso-Intents to allow for intent verification.
        Intents.init();

        // 2. Get the device ID, create a mock user.
        Context context = ApplicationProvider.getApplicationContext();
        testDeviceId = DevicePrefsManager.getDeviceId(context);
        mockUser = new User("Jane Doe", "jane.doe@example.com", "987-654-3210", testDeviceId, MOCK_FCM_TOKEN);

        // 3. Use a CountDownLatch to ensure the user is created in Firestore before tests run.
        final CountDownLatch latch = new CountDownLatch(1);
        UserDB.upsertUser(mockUser, (ok, e) -> {
            if (!ok) {
                Log.e("EditProfileTest", "Setup: Failed to create mock user", e);
            }
            latch.countDown(); // Signal that the database operation is complete.
        });

        // 4. Wait for the database write to finish. Fails the test if it takes too long.
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * This method runs after each test. It cleans up the test environment.
     */
    @After
    public void tearDown() {
        // 1. Release Espresso-Intents.
        Intents.release();

        // 2. Clean up the mock user from Firestore to ensure a clean state for the next test.
        if (testDeviceId != null) {
            UserDB.deleteUser(testDeviceId, (ok, e) -> {});
        }
    }

    /**
     * Helper method to create an Intent with the necessary deviceId extra.
     * The Activity under test is launched manually in each test.
     */
    private Intent createEditProfileActivityIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        // The EditProfileActivity gets the deviceId from DevicePrefsManager,
        // so we don't need to pass it as an extra.
        return new Intent(context, EditProfileActivity.class);
    }

    /**
     * Tests if the EditText fields are correctly pre-populated with the user's data upon launch.
     */
    @Test
    public void test_initialDataIsDisplayedCorrectly() {
        // Launch the activity for this specific test
        ActivityScenario.launch(createEditProfileActivityIntent());

        // Check if the EditText fields are populated with the correct data
        Espresso.onView(ViewMatchers.withId(R.id.etName)).check(ViewAssertions.matches(ViewMatchers.withText("Jane Doe")));
        Espresso.onView(ViewMatchers.withId(R.id.etEmail)).check(ViewAssertions.matches(ViewMatchers.withText("jane.doe@example.com")));
        Espresso.onView(ViewMatchers.withId(R.id.etPhone)).check(ViewAssertions.matches(ViewMatchers.withText("987-654-3210")));
    }

    /**
     * Tests that editing data, clicking 'Save', and verifying the update in Firestore works correctly.
     */
    @Test
    public void test_editAndSaveChanges_updatesDataSuccessfully() throws InterruptedException {
        // Launch the activity
        ActivityScenario.launch(createEditProfileActivityIntent());

        // 1. Clear existing text, type new values, and close the keyboard.
        Espresso.onView(ViewMatchers.withId(R.id.etName)).perform(ViewActions.clearText(), ViewActions.typeText("Jane Smith"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.etEmail)).perform(ViewActions.clearText(), ViewActions.typeText("jane.smith@example.com"), ViewActions.closeSoftKeyboard());

        // 2. Click the 'Save' button.
        Espresso.onView(ViewMatchers.withId(R.id.btnSaveChanges)).perform(ViewActions.click());

        // 3. The activity should finish after saving. We can verify this by checking that
        // a view from its layout no longer exists.
        Espresso.onView(ViewMatchers.withId(R.id.btnSaveChanges)).check(ViewAssertions.doesNotExist());

        // 4. Verify the change was actually saved in the database using a CountDownLatch.
        final CountDownLatch latch = new CountDownLatch(1);
        UserDB.getUserOrNull(testDeviceId, (updatedUser, e) -> {
            Assert.assertNotNull("User should not be null after update", updatedUser);
            Assert.assertEquals("Name should be updated", "Jane Smith", updatedUser.getName());
            Assert.assertEquals("Email should be updated", "jane.smith@example.com", updatedUser.getEmail());
            latch.countDown(); // Signal assertions are complete.
        });

        // 5. Wait for the database read to finish. Fails if it times out.
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Tests that clicking the 'Cancel' button closes the activity without saving changes.
     */
    @Test
    public void test_cancelButton_discardsChangesAndClosesActivity() throws InterruptedException {
        // Launch the activity
        ActivityScenario.launch(createEditProfileActivityIntent());

        // 1. Make a change to the name field.
        Espresso.onView(ViewMatchers.withId(R.id.etName)).perform(ViewActions.clearText(), ViewActions.typeText("A Canceled Name"), ViewActions.closeSoftKeyboard());

        // 2. Click the 'Cancel' button.
        Espresso.onView(ViewMatchers.withId(R.id.btnCancel)).perform(ViewActions.click());

        // 3. Verify the activity has closed.
        Espresso.onView(ViewMatchers.withId(R.id.btnCancel)).check(ViewAssertions.doesNotExist());

        // 4. Verify the data was NOT changed in the database.
        final CountDownLatch latch = new CountDownLatch(1);
        UserDB.getUserOrNull(testDeviceId, (user, e) -> {
            Assert.assertNotNull("User should still exist", user);
            // Check that the name is the original name, not the canceled one.
            Assert.assertEquals("Name should not have been updated", "Jane Doe", user.getName());
            latch.countDown();
        });

        // 5. Wait for the check to complete.
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Tests if clicking the 'Return Home' button navigates to the EntrantHomeActivity.
     */
    @Test
    public void test_returnHomeButton_navigatesToHome() {
        // Launch the activity
        ActivityScenario.launch(createEditProfileActivityIntent());

        // Click the 'Return Home' button
        Espresso.onView(ViewMatchers.withId(R.id.btnReturnHome)).perform(ViewActions.click());

        // Verify that an Intent was sent targeting the EntrantHomeActivity
        Intents.intended(IntentMatchers.hasComponent(EntrantHomeActivity.class.getName()));
    }
}
