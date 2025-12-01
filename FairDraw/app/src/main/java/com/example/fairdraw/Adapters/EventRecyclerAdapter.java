package com.example.fairdraw.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecyclerView ListAdapter for Event items. Uses DiffUtil for smooth updates.
 */
public class EventRecyclerAdapter extends ListAdapter<Event, EventRecyclerAdapter.ViewHolder> {

    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    public interface EditEventListener { void onEditEvent(int position); }
    public interface EventCardClickedListener { void onEventCardClicked(int position); }

    private final EditEventListener editEventListener;
    private final EventCardClickedListener eventCardClickedListener;

    FirebaseImageStorageService imageStorageService = new FirebaseImageStorageService();

    public EventRecyclerAdapter(EditEventListener editEventListener, EventCardClickedListener eventCardClickedListener) {
        super(DIFF_CALLBACK);
        this.editEventListener = editEventListener;
        this.eventCardClickedListener = eventCardClickedListener;
    }

    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            String o = oldItem.getUuid();
            String n = newItem.getUuid();
            return o != null && o.equals(n);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            // Compare a few meaningful fields for equality
            if (oldItem.getUuid() != null && oldItem.getUuid().equals(newItem.getUuid())) {
                Date os = oldItem.getStartDate();
                Date ns = newItem.getStartDate();
                boolean dateEq = (os == null && ns == null) || (os != null && os.equals(ns));
                boolean titleEq = (oldItem.getTitle() == null ? "" : oldItem.getTitle()).equals(newItem.getTitle() == null ? "" : newItem.getTitle());
                boolean locEq = (oldItem.getLocation() == null ? "" : oldItem.getLocation()).equals(newItem.getLocation() == null ? "" : newItem.getLocation());

                float oldPrice = oldItem.getPrice() == null ? 0f : oldItem.getPrice();
                float newPrice = newItem.getPrice() == null ? 0f : newItem.getPrice();
                boolean priceEq = Float.compare(oldPrice, newPrice) == 0;

                int oldCap = oldItem.getCapacity() == null ? 0 : oldItem.getCapacity();
                int newCap = newItem.getCapacity() == null ? 0 : newItem.getCapacity();
                boolean capEq = oldCap == newCap;

                return dateEq && titleEq && locEq && priceEq && capEq;
            }
            return false;
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_content, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = getItem(position);
        if (event == null) return;

        holder.eventTitle.setText(event.getTitle() == null ? "" : event.getTitle());
        holder.eventLocation.setText(event.getLocation() == null ? "" : event.getLocation());
        Date sd = event.getStartDate();
        holder.eventDate.setText(sd == null ? "" : dateFormat.format(sd));
        holder.eventCapacity.setText(event.getCapacity() == null ? "0" : event.getCapacity().toString());
        holder.eventPrice.setText(String.format(Locale.US, "$%.2f", event.getPrice() == null ? 0f : event.getPrice()));

        holder.editButton.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && editEventListener != null) {
                editEventListener.onEditEvent(pos);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && eventCardClickedListener != null) {
                eventCardClickedListener.onEventCardClicked(pos);
            }
        });

        // poster image handling left to caller; event_content has an ImageView with id eventImage
        imageStorageService.getEventPosterDownloadUrl(event.getUuid()).addOnSuccessListener(uri -> {
            Glide.with(holder.itemView.getContext())
                    .load(uri)
                    .placeholder(R.drawable.swimming)
                    .into(holder.eventImage);
        }).addOnFailureListener(e -> {
            // Handle any errors here, e.g., set a default image
            Log.e("EventRecyclerAdapter", "Failed to load event image for event " + event.getUuid(), e);
        });
    }

    /**
     * Public accessor for external callers to fetch an item at position.
     * This wraps the protected ListAdapter.getItem(...) method.
     */
    public Event getEventAt(int position) {
        return getItem(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventLocation, eventDate, eventCapacity, eventPrice;
        View editButton;
        ImageView eventImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.event_content_title);
            eventLocation = itemView.findViewById(R.id.event_content_location);
            eventDate = itemView.findViewById(R.id.event_content_date);
            eventCapacity = itemView.findViewById(R.id.event_content_capacity);
            eventPrice = itemView.findViewById(R.id.event_content_price);
            editButton = itemView.findViewById(R.id.event_edit_button);
            eventImage = itemView.findViewById(R.id.eventImage);
        }
    }
}
