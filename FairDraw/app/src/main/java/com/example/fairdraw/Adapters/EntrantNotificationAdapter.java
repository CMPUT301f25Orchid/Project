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
 * RecyclerView adapter for displaying entrant notifications with different layouts based on notification type.
 * Supports different view types for WIN, LOSE, WAITLIST, and REPLACE notifications.
 */
public class EntrantNotificationAdapter extends RecyclerView.Adapter<EntrantNotificationAdapter.ViewHolder> {

    /**
     * Interface for handling user actions on notification items.
     */
    public interface OnAction {
        /**
         * Called when user accepts or declines a WIN notification.
         * @param notification The notification being acted upon
         */
        void onAcceptDecline(EntrantNotification notification);
        
        /**
         * Called when user clicks on any notification item.
         * @param notification The notification that was clicked
         */
        void onItemClick(EntrantNotification notification);
    }

    /** View type constant for WIN notifications */
    private static final int viewWon         = 1;
    /** View type constant for LOSE notifications */
    private static final int viewLost        = 2;
    /** View type constant for WAITLIST notifications */
    private static final int viewWaitJoined = 3;
    /** View type constant for REPLACE notifications */
    private static final int viewWaitLeft   = 4;

    /** The list of notifications to display */
    private final List<EntrantNotification> items = new ArrayList<>();
    /** Callback for handling user actions */
    private final OnAction actions;

    /**
     * Constructs a new EntrantNotificationAdapter.
     * 
     * @param actions The callback interface for handling user actions
     */
    public EntrantNotificationAdapter(OnAction actions) {
        this.actions = actions;
    }

    /**
     * Updates the list of notifications and refreshes the display.
     * 
     * @param list The new list of notifications
     */
    public void setItems(List<EntrantNotification> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * Gets the number of notifications in the list.
     * 
     * @return The total number of notifications
     */
    @Override public int getItemCount() { return items.size(); }

    /**
     * Determines the view type for a notification based on its type.
     * 
     * @param pos The position of the notification
     * @return The view type constant for the notification
     */
    @Override public int getItemViewType(int pos) {
        String raw = items.get(pos).type;
        String t = (raw == null) ? "" : raw.trim().toUpperCase(Locale.US);
        switch (t) {
            case "WIN":      return viewWon;
            case "LOSE":     return viewLost;
            case "WAITLIST": return viewWaitJoined;
            case "REPLACE":  return viewWaitLeft;
            default:         return viewWon;
        }
    }

    /**
     * Creates a new ViewHolder for a notification item based on the view type.
     * Different layouts are inflated for different notification types.
     * 
     * @param parent The parent ViewGroup
     * @param vt The view type
     * @return A new ViewHolder for the notification
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
            case viewLost:
            default:
                v = inf.inflate(R.layout.item_notification_lost, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    /**
     * Binds data to the ViewHolder for the notification at the specified position.
     * Sets up the notification message and click listeners.
     * 
     * @param h The ViewHolder to bind data to
     * @param position The position of the notification in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        EntrantNotification notification = items.get(position);

        String event = (notification.title == null || notification.title.isEmpty()) ? "this event" : "“" + notification.title + "”";
        String msg;
        if (resolveType(notification.type) != NotificationType.OTHER) {
            msg = resolveType(notification.type).title(event);
        } else {
            msg = notification.message;
        }

        if (h.msg != null) h.msg.setText(msg);
        if (h.cta != null) h.cta.setOnClickListener(v -> { if (actions != null) actions.onAcceptDecline(notification); });
        h.itemView.setOnClickListener(v -> { if (actions != null) actions.onItemClick(notification); });
    }

    /**
     * Resolves a string to a NotificationType enum value.
     * 
     * @param s The string representation of the notification type
     * @return The corresponding NotificationType, or NotificationType.WIN if parsing fails
     */
    private static NotificationType resolveType(String s) {
        try { return NotificationType.valueOf(s); }
        catch (Exception ignore) { return NotificationType.WIN; }
    }

    /**
     * ViewHolder for notification items.
     * Holds references to views that may vary based on notification type.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the notification message */
        final TextView msg;
        /** TextView for the call-to-action button (only for WIN notifications) */
        final TextView cta;

        /**
         * Constructs a new ViewHolder.
         * 
         * @param v The item view
         */
        ViewHolder(@NonNull View v) {
            super(v);
            msg = pickOne(v, R.id.tvMessageWon, R.id.tvMessageLost, R.id.tvMessageWaitJoined, R.id.tvMessageWaitLeft);
            cta = v.findViewById(R.id.btnAcceptDecline);
        }

        /**
         * Searches for the first TextView with a matching ID from the provided list.
         * 
         * @param v The view to search in
         * @param ids The list of resource IDs to search for
         * @return The first TextView found, or null if none match
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