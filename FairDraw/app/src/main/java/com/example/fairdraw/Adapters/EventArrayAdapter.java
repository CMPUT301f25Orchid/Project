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
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {
    public EventArrayAdapter(Context context, ArrayList<Event> events){super(context, 0, events);}
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
