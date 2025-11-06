package com.example.fairdraw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EntrantListArrayAdapter extends RecyclerView.Adapter<EntrantListArrayAdapter.EntrantViewHolder> {

    private final Context context;
    private final List<ListItemEntrant> entrantList;
    private Boolean hideCloseButton = true;

    public EntrantListArrayAdapter(Context context, List<ListItemEntrant> entrantList) {
        this.context = context;
        this.entrantList = entrantList;
    }

    public EntrantListArrayAdapter(Context context, List<ListItemEntrant> entrantList, Boolean hideCloseButton) {
        this.context = context;
        this.entrantList = entrantList;
        this.hideCloseButton = hideCloseButton;
    }

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

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        ListItemEntrant listItemEntrant = entrantList.get(position);
        if (listItemEntrant != null) {
            holder.entrantName.setText(listItemEntrant.getEntrantId());
        }
    }

    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView entrantName;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            entrantName = itemView.findViewById(R.id.tvEntrantName);
        }
    }
}
