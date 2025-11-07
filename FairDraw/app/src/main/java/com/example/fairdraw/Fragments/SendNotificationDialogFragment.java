package com.example.fairdraw;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Objects;

/**
 * DialogFragment for sending notifications to entrants in specific event categories.
 * This fragment allows organizers to compose and send custom notifications to
 * waiting list, selected, or cancelled entrants for a specific event.
 */
public class SendNotificationDialogFragment extends DialogFragment {

    /**
     * Enum representing the possible target audiences for notifications.
     */
    public enum Audience { 
        /** Entrants currently on the waiting list */
        WAITING_LIST, 
        /** Entrants who have been selected (invited) */
        SELECTED, 
        /** Entrants who cancelled or were cancelled */
        CANCELLED 
    }

    /**
     * Interface for handling notification send events.
     */
    public interface Listener {
        /**
         * Called when a notification is ready to be sent.
         * 
         * @param eventId The ID of the event
         * @param audience The target audience for the notification
         * @param message The notification message content
         */
        void onSendNotification(String eventId, Audience audience, String message);
    }

    /** Bundle argument key for event ID */
    private static final String ARG_EVENT_ID = "arg_event_id";
    /** Listener for notification send events */
    @Nullable private Listener listener;

    /**
     * Creates a new instance of SendNotificationDialogFragment for a specific event.
     * 
     * @param eventId The ID of the event to send notifications for
     * @return A new instance with arguments set
     */
    public static SendNotificationDialogFragment newInstance(String eventId) {
        SendNotificationDialogFragment f = new SendNotificationDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }

    /**
     * Sets the listener for notification send events.
     * 
     * @param l The Listener to receive notification send events
     */
    public void setListener(@Nullable Listener l) { this.listener = l; }

    /**
     * Called when the dialog is starting. Sets the dialog width to match parent.
     */
    @Override public void onStart() {
        super.onStart();
        // Make dialog width nice & centered
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * Creates and returns the view for the notification dialog.
     * Sets up UI controls for audience selection, message input, and send/cancel actions.
     * 
     * @param inflater The LayoutInflater to inflate views
     * @param container The parent view
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The created view
     */
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send_notification_modal, container, false);

        ImageButton btnClose = v.findViewById(R.id.btnClose);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        Button btnSend = v.findViewById(R.id.btnSend);
        TextInputEditText etMessage = v.findViewById(R.id.etMessage);

        RadioGroup rg = v.findViewById(R.id.rgAudience);
        RadioButton rbWaiting = v.findViewById(R.id.rbWaiting);
        RadioButton rbSelected = v.findViewById(R.id.rbSelected);
        RadioButton rbCancelled = v.findViewById(R.id.rbCancelled);

        View.OnClickListener dismiss = vv -> dismiss();
        btnClose.setOnClickListener(dismiss);
        btnCancel.setOnClickListener(dismiss);

        btnSend.setOnClickListener(vv -> {
            String msg = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
            if (TextUtils.isEmpty(msg)) {
                Toast.makeText(getContext(), "Message cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            Audience audience = Audience.WAITING_LIST;
            if (rbSelected.isChecked()) audience = Audience.SELECTED;
            else if (rbCancelled.isChecked()) audience = Audience.CANCELLED;

            String eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;
            if (listener != null && eventId != null) {
                listener.onSendNotification(eventId, audience, msg);
            }
            dismiss();
        });

        setCancelable(true);
        return v;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppDialog);
    }
}
