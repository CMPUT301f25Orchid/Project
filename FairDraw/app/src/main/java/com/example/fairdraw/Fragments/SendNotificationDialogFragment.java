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

public class SendNotificationDialogFragment extends DialogFragment {

    public enum Audience { WAITING_LIST, SELECTED, CANCELLED }

    public interface Listener {
        void onSendNotification(String eventId, Audience audience, String message);
    }

    private static final String ARG_EVENT_ID = "arg_event_id";
    @Nullable private Listener listener;

    public static SendNotificationDialogFragment newInstance(String eventId) {
        SendNotificationDialogFragment f = new SendNotificationDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }

    public void setListener(@Nullable Listener l) { this.listener = l; }

    @Override public void onStart() {
        super.onStart();
        // Make dialog width nice & centered
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

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
