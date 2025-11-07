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

public class EntrantNotificationAdapter extends RecyclerView.Adapter<EntrantNotificationAdapter.ViewHolder> {

    public interface OnAction {
        void onAcceptDecline(EntrantNotification notification);  // only for WIN row
        void onItemClick(EntrantNotification notification);       // optional
    }

    private static final int viewWon         = 1;
    private static final int viewLost        = 2;
    private static final int viewWaitJoined = 3;
    private static final int viewWaitLeft   = 4;

    private final List<EntrantNotification> items = new ArrayList<>();
    private final OnAction actions;

    public EntrantNotificationAdapter(OnAction actions) {
        this.actions = actions;
    }

    public void setItems(List<EntrantNotification> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @Override public int getItemCount() { return items.size(); }

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
                v = inf.inflate(R.layout.item_notification_won, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

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

    private static NotificationType resolveType(String s) {
        try { return NotificationType.valueOf(s); }
        catch (Exception ignore) { return NotificationType.WIN; }
    }

    /** */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView msg; // tvMessageWon / tvMessageLost / tvMessageWaitJoined / tvMessageWaitLeft
        final TextView cta; // btnAcceptDecline (WIN only), else null

        ViewHolder(@NonNull View v) {
            super(v);
            msg = pickOne(v, R.id.tvMessageWon, R.id.tvMessageLost, R.id.tvMessageWaitJoined, R.id.tvMessageWaitLeft);
            cta = v.findViewById(R.id.btnAcceptDecline);
        }

        private static TextView pickOne(View v, int... ids) {
            for (int id : ids) {
                TextView t = v.findViewById(id);
                if (t != null) return t;
            }
            return null;
        }
    }
}