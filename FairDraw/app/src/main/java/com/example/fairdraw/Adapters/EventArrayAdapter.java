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
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {

    String startDate;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public interface EditEventListener {
        void onEditEvent(int position);
    }

    public interface EventCardClickedListener {
        void onEventCardClicked(int position);
    }

    private EditEventListener editEventListener;
    private EventCardClickedListener eventCardClickedListener;

    public EventArrayAdapter(Context context, ArrayList<Event> events, EditEventListener editEventListener, EventCardClickedListener eventCardClickedListener) {
        super(context, 0, events);
        this.editEventListener = editEventListener;
        this.eventCardClickedListener = eventCardClickedListener;
    }

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
