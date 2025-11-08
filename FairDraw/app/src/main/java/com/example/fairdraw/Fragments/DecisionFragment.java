package com.example.fairdraw.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
 * DialogFragment presented to entrant users when they receive an invitation
 * (notification) to accept or decline an event invitation.
 *
 * <p>The fragment shows the event name and two actions: accept or decline.
 * Accepting updates the corresponding Event model to move the user to the
 * enrolled list; declining removes the user from the invited/winner list.
 * Database updates are performed via {@link EventDB}.</p>
 */
public class DecisionFragment extends DialogFragment {
    private static EntrantNotification notification;

    /**
     * Create a new DecisionFragment for the supplied notification.
     *
     * @param notification notification containing eventId and title
     * @return a configured DecisionFragment
     */
    public DecisionFragment newInstance(EntrantNotification notification){
        DecisionFragment.notification = notification;
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
     * Create the dialog that presents accept/decline options to the user.
     *
     * <p>The dialog reads the event name from the fragment arguments and
     * performs database updates via {@link EventDB} when the user accepts or
     * declines. Toasts and logs are used to provide feedback.</p>
     *
     * @param savedInstanceState saved state bundle (may be null)
     * @return a configured {@link Dialog}
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.entrant_decision_fragment, null);

        TextView eventNameText = view.findViewById(R.id.dialog_event);
        assert getArguments() != null;
        String eventName = getArguments().getString("eventName");
        assert eventName != null;
        eventNameText.setText(eventName.toUpperCase());
        View btnAccept = view.findViewById(R.id.accept_button);
        View btnDecline = view.findViewById(R.id.decline_button);

        btnDecline.setOnClickListener(v -> {
            Toast.makeText(getContext(), "You have declined the invitation", Toast.LENGTH_SHORT).show();
            Log.d("DecisionFragment", "User declined the invitation for event: " + eventName);
            EventDB.getEvent(DecisionFragment.notification.eventId, new EventDB.GetEventCallback() {
                @Override
                public void onCallback(Event event) {
                    if (event == null) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userId = DevicePrefsManager.getDeviceId(getContext());
                    event.cancelLotteryWinner(userId);
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
            Log.d("DecisionFragment", "Accept button clicked for eventId: " + notification.eventId);
            EventDB.getEvent(notification.eventId, new EventDB.GetEventCallback() {
                @Override
                public void onCallback(Event event) {
                    if (event == null) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userId = DevicePrefsManager.getDeviceId(getContext());
                    event.acceptLotteryWinner(userId);
                    EventDB.updateEvent(event, new EventDB.UpdateEventCallback() {
                        @Override
                        public void onCallback(boolean success) {
                            if (success) {
                                Toast.makeText(getContext(), "You have been added to the enrolled list", Toast.LENGTH_SHORT).show();
                                Log.d("DecisionFragment", "Successfully updated event for accepting invitation");
                            }
                            else {
                                Toast.makeText(getContext(), "Failed to accept invitation", Toast.LENGTH_SHORT).show();
                                Log.e("DecisionFragment", "Failed to update event for accepting invitation");
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
