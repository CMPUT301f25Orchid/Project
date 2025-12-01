package com.example.fairdraw.Activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import android.os.SystemClock;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fairdraw.R;
import com.example.fairdraw.ToastMatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;


/**
 * Covers Entrant Section A (Sign-up basics):
 *
 *  - US 01.02.01: entrant can provide name, email, and phone in the app.
 *  - Prepares for US 01.07.01: device-based identity is used when signing up
 *    (SignUpActivity calls DevicePrefsManager internally).
 *
 * These tests focus on UI validation and navigation; Firestore persistence
 * is handled by UserDB and is covered separately in unit/integration tests.
 */
@RunWith(AndroidJUnit4.class)
public class SignUpActivityTest {

    private View decorView;

    @Rule
    public ActivityScenarioRule<SignUpActivity> activityRule =
            new ActivityScenarioRule<>(SignUpActivity.class);

    @Before
    public void setUp() {
        // Enable Espresso-Intents so we can verify navigation to ProfileActivity
        Intents.init();

        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<SignUpActivity>() {
            @Override
            public void perform(SignUpActivity activity) {
                decorView = activity.getWindow().getDecorView();
            }
        });
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Happy path:
     *  - Fill in all fields with valid data.
     *  - Tap "Sign Up".
     *  - Expect navigation to ProfileActivity.
     *
     * Maps to: US 01.02.01.
     */
    @Test
    public void validData_navigatesToProfileActivity() {
        // Arrange
        onView(withId(R.id.fullName))
                .perform(replaceText("Test Entrant"), closeSoftKeyboard());

        onView(withId(R.id.etEmail))
                .perform(replaceText("entrant@example.com"), closeSoftKeyboard());

        onView(withId(R.id.etPhone))
                .perform(replaceText("1234567890"), closeSoftKeyboard());

        // Act
        onView(withId(R.id.btnSignUp)).perform(click());

        // Sleep wait for navigation to complete
        SystemClock.sleep(3000);

        // Assert: ProfileActivity is started
        intended(hasComponent(ProfileActivity.class.getName()));
    }

    /**
     * Validation: missing fields.
     *  - Leave at least one required field empty.
     *  - Tap "Sign Up".
     *  - Expect a Toast: "Please fill all fields."
     *
     * Maps to: US 01.02.01 (negative/validation scenario).
     */
    @Test
    public void missingField_showsFillAllFieldsToast() {
        // Arrange: only fill email & phone, leave name empty
        onView(withId(R.id.etEmail))
                .perform(replaceText("entrant@example.com"), closeSoftKeyboard());

        onView(withId(R.id.etPhone))
                .perform(replaceText("1234567890"), closeSoftKeyboard());

        // Act
        onView(withId(R.id.btnSignUp)).perform(click());

        // Assert snackbar is shown
        onView(withText("Please fill all fields."))
                .check(matches(isDisplayed()));
    }

    /**
     * Validation: invalid email.
     *  - Fill name and phone.
     *  - Use a non-email string for the email field.
     *  - Tap "Sign Up".
     *  - Expect a Toast: "Please enter a valid email address."
     *
     * Maps to: US 01.02.01 (input quality / format).
     */
    @Test
    public void invalidEmail_showsEmailValidationToast() {
        // Arrange
        onView(withId(R.id.fullName))
                .perform(replaceText("Test Entrant"), closeSoftKeyboard());

        onView(withId(R.id.etEmail))
                .perform(replaceText("not-an-email"), closeSoftKeyboard());

        onView(withId(R.id.etPhone))
                .perform(replaceText("1234567890"), closeSoftKeyboard());

        // Act
        onView(withId(R.id.btnSignUp)).perform(click());

        // Assert snackbar is shown
        onView(withText("Please enter a valid email address."))
                .check(matches(isDisplayed()));
    }
}
