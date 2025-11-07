package com.example.fairdraw.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;

import java.util.ArrayList;

/**
 * An adapter for the event list view.
 *
 * <p>Provides a simple mapping from {@link com.example.fairdraw.Models.Event} objects
 * into the {@code R.layout.event_content} layout used by the events list UI.</p>
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {
    /**
     * Create a new EventArrayAdapter.
     *
     * @param context host context
     * @param events list of events to display
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events){super(context, 0, events);}

    /**
     * Return a view for the event at the given position.
     * Safely handles a null Event object by showing empty strings.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_content, parent, false);

        }
        else{
            view = convertView;
        }
        Event event = getItem(position);
        TextView eventTitle = view.findViewById(R.id.event_content_title);
        TextView eventId = view.findViewById(R.id.event_info);
        if (event != null) {
            eventTitle.setText(event.getTitle());
            eventId.setText(event.getDescription());
        } else {
            eventTitle.setText("");
            eventId.setText("");
        }
        return view;


    }
}
