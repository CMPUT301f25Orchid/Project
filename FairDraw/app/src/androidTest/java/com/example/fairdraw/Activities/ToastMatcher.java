package com.example.fairdraw;

import android.os.IBinder;
import android.view.WindowManager;

import androidx.test.espresso.Root;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * A Toast matcher that works on newer Android versions.
 *
 * It treats a root as a Toast window if:
 *  - The window type is TYPE_TOAST or TYPE_APPLICATION, and
 *  - The window is not contained by another window (windowToken == appToken).
 */
public class ToastMatcher extends TypeSafeMatcher<Root> {

    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    @Override
    protected boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;

        if (type == WindowManager.LayoutParams.TYPE_TOAST
                || type == WindowManager.LayoutParams.TYPE_APPLICATION) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();

            // For Toasts, windowToken == appToken.
            // For normal activity windows, these usually differ.
            if (windowToken == appToken) {
                return true;
            }
        }
        return false;
    }
}
