package com.example.fairdraw.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Others.EntrantEventStatus;
import com.example.fairdraw.Others.ListItemEntrant;
import com.example.fairdraw.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecyclerView adapter that displays a list of entrants in a compact item view.
 *
 * <p>Each item shows the entrant's name (or ID) and an optional close button. The
 * adapter accepts a list of {@link ListItemEntrant} objects and inflates
 * {@code R.layout.item_entrant_small} for each entry.</p>
 *
 * Usage notes:
 * - If {@code hideCloseButton} is true, the close (remove) button will be hidden.
 * - The adapter does not modify the provided list; callers should update the list
 *   and call {@link #notifyDataSetChanged()} or the appropriate notify method when
 *   the data changes.</p>
 */
public class EntrantListArrayAdapter extends RecyclerView.Adapter<EntrantListArrayAdapter.EntrantViewHolder> {

    private final Context context;
    private final List<ListItemEntrant> entrantList;
    private final Boolean hideCloseButton; // initialize in constructor
    private final String eventId; // initialize in constructor

    // Simple in-memory cache mapping deviceId -> display name to avoid repeated DB calls
    private final Map<String, String> nameCache = new HashMap<>();

    /**
     * Create an adapter with explicit control over the close button visibility.
     *
     * @param context the Android context used to inflate views
     * @param entrantList list of entrants to display (must not be null)
     * @param hideCloseButton if true, the close button will be hidden
     */
    public EntrantListArrayAdapter(Context context, List<ListItemEntrant> entrantList, Boolean hideCloseButton, String eventId) {
        this.context = context;
        this.entrantList = entrantList;
        this.hideCloseButton = hideCloseButton != null ? hideCloseButton : true;
        this.eventId = eventId != null ? eventId : "";
    }

    /**
     * Inflates the item view and sets up the optional close button behavior.
     *
     * @param parent the parent view group
     * @param viewType the view type for the new view
     * @return a new {@link EntrantViewHolder}
     */
    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_entrant_small, parent, false);

        // Don't set click behavior here because we need the adapter position which is
        // only reliably available in onBindViewHolder. We'll control visibility there too.
        return new EntrantViewHolder(view);
    }

    /**
     * Binds entrant data to the view holder at the given position.
     *
     * @param holder the view holder to bind
     * @param position position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        ListItemEntrant listItemEntrant = entrantList.get(position);
        if (listItemEntrant != null) {
            String deviceId = listItemEntrant.getEntrantId();

            // If we have the name cached, use it; otherwise show a placeholder (deviceId) and fetch.
            if (deviceId != null && nameCache.containsKey(deviceId)) {
                holder.entrantName.setText(nameCache.get(deviceId));
            } else {
                // Show device id as temporary text to avoid empty UI
                holder.entrantName.setText(deviceId != null ? deviceId : context.getString(R.string.unavailable));

                // Avoid refetching if we've previously recorded absence by caching deviceId->deviceId
                if (deviceId != null && !nameCache.containsKey(deviceId)) {
                    final String did = deviceId;
                    UserDB.getUserOrNull(did, (user, e) -> {
                        if (e == null && user != null && user.getName() != null && !user.getName().isEmpty()) {
                            nameCache.put(did, user.getName());
                        } else {
                            // Fall back to device id as display name so we don't keep refetching
                            nameCache.put(did, did);
                        }

                        // Find all positions where this device id appears and notify to rebind
                        for (int i = 0; i < entrantList.size(); i++) {
                            ListItemEntrant it = entrantList.get(i);
                            if (it != null && did.equals(it.getEntrantId())) {
                                notifyItemChanged(i);
                            }
                        }
                    });
                }
            }
        }

        // Configure close button visibility and behavior here so we have the current position
        if (hideCloseButton) {
            holder.ivClose.setVisibility(View.GONE);
            holder.ivClose.setOnClickListener(null);
        } else {
            holder.ivClose.setVisibility(View.VISIBLE);
            holder.ivClose.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                // Capture entrant id and remove from adapter list immediately for responsive UI
                ListItemEntrant removedItem = entrantList.remove(pos);
                notifyItemRemoved(pos);

                if (removedItem == null) return;
                String entrantId = removedItem.getEntrantId();

                // If we don't have an eventId, just return after removing locally.
                if (eventId.isEmpty()) {
                    return;
                }

                // Fetch the event, remove the entrant from any list they may be in and add to cancelledList.
                EventDB.getEvent(eventId, event -> {
                    if (event == null) {
                        // Could not fetch event; nothing to persist. Optionally inform user.
                        // We avoid restoring the item to keep behavior simple.
                        return;
                    }

                    // Defensive null checks for lists
                    List<String> waiting = event.getWaitingList();
                    List<String> invited = event.getInvitedList();
                    List<String> enrolled = event.getEnrolledList();
                    List<String> cancelled = event.getCancelledList();

                    if (waiting == null) waiting = new ArrayList<>();
                    if (invited == null) invited = new ArrayList<>();
                    if (enrolled == null) enrolled = new ArrayList<>();
                    if (cancelled == null) cancelled = new ArrayList<>();

                    // Remove from any list the entrant might currently be in
                    waiting.remove(entrantId);
                    invited.remove(entrantId);
                    enrolled.remove(entrantId);

                    // Add to cancelled list if not already present
                    if (!cancelled.contains(entrantId)) {
                        cancelled.add(entrantId);
                    }

                    // Persist changes back to the event object
                    event.setWaitingList(waiting);
                    event.setInvitedList(invited);
                    event.setEnrolledList(enrolled);
                    event.setCancelledList(cancelled);

                    EventDB.updateEvent(event, success -> {
                        if (!success) {
                            // Inform user of failure to persist; local removal already happened.
                            // Use a Snackbar to notify; this is low-risk and non-blocking.
                            View root = ((Activity)context).findViewById(android.R.id.content);
                            Snackbar.make(root, R.string.error_updating_event, Snackbar.LENGTH_SHORT).show();
                        }
                        else {
                            EntrantDB.addEventToHistory(entrantId, eventId, EntrantEventStatus.CANCELLED, (historySuccess, e) -> {
                                if (!historySuccess) {
                                    // Inform user of failure to update entrant history
                                    View root = ((Activity)context).findViewById(android.R.id.content);
                                    Snackbar.make(root, "Failed to update entrant's history", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                });
            });
        }
    }

    /**
     * Returns the number of entrants represented by this adapter.
     *
     * @return the size of the entrant list
     */
    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    /**
     * ViewHolder for an entrant item. Holds references to the view widgets so they
     * can be quickly bound during {@link #onBindViewHolder}.
     */
    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView entrantName;
        ImageView ivClose;

        /**
         * Create a view holder for the provided item view.
         *
         * @param itemView inflated item view (expects a TextView with id {@code tvEntrantName})
         */
        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            entrantName = itemView.findViewById(R.id.tvEntrantName);
            ivClose = itemView.findViewById(R.id.ivClose);
        }
    }
}
