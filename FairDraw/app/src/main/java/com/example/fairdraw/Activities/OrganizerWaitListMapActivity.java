package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Models.AreaStats;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrganizerWaitListMapActivity extends BaseTopBottomActivity implements OnMapReadyCallback {

    private static final String TAG = "OrganizerWaitListMap";

    private GoogleMap mMap;
    private String eventId;
    private Event event;
    private ImageView ivHeroImage;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvStatusPill;
    private FirebaseImageStorageService storageService;

    // key -> AreaStats (bucketed region)
    private final Map<String, AreaStats> areaMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_wait_list_map);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initTopNav(com.example.fairdraw.Others.BarType.ORGANIZER);
        initBottomNav(com.example.fairdraw.Others.BarType.ORGANIZER, findViewById(R.id.home_bottom_nav_bar));

        // Get event id from intent
        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Back button
        Button btnBack = findViewById(R.id.btnBackToEvent);
        btnBack.setOnClickListener(v -> finish());

        // Initialize storage service and views
        storageService = new FirebaseImageStorageService();
        ivHeroImage = findViewById(R.id.heroImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatusPill = findViewById(R.id.tvStatusPill);

        // Subscribe to realtime event updates
        EventDB.getEventCollection().document(eventId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Event event1 = snapshot.toObject(Event.class);
                if (event1 != null) {
                    bindEvent(event1);
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
        });

        // Attach a SupportMapFragment into the FrameLayout
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);
    }

    /**
     * Binds the event data to the UI components.
     *
     * @param e The event object containing event details.
     */
    private void bindEvent(Event e) {
        event = e;

        // Show hero image
        storageService.getEventPosterDownloadUrl(eventId).addOnCompleteListener(urlTask -> {
            if (urlTask.isSuccessful()) {
                String downloadUrl = urlTask.getResult().toString();
                Glide.with(this).load(downloadUrl).into(ivHeroImage);
            }
        });

        // Show title
        tvTitle.setText((e.getTitle() == null || e.getTitle().isEmpty()) ? "Untitled Event" : e.getTitle());

        // Show description
        tvDescription.setText((e.getDescription() == null || e.getDescription().isEmpty()) ? "No description provided" : e.getDescription());

        // Update registration status pill based on open/close dates
        Date today = new Date();
        Date eventRegOpen = e.getEventOpenRegDate();
        Date eventRegClose = e.getEventCloseRegDate();

        if (eventRegOpen != null && eventRegClose != null) {
            if (today.before(eventRegOpen)) {
                tvStatusPill.setText("Registration Not Open");
            } else if (today.after(eventRegClose)) {
                tvStatusPill.setText("Registration Closed");
            } else {
                tvStatusPill.setText("Registration Open");
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Optional: enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Load event and build area markers
        loadEventAndBuildAreas();
    }

    private void loadEventAndBuildAreas() {
        EventDB.getEvent(eventId, event -> {
            if (event == null) {
                Toast.makeText(this, "Failed to load event", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Event.EntrantLocation> waitlistLocations = event.getWaitlistLocations();
            if (waitlistLocations == null || waitlistLocations.isEmpty()) {
                Toast.makeText(this, "No waitlist locations yet", Toast.LENGTH_SHORT).show();
                return;
            }

            areaMap.clear();

            // Aggregate into "buckets" by rounded lat/lng (~city-level)
            for (Map.Entry<String, Event.EntrantLocation> entry : waitlistLocations.entrySet()) {
                Event.EntrantLocation loc = entry.getValue();
                if (loc == null || loc.getLat() == null || loc.getLng() == null) {
                    continue;
                }

                double lat = loc.getLat();
                double lng = loc.getLng();

                double latBucket = round(lat, 2); // ~1–2km resolution
                double lngBucket = round(lng, 2);

                String key = latBucket + "," + lngBucket;

                AreaStats stats = areaMap.get(key);
                if (stats == null) {
                    stats = new AreaStats(latBucket, lngBucket);
                    areaMap.put(key, stats);
                }
                stats.increment();
            }

            if (areaMap.isEmpty()) {
                Toast.makeText(this, "No valid locations to display", Toast.LENGTH_SHORT).show();
                return;
            }

            showAreasOnMap();
        });
    }

    private double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    private void showAreasOnMap() {
        mMap.clear();

        LatLng firstCenter = null;

        for (AreaStats area : areaMap.values()) {
            LatLng position = new LatLng(area.getLat(), area.getLng());

            if (firstCenter == null) {
                firstCenter = position;
            }

            String title = String.format(Locale.getDefault(),
                    "Entrants: %d", area.getCount());
            String snippet = String.format(Locale.getDefault(),
                    "Approx. area\nLat: %.4f, Lng: %.4f",
                    area.getLat(), area.getLng());

            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)));
        }

        if (firstCenter != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstCenter, 10f));
        }
    }
}
