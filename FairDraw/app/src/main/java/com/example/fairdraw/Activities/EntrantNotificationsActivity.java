package com.example.fairdraw.Activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.Others.BarType;
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


    /** Entrant screen: listens to entrants/{deviceId}.notifications and displays them. */
    public final class EntrantNotificationsActivity extends BaseTopBottomActivity {
        private ListenerRegistration reg;
        private EntrantNotificationAdapter adapter;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.fragment_notifications_entrant);


            RecyclerView rv = findViewById(R.id.rvNotifications);
            rv.setLayoutManager(new LinearLayoutManager(this));

            adapter = new EntrantNotificationAdapter(new EntrantNotificationAdapter.OnAction() {
                @Override public void onAcceptDecline(EntrantNotification notification) {
                    // TODO: navigate to event details
                }
                @Override public void onItemClick(EntrantNotification notification) {

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
                        adapter.setItems(parseNotifications(snap.get("notifications")));
                    });


        }

        @Override
        protected void onStop() {
            super.onStop();
            if (reg != null) { reg.remove(); reg = null; }
        }

        // --- helpers ---

        @SuppressWarnings("unchecked")
        private static List<EntrantNotification> parseNotifications(Object raw) {
            List<EntrantNotification> out = new ArrayList<>();
            if (!(raw instanceof List)) return out;

            for (Object o : (List<?>) raw) {
                if (!(o instanceof Map)) continue;
                Map<String, Object> m = (Map<String, Object>) o;

                EntrantNotification notification = new EntrantNotification();
                notification.type    = asStr(m.get("type"));
                notification.eventId = asStr(m.get("eventId"));
                notification.title   = asStr(m.get("title"));

                Object r  = m.get("read");
                notification.read    = (r instanceof Boolean) && (Boolean) r;

                out.add(notification);
            }
            return out;
        }

        private static String asStr(Object o) {
            return o == null ? null : String.valueOf(o);
        }
    }