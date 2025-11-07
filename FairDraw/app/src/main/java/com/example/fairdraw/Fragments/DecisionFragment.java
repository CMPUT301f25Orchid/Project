package com.example.fairdraw.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
 * DialogFragment that displays a decision dialog for an entrant notification.
 *
 * <p>This dialog lets a notified entrant accept or decline a lottery win. It
 * reads the event information from the fragment arguments and updates the
 * event document via {@link EventDB} when the user accepts or declines.</p>
 */
public class DecisionFragment extends DialogFragment {
    private EntrantNotification notification;

    /**
     * Create a new DecisionFragment pre-populated with the given notification.
     *
     * <p>Callers should pass the returned fragment to a FragmentManager to show
     * it. The method stores event name, id and the local user id in the
     * fragment arguments.</p>
     *
     * @param notification the entrant notification that triggered this dialog
     * @return a configured DecisionFragment instance
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
        // Keep the original notification object available on the fragment instance
        fragment.notification = notification;
        return fragment;
    }

    /**
     * Create the dialog shown to the user.
     *
     * <p>This method inflates the {@code R.layout.entrant_decision_fragment}
     * layout, wires up the accept/decline buttons and updates the Event via
     * {@link EventDB} based on the user's choice.</p>
     *
     * @param savedInstanceState saved state bundle, if any
     * @return the created Dialog instance
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.entrant_decision_fragment, null);

        TextView eventNameText = view.findViewById(R.id.dialog_event);
        String eventName = (getArguments() != null) ? getArguments().getString("eventName") : null;
        eventNameText.setText(eventName != null ? eventName.toUpperCase() : "");
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
