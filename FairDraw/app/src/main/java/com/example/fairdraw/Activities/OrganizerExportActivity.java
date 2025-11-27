package com.example.fairdraw.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;

import java.io.IOException;
import java.io.OutputStream;

public class OrganizerExportActivity extends AppCompatActivity {
    String eventId;
    Event event;
    ImageView ivHeroImage;
    TextView tvTitle;
    TextView tvRegStatus;
    TextView tvDescription;
    RecyclerView rvFinalList;
    Button btnReturnHome;
    Button btnDownloadFinalEntrants;
    FirebaseImageStorageService storageService;

    // Fields used for ACTION_CREATE_DOCUMENT flow using Activity Result API
    private ActivityResultLauncher<Intent> createFileLauncher;
    private byte[] pendingCsvBytes = null;
    private String pendingCsvFileName = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_final_entrants);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.top_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        rvFinalList = findViewById(R.id.rvFinalList);
        btnReturnHome = findViewById(R.id.btnReturn);
        btnDownloadFinalEntrants = findViewById(R.id.btnDownloadFinalEntrants);

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
    }
}
