package com.example.fairdraw.Activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.Fragments.DecisionFragment;
import com.example.fairdraw.Models.Entrant;
import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.Adapters.EntrantNotificationAdapter;
import com.example.fairdraw.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Displays entrant notifications stored under entrants/{deviceId}.notifications.
 * <p>
 * The activity listens to the Entrant document for the current device id and binds
 * the list of notifications to a RecyclerView adapter.
 */
public final class EntrantNotificationsActivity extends BaseTopBottomActivity {
    private ListenerRegistration reg;
    private EntrantNotificationAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notifications_entrant); // contains @id/rvNotifications

        RecyclerView rv = findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EntrantNotificationAdapter(new EntrantNotificationAdapter.OnAction() {
            @Override public void onAcceptDecline(EntrantNotification n) {
                // TODO: navigate to event details
                new DecisionFragment().newInstance(n).show(getSupportFragmentManager(), "Decision");
            }
            @Override public void onItemClick(EntrantNotification n) {

            }
        });
        rv.setAdapter(adapter);
        initBottomNav(BarType.ENTRANT, findViewById(R.id.home_bottom_nav_bar));

        BottomNavigationView bottomNav = findViewById(R.id.home_bottom_nav_bar);
        bottomNav.setSelectedItemId(R.id.notifications_activity);

    }

    @Override
    protected void onStart() {
        super.onStart();
        String deviceId = DevicePrefsManager.getDeviceId(this);
        reg = EntrantDB.getEntrantCollection()
                .document(deviceId)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null || !snap.exists()) {
                        adapter.setItems(Collections.emptyList());
                        return;
                    }

                    Entrant entrant = snap.toObject(Entrant.class);

                    assert entrant != null;
                    adapter.setItems(entrant.getNotifications());
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (reg != null) { reg.remove(); reg = null; }
    }

    // --- helpers ---




    private static String asStr(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
