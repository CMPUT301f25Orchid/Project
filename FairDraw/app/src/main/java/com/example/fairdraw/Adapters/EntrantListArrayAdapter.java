package com.example.fairdraw.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairdraw.Others.ListItemEntrant;
import com.example.fairdraw.R;

import java.util.List;

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
    private Boolean hideCloseButton = true;

    /**
     * Create an adapter that shows entrants and hides the close button.
     *
     * @param context the Android context used to inflate views
     * @param entrantList list of entrants to display (must not be null)
     */
    public EntrantListArrayAdapter(Context context, List<ListItemEntrant> entrantList) {
        this.context = context;
        this.entrantList = entrantList;
    }

    /**
     * Create an adapter with explicit control over the close button visibility.
     *
     * @param context the Android context used to inflate views
     * @param entrantList list of entrants to display (must not be null)
     * @param hideCloseButton if true, the close button will be hidden
     */
    public EntrantListArrayAdapter(Context context, List<ListItemEntrant> entrantList, Boolean hideCloseButton) {
        this.context = context;
        this.entrantList = entrantList;
        this.hideCloseButton = hideCloseButton;
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

        // If hideCloseButton is true, hide the close button
        if (hideCloseButton) {
            ((ImageView) view.findViewById(R.id.ivClose)).setVisibility(View.GONE);
        }
        else {
            view.findViewById(R.id.ivClose).setOnClickListener(v -> {
                // TODO: remove entrant from list and put in cancelled list
            });
        }

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
            holder.entrantName.setText(listItemEntrant.getEntrantId());
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

        /**
         * Create a view holder for the provided item view.
         *
         * @param itemView inflated item view (expects a TextView with id {@code tvEntrantName})
         */
        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            entrantName = itemView.findViewById(R.id.tvEntrantName);
        }
    }
}
