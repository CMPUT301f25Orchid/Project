package com.example.fairdraw.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.example.fairdraw.Activities.EditEventPage;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.OrganizerEventsDataHolder;
import com.example.fairdraw.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * An adapter for the event list view.
 *
 * <p>This ArrayAdapter adapts {@link Event} objects into the {@code R.layout.event_content}
 * view for display in a ListView or similar AdapterView. It also exposes two listener
 * interfaces so the host activity/fragment can respond to edit button clicks and
 * card clicks separately.</p>
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {

    /** Formatted start date string for the current bound item. */
    String startDate;

    /** Date formatter used to present the event start date. */
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Listener invoked when the edit button for an event is pressed.
     */
    public interface EditEventListener {
        /**
         * Called to request editing of the event at the given adapter position.
         *
         * @param position adapter position of the event to edit
         */
        void onEditEvent(int position);
    }

    /**
     * Listener invoked when an event card (row) is clicked.
     */
    public interface EventCardClickedListener {
        /**
         * Called when the event card at {@code position} is clicked.
         *
         * @param position adapter position of the clicked event card
         */
        void onEventCardClicked(int position);
    }

    private EditEventListener editEventListener;
    private EventCardClickedListener eventCardClickedListener;

    /**
     * Construct a new EventArrayAdapter.
     *
     * @param context the current context
     * @param events list of events to display
     * @param editEventListener callback for edit button presses (may be null)
     * @param eventCardClickedListener callback for card clicks (may be null)
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events, EditEventListener editEventListener, EventCardClickedListener eventCardClickedListener) {
        super(context, 0, events);
        this.editEventListener = editEventListener;
        this.eventCardClickedListener = eventCardClickedListener;
    }

    /**
     * Bind an {@link Event} at the provided position into the list item view.
     *
     * <p>This method inflates {@code R.layout.event_content} when necessary and
     * populates its fields (title, location, date, capacity, price). It also
     * wires the edit button and the card click to the adapter's listeners if they
     * are provided.</p>
     *
     * @param position adapter position to bind
     * @param convertView recycled view (may be null)
     * @param parent parent view group
     * @return the bound view for display
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_content, parent, false);

        } else {
            view = convertView;
        }
        Event event = getItem(position);

        // Setting cardview variables
        TextView eventTitle = view.findViewById(R.id.event_content_title);
        TextView eventLocation = view.findViewById(R.id.event_content_location);
        TextView eventDate = view.findViewById(R.id.event_content_date);
        TextView eventCapacity = view.findViewById(R.id.event_content_capacity);
        TextView eventPrice = view.findViewById(R.id.event_content_price);

        startDate = dateFormat.format(event.getStartDate());

        // Setting cardview values
        // TODO Add poster image
        eventTitle.setText(event.getTitle());
        eventLocation.setText(event.getLocation());
        eventDate.setText(startDate);
        eventCapacity.setText(event.getCapacity().toString());
        eventPrice.setText(String.format("$%.2f", event.getPrice()));

        // Set edit button listener
        view.findViewById(R.id.event_edit_button).setOnClickListener(v -> {
            // Call the edit event listener
            if (editEventListener != null) {
                editEventListener.onEditEvent(position);
            }
        });

        // Set cardview click listener
        view.setOnClickListener(v -> {
            // Call the event card clicked listener
            if (eventCardClickedListener != null) {
                eventCardClickedListener.onEventCardClicked(position);
            }
        });

        return view;


    }
}
