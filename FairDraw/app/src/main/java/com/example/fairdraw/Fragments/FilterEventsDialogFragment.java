package com.example.fairdraw;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FilterEventsDialogFragment extends DialogFragment {

    private FilterListener listener;

    public interface FilterListener {
        void onFiltersApplied(String status, String interest, String availability);
        void onFiltersCleared();
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.filter_events_dialog, null);

        Button applyBtn = view.findViewById(R.id.applyFiltersBtn);
        Button clearBtn = view.findViewById(R.id.clearFiltersBtn);

        applyBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFiltersApplied("Open", "Sports and Recreation", "Mondays"); // placeholder
            }
            dismiss();
        });

        clearBtn.setOnClickListener(v -> {
            if (listener != null) listener.onFiltersCleared();
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}
