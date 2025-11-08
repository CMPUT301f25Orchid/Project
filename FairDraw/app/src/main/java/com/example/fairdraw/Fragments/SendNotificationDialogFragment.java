package com.example.fairdraw.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.example.fairdraw.R;

/**
 * DialogFragment for composing and sending a notification for an event.
 *
 * <p>Hosts can show this dialog to compose a message and choose the target
 * audience (waiting list, selected, or cancelled). Results are delivered via
 * the {@link Listener} callback interface.</p>
 */
public class SendNotificationDialogFragment extends DialogFragment {

    /**
     * Audience groups supported by the dialog.
     */
    public enum Audience { WAITING_LIST, SELECTED, CANCELLED }

    /**
     * Callback used to deliver the composed notification back to the host.
     */
    public interface Listener {
        /**
         * Called when the user taps the Send button.
         *
         * @param eventId the event id provided to the dialog via {@link #newInstance}
         * @param audience the audience to receive the message
         * @param message the notification message text
         */
        void onSendNotification(String eventId, Audience audience, String message);
    }

    private static final String ARG_EVENT_ID = "arg_event_id";
    @Nullable private Listener listener;

    /**
     * Create a new SendNotificationDialogFragment for the given event id.
     *
     * @param eventId id of the event to notify about
     * @return a configured fragment
     */
    public static SendNotificationDialogFragment newInstance(String eventId) {
        SendNotificationDialogFragment f = new SendNotificationDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }

    /**
     * Set the Listener that will receive the send callback.
     *
     * @param l listener or null
     */
    public void setListener(@Nullable Listener l) { this.listener = l; }

    /**
     * Apply dialog window sizing adjustments when the dialog becomes visible.
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
     * Inflate the dialog UI and wire up button handlers.
     *
     * @param inflater layout inflater
     * @param container parent view group
     * @param savedInstanceState saved bundle
     * @return created view hierarchy
     */
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send_notification_modal, container, false);

        ImageButton btnClose = v.findViewById(R.id.btnClose);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        Button btnSend = v.findViewById(R.id.btnSend);
        TextInputEditText etMessage = v.findViewById(R.id.etMessage);

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

    /**
     * Apply styling for the dialog before creation.
     */
    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppDialog);
    }
}
