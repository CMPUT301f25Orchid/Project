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

public class MainActivity extends AppCompatActivity {

    private ImageView previewImage;
    private Button pickButton, uploadEntrantBtn, uploadEventBtn;

    @Nullable
    private Uri pickedImageUri = null;
    @Nullable
    private Bitmap sampleBitmap = null; // optional path

    private FirebaseImageStorageService storageService;

    // simple demo IDs
    private static final String DEMO_ENTRANT_ID = "demo_entrant_123";
    private static final String DEMO_EVENT_ID   = "demo_event_ABC";

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