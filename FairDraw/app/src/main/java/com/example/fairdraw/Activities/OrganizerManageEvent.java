package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Adapters.EntrantListArrayAdapter;
import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.Others.NotificationType;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.example.fairdraw.Others.ListItemEntrant;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;
import com.example.fairdraw.Fragments.SendNotificationDialogFragment;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Intent;
import android.net.Uri;

/**
 * OrganizerManageEvent class allows organizers to manage event details,
 * view and manage entrants, and send notifications.
 */
public class OrganizerManageEvent extends BaseTopBottomActivity {

    String eventId;
    Event event;
    ImageView ivHeroImage;
    TextView tvTitle;
    TextView tvRegStatus;
    TextView tvDescription;
    Button btnDrawAndInvite;
    RecyclerView rvInvited;
    RecyclerView rvCancelled;
    RecyclerView rvRegistered;
    RecyclerView rvWaiting;
    Button btnSendNotification;
    Button btnDownloadFinalEntrants;
    Button btnSeeWaitingMap;
    FirebaseImageStorageService storageService;

    // Fields used for ACTION_CREATE_DOCUMENT flow using Activity Result API
    private ActivityResultLauncher<Intent> createFileLauncher;
    private byte[] pendingCsvBytes = null;
    private String pendingCsvFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_manage_event);
        eventId = getIntent().getStringExtra("eventId");

        // Register the ActivityResultLauncher to handle the CREATE_DOCUMENT result
        createFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            int resultCode = result.getResultCode();
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri == null) {
                    Toast.makeText(this, "No location selected to save the CSV.", Toast.LENGTH_LONG).show();
                    pendingCsvBytes = null;
                    pendingCsvFileName = null;
                    return;
                }

                if (pendingCsvBytes == null) {
                    Toast.makeText(this, "No CSV content to save.", Toast.LENGTH_LONG).show();
                    pendingCsvFileName = null;
                    return;
                }

                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os == null) throw new IOException("OutputStream was null");
                    os.write(pendingCsvBytes);
                    os.flush();
                } catch (IOException ioe) {
                    Log.e("OrganizerManageEvent", "Failed to write CSV to selected Uri", ioe);
                    Toast.makeText(this, "Failed to save CSV: " + ioe.getMessage(), Toast.LENGTH_LONG).show();
                    pendingCsvBytes = null;
                    pendingCsvFileName = null;
                    return;
                }

                // clear pending data and notify
                String savedFileName = pendingCsvFileName;
                pendingCsvBytes = null;
                pendingCsvFileName = null;
                Toast.makeText(this, "Saved CSV to chosen location: " + (savedFileName != null ? savedFileName : ""), Toast.LENGTH_LONG).show();

                // Offer to share the newly created document
                try {
                    Uri shareUri = data.getData();
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/csv");
                    share.putExtra(Intent.EXTRA_STREAM, shareUri);
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(share, "Share enrolled entrants"));
                } catch (Exception ex) {
                    Log.w("OrganizerManageEvent", "Share chooser failed", ex);
                }

            } else {
                // User cancelled or no data
                Toast.makeText(this, "Save cancelled.", Toast.LENGTH_SHORT).show();
                pendingCsvBytes = null;
                pendingCsvFileName = null;
            }
        });

        // Setup the top and bottom edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.top_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize shared top and bottom navigation
        initTopNav(com.example.fairdraw.Others.BarType.ORGANIZER);
        initBottomNav(com.example.fairdraw.Others.BarType.ORGANIZER, findViewById(R.id.home_bottom_nav_bar));
        // Select the organizer home tab (manager pages belong to organizer section)
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.home_bottom_nav_bar);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.home_activity);

        // If no event id is provided, finish the activity
        if (eventId == null || eventId.isEmpty()) {
            Log.d("OrganizerManageEvent", "No event id provided");
            finish();
            return;
        }

        storageService = new FirebaseImageStorageService();

        // Initialize views
        ivHeroImage = findViewById(R.id.heroImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvRegStatus = findViewById(R.id.tvStatusPill);
        tvDescription = findViewById(R.id.tvDescription);
        btnDrawAndInvite = findViewById(R.id.btnDrawAndInvite);
        rvInvited = findViewById(R.id.rvInvited);
        rvCancelled = findViewById(R.id.rvCancelled);
        rvRegistered = findViewById(R.id.rvRegistered);
        rvWaiting = findViewById(R.id.rvWaiting);
        btnSendNotification = findViewById(R.id.btnSendNotification);
        btnDownloadFinalEntrants = findViewById(R.id.btnReturn);
        btnSeeWaitingMap = findViewById(R.id.btnSeeWaitingMap);

        EventDB.getEventCollection().document(eventId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("OrganizerManageEvent", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Event event1 = snapshot.toObject(Event.class);
                assert event1 != null;
                bindEvent(event1);
            } else {
                Log.d("OrganizerManageEvent", "Current data: null");
            }
        });
    }

    /**
     * Binds the event data to the UI components.
     *
     * @param e The event object containing event details.
     */
    public void bindEvent(Event e) {
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

        // Show registration status
        tvRegStatus.setText(e.getState().toString());

        // Show description
        tvDescription.setText((e.getDescription() == null || e.getDescription().isEmpty()) ? "No description provided" : e.getDescription());

        // Draw and invite button logic later
        btnDrawAndInvite.setOnClickListener(v -> {
            Toast.makeText(this, "Draw and Invite clicked for event: " + eventId, Toast.LENGTH_SHORT).show();
            EventDB.getEvent(eventId, (event) -> {
                if (event != null) {
                    // Perform draw and invite logic here
                    Toast.makeText(this, "Drawing and inviting entrants...", Toast.LENGTH_SHORT).show();
                    List<String> newInvitedEntrants = event.drawLotteryWinners();

                    // Update the event in the database
                    EventDB.updateEvent(event, success -> {
                        if (success) {
                            Toast.makeText(OrganizerManageEvent.this, "Entrants drawn and updated!", Toast.LENGTH_SHORT).show();
                            // now send the win notifications to the invited entrants
                            for (String entrantId : newInvitedEntrants) {
                                EntrantDB.pushNotificationToUser(entrantId, new EntrantNotification(NotificationType.WIN, eventId, event.getTitle()), (ok, e1) -> {
                                    if (ok) {
                                        Log.d("OrganizerManageEvent", "Notification sent to entrant ID: " + entrantId);
                                    } else {
                                        Log.d("OrganizerManageEvent", "Failed to send notification to entrant ID: " + entrantId + " | " + (e1 != null ? e1.getMessage() : "Unknown error"));
                                    }
                                });
                            }

                            // Also notify those who remained on the waiting list that they did not win
                            List<String> remainingWaitlist = event.getWaitingList();
                            if (remainingWaitlist != null && !remainingWaitlist.isEmpty()) {
                                for (String loserId : remainingWaitlist) {
                                    EntrantDB.pushNotificationToUser(loserId, new EntrantNotification(NotificationType.LOSE, eventId, event.getTitle()), (ok, e2) -> {
                                        if (ok) {
                                            Log.d("OrganizerManageEvent", "Lose notification sent to entrant ID: " + loserId);
                                        } else {
                                            Log.d("OrganizerManageEvent", "Failed to send lose notification to entrant ID: " + loserId + " | " + (e2 != null ? e2.getMessage() : "Unknown error"));
                                        }
                                    });
                                }
                            }
                        } else {
                            Toast.makeText(OrganizerManageEvent.this, "Failed to update event after drawing.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Failed to retrieve event for drawing: ", Toast.LENGTH_LONG).show();
                }
            });
        });

        // Show invited list
        buildEntrantItemRecyclerView(rvInvited, e.getInvitedList(), false);

        // Cancelled list
        buildEntrantItemRecyclerView(rvCancelled, e.getCancelledList(), true);

        // Registered list
        buildEntrantItemRecyclerView(rvRegistered, e.getEnrolledList(), true);

        // Waiting list
        buildEntrantItemRecyclerView(rvWaiting, e.getWaitingList(), true);

        // Link action buttons later
        btnSendNotification.setOnClickListener(v -> {
            // Open the Dialog
            SendNotificationDialogFragment dialog = SendNotificationDialogFragment.newInstance(eventId);
            dialog.setListener((eventId1, audience, message) -> {
                Toast.makeText(this,
                        "Send to " + audience + " | " + message, Toast.LENGTH_SHORT).show();

                List<String> targetEntrants;
                switch (audience) {
                    case SELECTED:
                        targetEntrants = e.getInvitedList();
                        break;
                    case CANCELLED:
                        targetEntrants = e.getCancelledList();
                        break;
                    case WAITING_LIST:
                    default:
                        targetEntrants = e.getWaitingList();
                        break;
                }
                EntrantNotification notification = new EntrantNotification(NotificationType.OTHER, eventId1, "No title");
                notification.message = message;
                for (String entrantId : targetEntrants) {
                    EntrantDB.pushNotificationToUser(entrantId, notification, (ok, ex) -> {
                        if (ok) {
                            Log.d("OrganizerManageEvent", "Custom notification sent to entrant ID: " + entrantId);
                        } else {
                            Log.d("OrganizerManageEvent", "Failed to send custom notification to entrant ID: " + entrantId + " | " + (ex != null ? ex.getMessage() : "Unknown error"));
                        }
                    });
                }
            });

            dialog.show(getSupportFragmentManager(), "SendNotificationDialog");
        });
        
        btnDownloadFinalEntrants.setOnClickListener(v -> {
            List<String> enrolled = e.getEnrolledList();
            Intent intent = new Intent(OrganizerManageEvent.this, OrganizerExportActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        btnSeeWaitingMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerWaitListMapActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
    }

    private void buildEntrantItemRecyclerView(RecyclerView recyclerView, List<String> stringList, Boolean hideCloseButton) {
        List<ListItemEntrant> entrantList = new ArrayList<>();
        for (String s : stringList) {
            entrantList.add(new ListItemEntrant(s));
        }
        EntrantListArrayAdapter adapter = new EntrantListArrayAdapter(this, entrantList, hideCloseButton, event.getUuid());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Export the enrolled entrant IDs to a CSV file. For each entrant id we try to fetch
     * a User record to include name/email/phone if available. The resulting file is saved
     * into the app external files directory and then shared using a FileProvider URI.
     */
    private void exportEnrolledEntrantsToCsv(List<String> enrolledIds, String eventTitle) {
        if (enrolledIds == null || enrolledIds.isEmpty()) {
            Toast.makeText(this, "No enrolled entrants to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Header
        final List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"DeviceId", "Name", "Email", "Phone"});

        // We'll fetch User objects for each entrant asynchronously and collect rows.
        AtomicInteger remaining = new AtomicInteger(enrolledIds.size());

        for (String deviceId : enrolledIds) {
            UserDB.getUserOrNull(deviceId, (user, ex) -> {
                if (user != null) {
                    rows.add(new String[]{deviceId,
                            user.getName() == null ? "" : user.getName(),
                            user.getEmail() == null ? "" : user.getEmail(),
                            user.getPhoneNum() == null ? "" : user.getPhoneNum()});
                } else {
                    // Either user not found or error; still include id and leave other columns blank
                    rows.add(new String[]{deviceId, "", "", ""});
                    if (ex != null) Log.w("OrganizerManageEvent", "Failed to fetch user " + deviceId, ex);
                }

                if (remaining.decrementAndGet() == 0) {
                    // All fetched - write CSV
                    writeCsvAndShare(rows, eventTitle);
                }
            });
        }
    }

    private void writeCsvAndShare(List<String[]> rows, String eventTitle) {
        // Build CSV content bytes
        StringBuilder sb = getStringBuilder(rows);
        byte[] bytes = sb.toString().getBytes();

        // File name with timestamp
        String safeTitle = (eventTitle == null || eventTitle.isEmpty()) ? "event" : eventTitle.replaceAll("[^a-zA-Z0-9-_]", "_");
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = safeTitle + "_enrolled_" + time + ".csv";

        // Store pending bytes and fileName, then launch ACTION_CREATE_DOCUMENT so the user can pick Downloads or another location
        pendingCsvBytes = bytes;
        pendingCsvFileName = fileName;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        // Use ActivityResultLauncher instead of deprecated startActivityForResult
        try {
            createFileLauncher.launch(intent);
        } catch (Exception ex) {
            Log.e("OrganizerManageEvent", "Failed to launch create document picker", ex);
            Toast.makeText(this, "Unable to open save dialog: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    private static StringBuilder getStringBuilder(List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                String cell = row[i] == null ? "" : row[i];
                // Escape quotes
                cell = cell.replace("\"", "\"\"");
                if (cell.contains(",") || cell.contains("\n") || cell.contains("\"")) {
                    sb.append('"').append(cell).append('"');
                } else {
                    sb.append(cell);
                }
                if (i < row.length - 1) sb.append(',');
            }
            sb.append('\n');
        }
        return sb;
    }

}
