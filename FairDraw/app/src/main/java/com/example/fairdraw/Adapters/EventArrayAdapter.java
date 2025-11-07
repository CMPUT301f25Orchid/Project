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
 * ArrayAdapter for displaying a list of events in a ListView.
 * This adapter inflates event_content layout for each event item and displays
 * the event's title and description.
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {
    /**
     * Constructs a new EventArrayAdapter.
     * 
     * @param context The application context
     * @param events The list of events to display
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events){super(context, 0, events);}
    
    /**
     * Creates and returns a view for an event item at the specified position.
     * 
     * @param position The position of the event in the list
     * @param convertView The recycled view to reuse, if available
     * @param parent The parent ViewGroup
     * @return The view for the event item
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
        eventTitle.setText(event.getTitle());
        eventId.setText(event.getDescription());
        return view;


    }
}
