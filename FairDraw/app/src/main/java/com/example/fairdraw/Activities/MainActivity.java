package com.example.fairdraw.Activities;

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

import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.example.fairdraw.Others.EntrantNotification;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.example.fairdraw.Models.Entrant;
import com.example.fairdraw.Others.NotificationType;
import com.example.fairdraw.R;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;
import java.util.List;

/**
 * Main activity of the FairDraw application.
 * Serves as a test/demo activity for image upload functionality and navigation to other screens.
 * Provides buttons for uploading entrant profiles, event posters, and accessing notifications.
 */
public class MainActivity extends AppCompatActivity {

    /** ImageView for previewing selected images */
    private ImageView previewImage;
    /** UI buttons for image picking, uploading, and navigation */
    private Button pickButton, uploadEntrantBtn, uploadEventBtn, openNotifications;

    /** URI of the image selected from gallery */
    @Nullable
    private Uri pickedImageUri = null;
    /** Sample bitmap for testing (optional) */
    @Nullable
    private Bitmap sampleBitmap = null;

    /** Service for handling Firebase Storage operations */
    private FirebaseImageStorageService storageService;

    /** Demo entrant ID for testing uploads */
    private static final String DEMO_ENTRANT_ID = "demo_entrant_123";
    /** Demo event ID for testing uploads */
    private static final String DEMO_EVENT_ID   = "demo_event_ABC";

    /** Button for navigating to entrant home screen */
    private Button entrantHomeScreen;


    /** Activity result launcher for picking images from gallery */
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

    /**
     * Called when the activity is created.
     * Initializes UI components, sets up Firebase Storage, and configures button click listeners.
     * 
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
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

        Button myTest = findViewById(R.id.my_test);
        myTest.setOnClickListener(v -> {
            // Send user to organizer manage event activity
            Intent intent = new Intent(this, OrganizerManageEvent.class);
            intent.putExtra("eventId", "66aab1da-7e37-4f03-8eeb-51e9af9ffc55");
            startActivity(intent);
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

    /**
     * Seeds dummy notifications for testing purposes.
     * Creates sample notifications of different types and adds them to the specified user.
     * 
     * @param deviceId The device ID of the user to receive test notifications
     */
    private void seedDummyNotifications(String deviceId) {
        List<EntrantNotification> dummies = Arrays.asList(
                new EntrantNotification(NotificationType.WIN,      "evt_swim",  "Swimming Lessons"),
                new EntrantNotification(NotificationType.LOSE,     "evt_cook",  "Cooking 101"),
                new EntrantNotification(NotificationType.WAITLIST, "evt_ball",  "Basketball Camp"),
                new EntrantNotification(NotificationType.REPLACE,  "evt_piano", "Piano Basics")
        );
        for (EntrantNotification notification : dummies) {
            EntrantDB.pushNotificationToUser(deviceId, notification, (ok, e) -> {
                if (!ok) {
                    Toast.makeText(this, "Seed failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        Toast.makeText(this, "Seed request sent to entrants/" + deviceId, Toast.LENGTH_SHORT).show();
    }


    /**
     * Handles the result of an image upload task.
     * Displays success or failure messages to the user.
     * 
     * @param task The upload Task containing the download URL
     * @param label A label identifying what was uploaded (e.g., "Entrant", "Event")
     */
    private void handleUploadTask(Task<Uri> task, String label) {
        task.addOnSuccessListener(uri -> {
            Toast.makeText(this, label + " uploaded!\nURL:\notification" + uri, Toast.LENGTH_LONG).show();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}