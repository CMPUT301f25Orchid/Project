
package com.example.fairdraw.Activities;

import android.os.IBinder;
import android.view.WindowManager;

import androidx.test.espresso.Root;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Helper to match Toast windows in Espresso checks.
 *
 * Usage:
 *   onView(withText("Some message"))
 *       .inRoot(new ToastMatcher())
 *       .check(matches(isDisplayed()));
 */
public class ToastMatcher extends TypeSafeMatcher<Root> {

    @Override
    public void describeTo(Description description) {
        description.appendText("is a Toast");
    }

    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            // Toasts are in their own window: windowToken == appToken
            return windowToken == appToken;
        }
        return false;
    }
}
