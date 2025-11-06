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

public class EntrantListArrayAdapter extends RecyclerView.Adapter<EntrantListArrayAdapter.VH> {

    public interface OnCloseClickListener {
        void onCloseClick(String entrantId, int position);
    }
    private OnCloseClickListener closeClickListener;
    public void setOnCloseClickListener(OnCloseClickListener l) { this.closeClickListener = l; }

    private final List<ListItemEntrant> entrantList;
    private Boolean hideCloseButton = true;
    private final LayoutInflater inflater;

    public EntrantListArrayAdapter(Context context, List<ListItemEntrant> entrantList, Boolean hideCloseButton) {
        this.entrantList = entrantList;
        this.hideCloseButton = hideCloseButton;
        this.inflater = LayoutInflater.from(context);
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView name;
        ImageView close;
        VH(View v) {
            super(v);
            name  = v.findViewById(R.id.tvEntrantName);
            close = v.findViewById(R.id.ivCancelEntrant); // your X button
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_entrant_small, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        String entrantId = entrantList.get(pos).getEntrantId();
        h.name.setText(entrantId);

        if (hideCloseButton) {
            h.close.setVisibility(View.GONE);
        } else {
            h.close.setVisibility(View.VISIBLE);
            h.close.setOnClickListener(v -> {
                if (closeClickListener != null) closeClickListener.onCloseClick(entrantId, h.getBindingAdapterPosition());
            });
        }
    }

    @Override
    public int getItemCount() {
        return entrantList.size();
    }
}
