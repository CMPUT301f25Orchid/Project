package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EntrantEventDetails extends AppCompatActivity {

    // --- Views ---
    private TextView tvTitle;
    private TextView tvSummary;
    private ImageView heroImage;
    private GridLayout detailsGrid;
    private MaterialButton btnWaitlist;

    // ---- Date & money formatters ----
    private final SimpleDateFormat dayFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final NumberFormat moneyFmt = NumberFormat.getCurrencyInstance(Locale.getDefault());

    private ListenerRegistration eventListener;
    FirebaseImageStorageService storageService;


    private void bindCell(int includeId, int iconRes, String title, String subtitle) {
        View cell = findViewById(includeId);
        ((ImageView) cell.findViewById(R.id.icon)).setImageResource(iconRes);
        ((TextView) cell.findViewById(R.id.title)).setText(title);
        ((TextView) cell.findViewById(R.id.subtitle)).setText(subtitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_event_details);

        // grab views
        tvTitle     = findViewById(R.id.tvTitle);
        tvSummary   = findViewById(R.id.tvSummary);
        heroImage   = findViewById(R.id.heroImage);
        detailsGrid = findViewById(R.id.detailsGrid);
        btnWaitlist = findViewById(R.id.btnWaitlist);

        // 1) get the eventId from intent
        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2) fetch Event and bind, and setup listener for realtime updates
        eventListener = EventDB.listenToEvent(eventId, event -> {
            if (event == null) {
                Toast.makeText(this, "Unable to load event.", Toast.LENGTH_LONG).show();
                return;
            }
            bindEvent(event);
        });

        storageService = new FirebaseImageStorageService();

        // 3) button toggle (we’ll later connect to real waitlist status)
        final boolean[] onWaitlist = {false};
        btnWaitlist.setOnClickListener(v -> {
            onWaitlist[0] = !onWaitlist[0];
            btnWaitlist.setSelected(onWaitlist[0]);
            btnWaitlist.setText(onWaitlist[0] ? "Leave Waitlist" : "Join Lottery Waitlist");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    private static String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }

    private static int nz(@Nullable Integer i) {
        return i == null ? 0 : i;
    }
    
    private void bindEvent(Event e) {
        // title / summary
        setTextOrHide(tvTitle,  safe(e.getTitle(), "Event"));
        setTextOrHide(tvSummary, safe(e.getDescription(), ""));

        // cells (use the same include IDs you added)
        bindCell(R.id.cell_location,
                R.drawable.ic_location_on_24,
                "Location",
                safe(e.getLocation(), "—"));

        bindCell(R.id.cell_geolocation,
                R.drawable.ic_place_24,
                "Geolocation Requirement",
                true ? "Required" : "Not Required");

        bindCell(R.id.cell_capacity,
                R.drawable.ic_people_24,
                "Capacity & Waitlist",
                buildCapacityText(e));

        bindCell(R.id.cell_reg_period,
                R.drawable.ic_event_24,
                "Registration Period",
                buildDateRange(new Date(), e.getRegPeriod()));

        bindCell(R.id.cell_price,
                R.drawable.ic_attach_money_24,
                "Price",
                buildPrice(e));

        bindCell(R.id.cell_organizer,
                R.drawable.ic_person_24,
                "Organizer",
                safe(e.getOrganizer(), "—"));

        bindCell(R.id.cell_schedule,
                R.drawable.ic_schedule_24,
                "Schedule",
                safe(e.getTime().toString(), "—"));

        // Add a placeholder bitmap saying loading for event poster
        heroImage.setImageResource(R.drawable.loading);

        // Fetch the event poster
        storageService.getEventPosterDownloadUrl(e.getUuid()).addOnCompleteListener(urlTask -> {
            if (urlTask.isSuccessful()) {
                String url = urlTask.getResult().toString();
                Glide.with(this)
                        .load(url)
                        .into(heroImage);
            } else {
                // Handle error
                Toast.makeText(this, "Failed to load event poster", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildCapacityText(Event e) {
        // expected fields: totalSpots, waitlistCount, confirmedCount
        // fallbacks to 0 if null
        int total  = nz(e.getCapacity());
        int wait   = e.getWaitingList().size();
        int conf   = e.getEnrolledList().size();
        int avail  = Math.max(total - conf, 0);

        return avail + " spots • " + wait + " on waitlist • " + conf + " confirmed";
    }

    private String buildDateRange(Object start, Object end) {
        Date s = toDate(start);
        Date t = toDate(end);
        if (s == null && t == null) return "—";
        if (s == null) return "until " + dayFmt.format(t);
        if (t == null) return dayFmt.format(s);
        return dayFmt.format(s) + " to " + dayFmt.format(t);
    }

    /**
     * Accepts java.util.Date, com.google.firebase.Timestamp, or null.
     */
    @Nullable
    private static Date toDate(@Nullable Object obj) {
        if (obj == null) return null;
        if (obj instanceof Date) return (Date) obj;
        try {
            // Firestore Timestamp
            com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) obj;
            return ts.toDate();
        } catch (Throwable ignore) {
            return null;
        }
    }

    private String buildPrice(Event e) {
        // handle numeric or string
        if (e.getPrice() == null) return "—";
        try {
            if (e.getPrice() != null) {
                return moneyFmt.format(((Number) e.getPrice()).doubleValue());
            }
            return e.getPrice().toString();
        } catch (Exception ex) {
            return e.getPrice().toString();
        }
    }

    private void setTextOrHide(TextView textView, String string) {
        if (string == null || string.trim().isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(string);
        }
    }
}
