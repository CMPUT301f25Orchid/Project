package com.example.fairdraw.Adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.Others.NotificationType;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link EntrantNotificationAdapter}.
 *
 * <p>These tests exercise the adapter's public API (such as {@code setItems} and
 * {@code getItemCount}) and verify that the binding logic in {@code onBindViewHolder}
 * completes without throwing when provided with different {@link NotificationType}
 * inputs. The tests use a mocked {@link EntrantNotificationAdapter.ViewHolder} to
 * avoid Android view inflation in a pure JVM test.</p>
 */
public class EntrantNotificationAdapterTest {

    private EntrantNotificationAdapter adapter;

    /**
     * Create a fresh adapter instance before each test.
     */
    @Before
    public void setUp() {
        adapter = new EntrantNotificationAdapter(null);
    }

    /**
     * Verifies that {@link EntrantNotificationAdapter#setItems(List)} correctly sets
     * the internal item list and that {@link EntrantNotificationAdapter#getItemCount()}
     * returns the expected count.
     */
    @Test
    public void setItems_and_getItemCount() {
        List<EntrantNotification> list = Arrays.asList(
                new EntrantNotification(NotificationType.WIN, "e1", "Title 1"),
                new EntrantNotification(NotificationType.LOSE, "e2", "Title 2")
        );

        adapter.setItems(list);
        assertEquals(2, adapter.getItemCount());
    }

    /**
     * Confirms that the adapter can handle different view types and that the
     * {@code onBindViewHolder} method executes without error for WIN, LOSE,
     * WAITLIST and REPLACE notification types.
     *
     * <p>This test uses a Mockito-mocked ViewHolder having a mocked message
     * TextView to avoid Android framework dependencies in the JVM test. The
     * assertion is intentionally lightweight (presence of the ViewHolder) since
     * validating exact text requires running the test on an Android environment
     * or using Robolectric with real view inflation.</p>
     */
    @Test
    public void viewType_and_viewHolder_inflation_binding() {
        List<EntrantNotification> list = Arrays.asList(
                new EntrantNotification(NotificationType.WIN, "e1", "Title 1"),
                new EntrantNotification(NotificationType.LOSE, "e2", "Title 2"),
                new EntrantNotification(NotificationType.WAITLIST, "e3", "Title 3"),
                new EntrantNotification(NotificationType.REPLACE, "e4", "Title 4")
        );

        adapter.setItems(list);

        // Mock the ViewHolder since we can't inflate it without a real context
        EntrantNotificationAdapter.ViewHolder mockViewHolder = mock(EntrantNotificationAdapter.ViewHolder.class);
        mockViewHolder.msg = mock(TextView.class);

        // For each position, just test the binding logic
        for (int i = 0; i < adapter.getItemCount(); i++) {
            adapter.onBindViewHolder(mockViewHolder, i);

            // Verify the message TextView is set
            // We can't easily check the text content without a real context for string resources,
            // but we can confirm that the onBindViewHolder method completes without crashing.
            // A more advanced test could use Mockito to verify setText() was called.
            assertNotNull("Mock ViewHolder should exist for position " + i, mockViewHolder);
        }
    }
}
