package com.example.fairdraw;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView previewImage;
    private Button pickButton, uploadEntrantBtn, uploadEventBtn, openNotifications;

    @Nullable
    private Uri pickedImageUri = null;
    @Nullable
    private Bitmap sampleBitmap = null; // optional path

    private FirebaseImageStorageService storageService;

    // simple demo IDs
    private static final String DEMO_ENTRANT_ID = "demo_entrant_123";
    private static final String DEMO_EVENT_ID   = "demo_event_ABC";

    private Button entrantHomeScreen;


    // Gallery picker (images only)
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedImageUri = uri;
                    previewImage.setImageURI(uri);
                    sampleBitmap = null; // if user picked, ignore sample
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.organizer_navigation_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        storageService = new FirebaseImageStorageService();
        previewImage = findViewById(R.id.previewImage);
        pickButton = findViewById(R.id.testButton);
        uploadEntrantBtn = findViewById(R.id.btnUploadEntrant);
        uploadEventBtn = findViewById(R.id.btnUploadEvent);
        openNotifications= findViewById(R.id.btnOpenNotifications);


        pickButton.setOnClickListener(v -> pickImage.launch("image/*"));

        // (Optional) If you want to try a generated sample image instead of gallery:
        // pickButton.setOnLongClickListener(v -> {
        //     sampleBitmap = makeSampleBitmap(200, 200);
        //     previewImage.setImageBitmap(sampleBitmap);
        //     pickedImageUri = null;
        //     Toast.makeText(this, "Sample image generated (long-press).", Toast.LENGTH_SHORT).show();
        //     return true;
        // });

        uploadEntrantBtn.setOnClickListener(v -> {
            if (pickedImageUri != null) {
                Task<Uri> t = storageService.uploadEntrantProfile(DEMO_ENTRANT_ID, pickedImageUri);
                handleUploadTask(t, "Entrant");
            }
        });
        uploadEventBtn.setOnClickListener(v -> {
            if (pickedImageUri != null) {
                Task<Uri> t = storageService.uploadEventPoster(DEMO_EVENT_ID, pickedImageUri);
                handleUploadTask(t, "Event");
            }
        });

        entrantHomeScreen = findViewById(R.id.entrantHomeScreen);

        entrantHomeScreen.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EntrantHomeActivity.class);
            startActivity(intent);
        });


        String deviceId = DevicePrefsManager.getDeviceId(this);
        EntrantDB.addEntrant(new Entrant(deviceId), ok -> seedDummyNotifications(deviceId));


        // Open notifications
        openNotifications.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EntrantNotificationsActivity.class))
        );



        // Add a button that sends me to the EntrantEventDetails activity
        Button btn = findViewById(R.id.my_test);
        btn.setText("Test");
        btn.setOnClickListener(v -> {
            String testEventId = "3bc7da3d-3426-4f94-b2f8-004ffaf3604f";
            Intent intent = new Intent(this, EntrantEventDetails.class);
            intent.putExtra("event_id", testEventId);
            startActivity(intent);
            Log.d("test", "test");
        });
    }

    private void seedDummyNotifications(String deviceId) {
        List<EntrantNotification> dummies = Arrays.asList(
                new EntrantNotification(NotificationType.WIN,      "evt_swim",  "Swimming Lessons"),
                new EntrantNotification(NotificationType.LOSE,     "evt_cook",  "Cooking 101"),
                new EntrantNotification(NotificationType.WAITLIST, "evt_ball",  "Basketball Camp"),
                new EntrantNotification(NotificationType.REPLACE,  "evt_piano", "Piano Basics")
        );
        for (EntrantNotification n : dummies) {
            EntrantDB.pushNotificationToUser(deviceId, n, (ok, e) -> {
                if (!ok) {
                    Toast.makeText(this, "Seed failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        Toast.makeText(this, "Seed request sent to entrants/" + deviceId, Toast.LENGTH_SHORT).show();
    }


    private void handleUploadTask(Task<Uri> task, String label) {
        task.addOnSuccessListener(uri -> {
            Toast.makeText(this, label + " uploaded!\nURL:\n" + uri, Toast.LENGTH_LONG).show();
            // Tip: save uri.toString() to Firestore if you want to reference it later
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}