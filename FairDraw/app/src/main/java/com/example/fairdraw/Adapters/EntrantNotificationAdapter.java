package com.example.fairdraw.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.Others.NotificationType;
import com.example.fairdraw.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Adapter for displaying entrant notifications in a RecyclerView.
 *
 * <p>Supports multiple notification view types (WIN, LOSE, WAITLIST, REPLACE) and
 * maps them to different item layouts. The adapter exposes an {@link OnAction}
 * callback interface for handling user actions such as accepting/declining a win
 * and item clicks.</p>
 */
public class EntrantNotificationAdapter extends RecyclerView.Adapter<EntrantNotificationAdapter.ViewHolder> {

    /**
     * Callback interface used by the host to respond to user actions in the list.
     */
    public interface OnAction {
        /**
         * Called when the user accepts or declines a win notification.
         * Implementations should handle the accept/decline flow for the provided
         * {@link EntrantNotification}. This callback is only used for WIN-type rows.
         *
         * @param notification the notification that was acted upon
         */
        void onAcceptDecline(EntrantNotification notification);  // only for WIN row

        /**
         * Called when the list item itself is clicked. Implementations may open a
         * details view or perform any contextual action.
         *
         * @param notification the notification that was clicked
         */
        void onItemClick(EntrantNotification notification);       // optional
    }

    private static final int viewWon         = 1;
    private static final int viewLost        = 2;
    private static final int viewWaitJoined = 3;
    private static final int viewWaitLeft   = 4;
    private static final int viewOther      = 5;

    private final List<EntrantNotification> items = new ArrayList<>();
    private final OnAction actions;

    /**
     * Create the adapter.
     *
     * @param actions callback implementation to receive events from list items
     */
    public EntrantNotificationAdapter(OnAction actions) {
        this.actions = actions;
    }

    /**
     * Replace the adapter contents with a new list of notifications.
     *
     * @param list the list of notifications to display (may be null)
     */
    public void setItems(List<EntrantNotification> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * Return the total number of notification items currently held by the adapter.
     *
     * @return number of items
     */
    @Override public int getItemCount() { return items.size(); }

    /**
     * Determine the view type for the notification at the supplied position.
     * The returned integer is used by {@link #onCreateViewHolder} to select the
     * correct layout.
     *
     * @param pos position of the item
     * @return an int representing the view type (one of viewWon/viewLost/...)
     */
    @Override public int getItemViewType(int pos) {
        String raw = items.get(pos).type;
        String t = (raw == null) ? "" : raw.trim().toUpperCase(Locale.US);
        switch (t) {
            case "WIN":      return viewWon;
            case "LOSE":     return viewLost;
            case "WAITLIST": return viewWaitJoined;
            case "REPLACE":  return viewWaitLeft;
            case "OTHER":    return viewOther;
            default:         return viewOther;
        }
    }

    /**
     * Inflate the appropriate layout for the notification view type.
     *
     * @param parent the parent view group
     * @param vt the view type as returned by {@link #getItemViewType}
     * @return a new {@link ViewHolder} holding references to the inflated views
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        View v;
        switch (vt) {
            case viewWon:
                v = inf.inflate(R.layout.item_notification_won, parent, false);
                break;
            case viewWaitLeft:
                v = inf.inflate(R.layout.item_notification_wait_left, parent, false);
                break;
            case viewWaitJoined:
                v = inf.inflate(R.layout.items_notification_wait_joined, parent, false);
                break;
            case viewOther:
                v = inf.inflate(R.layout.item_notification_other, parent, false);
                break;
            case viewLost:
            default:
                v = inf.inflate(R.layout.item_notification_lost, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    /**
     * Bind data from an {@link EntrantNotification} into the holder's views.
     * Handles resolving built-in notification types to a generated message and
     * wires up action buttons.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        EntrantNotification notification = items.get(position);

        String event = (notification.title == null || notification.title.isEmpty()) ? "this event" : "“" + notification.title + "”";
        NotificationType nt = resolveType(notification.type);
        String msg;
        if (nt != NotificationType.OTHER) {
            msg = nt.title(event);
        } else {
            // show organizer-provided message if available; otherwise fall back to a generic title
            msg = (notification.message == null || notification.message.isEmpty()) ? nt.title(event) : notification.message;
        }

        if (h.msg != null) h.msg.setText(msg);
        if (h.cta != null) h.cta.setOnClickListener(v -> { if (actions != null) actions.onAcceptDecline(notification); });
        h.itemView.setOnClickListener(v -> { if (actions != null) actions.onItemClick(notification); });
    }

    /**
     * Resolve a raw type string into a {@link NotificationType} enum. If the
     * provided value is not a known enum constant, defaults to {@code WIN}.
     *
     * @param s raw string value from the notification
     * @return resolved NotificationType (never null)
     */
    private static NotificationType resolveType(String s) {
        try { return NotificationType.valueOf(s); }
        catch (Exception ignore) { return NotificationType.OTHER; }
    }

    /**
     * ViewHolder that lazily locates the message and CTA text views used by
     * different notification layouts.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView msg; // tvMessageWon / tvMessageLost / tvMessageWaitJoined / tvMessageWaitLeft
        final TextView cta; // btnAcceptDecline (WIN only), else null

        /**
         * Create a holder for the provided view. The constructor locates the
         * primary message TextView and the optional CTA button (if present in
         * the inflated layout).
         *
         * @param v inflated item view
         */
        public ViewHolder(@NonNull View v) {
            super(v);
            msg = pickOne(v, R.id.tvMessageWon, R.id.tvMessageLost, R.id.tvMessageWaitJoined, R.id.tvMessageWaitLeft, R.id.tvMessageOther);
            cta = v.findViewById(R.id.btnAcceptDecline);
        }

        /**
         * Helper that returns the first TextView found from the provided list of
         * ids, or null if none exist in the view. This supports different
         * notification layouts that use different ids for the message field.
         *
         * @param v view to search
         * @param ids candidate resource ids
         * @return the first matching TextView or null
         */
        private static TextView pickOne(View v, int... ids) {
            for (int id : ids) {
                TextView t = v.findViewById(id);
                if (t != null) return t;
            }
            return null;
        }
    }
}

