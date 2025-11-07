package com.example.fairdraw.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;

/**
 * DialogFragment for displaying an accept/decline decision dialog for event invitations.
 * This fragment allows entrants to accept or decline lottery win invitations.
 */
public class DecisionFragment extends DialogFragment {
    /** The notification associated with this decision dialog */
    private EntrantNotification notification;
    
    /**
     * Creates a new instance of DecisionFragment with the specified notification.
     * 
     * @param notification The EntrantNotification containing event and user information
     * @return A new instance of DecisionFragment with arguments set
     */
    public DecisionFragment newInstance(EntrantNotification notification){
        DecisionFragment fragment = new DecisionFragment();
        Bundle args = new Bundle();
        String eventName = notification.title;
        String eventId = notification.eventId;
        String userId = DevicePrefsManager.getDeviceId(getContext());
        args.putString("eventName", eventName);
        args.putString("eventId", eventId);
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }
        
        /**
         * Creates the dialog with accept and decline buttons for the event invitation.
         * 
         * @param savedInstanceState Bundle containing the fragment's previously saved state
         * @return The Dialog to be displayed
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.entrant_decision_fragment, null);

            TextView eventNameText = view.findViewById(R.id.dialog_event);
            String eventName = getArguments().getString("eventName");
            eventNameText.setText(eventName.toUpperCase());
            View btnAccept = view.findViewById(R.id.accept_button);
            View btnDecline = view.findViewById(R.id.decline_button);

            btnDecline.setOnClickListener(v -> {
                EventDB.getEvent(notification.eventId, new EventDB.GetEventCallback() {
                    @Override
                    public void onCallback(Event event) {
                        if (event == null) {
                            Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        event.cancelLotteryWinner(notification.eventId);
                        EventDB.updateEvent(event, new EventDB.UpdateEventCallback() {
                            @Override
                            public void onCallback(boolean success) {
                                if (success) {
                                    Toast.makeText(getContext(), "You have been removed from the invited list", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            });

            btnAccept.setOnClickListener(v -> {
                EventDB.getEvent(notification.eventId, new EventDB.GetEventCallback() {
                    @Override
                    public void onCallback(Event event) {
                        if (event == null) {
                            Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        event.acceptLotteryWinner(notification.eventId);
                        EventDB.updateEvent(event, new EventDB.UpdateEventCallback() {
                            @Override
                            public void onCallback(boolean success) {
                                if (success) {
                                    Toast.makeText(getContext(), "You have been added to the enrolled list", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(view);

            Dialog dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            dialog.setCanceledOnTouchOutside(true);
            return dialog;
    }
}
