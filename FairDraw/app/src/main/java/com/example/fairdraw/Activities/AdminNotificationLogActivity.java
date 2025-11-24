package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairdraw.Others.AdminNotificationLog;
import com.example.fairdraw.Others.NotificationType;
import com.example.fairdraw.R;

import java.util.ArrayList;
import java.util.List;

public class AdminNotificationLogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private final List<AdminNotificationLog> dummyLogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_log);

        recyclerView = findViewById(R.id.recyclerNotificationLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new LogAdapter(dummyLogs);
        recyclerView.setAdapter(adapter);

        loadDummyData();
    }

    /**
     * Create several dummy logs so you can see multiple rows
     */
    private void loadDummyData() {
        dummyLogs.clear();

        // 1 Anne Hathaway sent to all entrants
        AdminNotificationLog log1 = new AdminNotificationLog(
                "event1",
                "Anne Hathaway",
                "Swimming Lessons For Beginners",
                "device001",
                NotificationType.OTHER
        );
        dummyLogs.add(log1);

        // 2 Brian Johnson sent to all cancelled entrants
        AdminNotificationLog log2 = new AdminNotificationLog(
                "event2",
                "Brian Johnson",
                "Advanced Rock Climbing",
                "device002",
                NotificationType.OTHER
        );
        dummyLogs.add(log2);

        // 3 Emily Witherspoon sent to selected entrants
        AdminNotificationLog log3 = new AdminNotificationLog(
                "event3",
                "Emily Witherspoon",
                "Evening Yoga Session",
                "device003",
                NotificationType.OTHER
        );
        dummyLogs.add(log3);

        adapter.notifyDataSetChanged();
    }

    // simple RecyclerView adapter using the item_admin_notification_log row
    private static class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

        private final List<AdminNotificationLog> logs;

        LogAdapter(List<AdminNotificationLog> logs) {
            this.logs = logs;
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_notification_log, parent, false);
            return new LogViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            AdminNotificationLog log = logs.get(position);
            holder.bind(log, position);
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        static class LogViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvSenderLine;
            private final TextView tvAudienceLine;
            private final TextView tvDateLine;

            LogViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSenderLine = itemView.findViewById(R.id.tvSenderLine);
                tvAudienceLine = itemView.findViewById(R.id.tvAudienceLine);
                tvDateLine = itemView.findViewById(R.id.tvDateLine);
            }

            /**
             * Bind one row
             * position is used to give each dummy row a slightly different subtitle and date
             */
            void bind(AdminNotificationLog log, int position) {
                // first line: "<name> sent a notification"
                String userName = log.getUserName();
                if (userName == null || userName.isEmpty()) {
                    userName = "Unknown organizer";
                }
                tvSenderLine.setText(userName + " sent a notification");

                // second line: audience text based on which dummy row
                String audience;
                if (position == 0) {
                    audience = "Sent to all entrants";
                } else if (position == 1) {
                    audience = "Sent to all cancelled entrants";
                } else {
                    audience = "Sent to all selected entrants";
                }
                tvAudienceLine.setText(audience);

                // third line: fixed example dates for now
                String dateText;
                if (position == 0) {
                    dateText = "Sent on 19/12/2025";
                } else if (position == 1) {
                    dateText = "Sent on 21/11/2025";
                } else {
                    dateText = "Sent on 15/08/2025";
                }
                tvDateLine.setText(dateText);
            }
        }
    }
}
