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
 * RecyclerView adapter for displaying a list of entrants.
 * This adapter supports an optional close button for removing entrants from the list.
 */
public class EntrantListArrayAdapter extends RecyclerView.Adapter<EntrantListArrayAdapter.EntrantViewHolder> {

    /** The application context */
    private final Context context;
    /** The list of entrants to display */
    private final List<ListItemEntrant> entrantList;
    /** Whether to hide the close button for each item */
    private Boolean hideCloseButton = true;

    /**
     * Constructs a new EntrantListArrayAdapter with the close button hidden.
     * 
     * @param context The application context
     * @param entrantList The list of entrants to display
     */
    public EntrantListArrayAdapter(Context context, List<ListItemEntrant> entrantList) {
        this.context = context;
        this.entrantList = entrantList;
    }

    /**
     * Constructs a new EntrantListArrayAdapter with configurable close button visibility.
     * 
     * @param context The application context
     * @param entrantList The list of entrants to display
     * @param hideCloseButton True to hide the close button, false to show it
     */
    public EntrantListArrayAdapter(Context context, List<ListItemEntrant> entrantList, Boolean hideCloseButton) {
        this.context = context;
        this.entrantList = entrantList;
        this.hideCloseButton = hideCloseButton;
    }

    /**
     * Creates a new ViewHolder for an entrant item.
     * 
     * @param parent The parent ViewGroup
     * @param viewType The view type of the new View
     * @return A new EntrantViewHolder
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
     * Binds data to the ViewHolder for the entrant at the specified position.
     * 
     * @param holder The ViewHolder to bind data to
     * @param position The position of the entrant in the list
     */
    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        ListItemEntrant listItemEntrant = entrantList.get(position);
        if (listItemEntrant != null) {
            holder.entrantName.setText(listItemEntrant.getEntrantId());
        }
    }

    /**
     * Gets the number of entrants in the list.
     * 
     * @return The total number of entrants
     */
    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    /**
     * ViewHolder for entrant list items.
     * Holds references to views in the item layout.
     */
    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the entrant's name */
        TextView entrantName;

        /**
         * Constructs a new EntrantViewHolder.
         * 
         * @param itemView The item view
         */
        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            entrantName = itemView.findViewById(R.id.tvEntrantName);
        }
    }
}
