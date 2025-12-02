package com.example.fairdraw.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.AdminDB;
import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.DBs.OrganizerDB;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Models.User;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.google.android.material.materialswitch.MaterialSwitch;

import org.w3c.dom.Text;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private Button saveChangesButton, cancelButton, returnHomeButton, deleteAccButton;
    private EditText nameEditText, emailEditText, phoneEditText;
    private String deviceId; //Deviceid used to identify user

    private MaterialSwitch notificationSwitch;
    private User currentUser; // Store the fetched user object

    private ShapeableImageView avatarImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redirect to splash if user doc missing
        ActivityUtils.ensureUserExistsOrRedirect(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_edit);

        //Initialize views
        saveChangesButton = findViewById(R.id.btnSaveChanges);
        cancelButton = findViewById(R.id.btnCancel);
        returnHomeButton = findViewById(R.id.btnReturnHome);
        deleteAccButton = findViewById(R.id.btnDelete);
        nameEditText = findViewById(R.id.etName);
        emailEditText = findViewById(R.id.etEmail);
        phoneEditText = findViewById(R.id.etPhone);
        notificationSwitch = findViewById(R.id.swNotifications);
        avatarImageView = findViewById(R.id.ivAvatar);

        // Get User ID
        deviceId = DevicePrefsManager.getDeviceId(this);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        // Show a preview of the selected image
                        Glide.with(this).load(selectedImageUri).circleCrop().into(avatarImageView);
                        Snackbar.make(findViewById(android.R.id.content), "Image selected", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "No image selected", Snackbar.LENGTH_SHORT).show();
                    }
                });

        if (deviceId != null && !deviceId.isEmpty()) {
            loadUserData(deviceId);
        } else {
            Log.e(TAG, "No USER_ID was passed to EditProfileActivity.");
            Snackbar.make(findViewById(android.R.id.content), "Cannot load user profile for editing.", Snackbar.LENGTH_LONG).show();
            finish();
        }

        // Set up button listeners
        cancelButton.setOnClickListener(v -> {
            Snackbar.make(findViewById(android.R.id.content), "Changes discarded", Snackbar.LENGTH_SHORT).show();
            finish();
        });

        saveChangesButton.setOnClickListener(v -> {
            saveChanges();
        });

        returnHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantHomeActivity.class);
            startActivity(intent);
            finish();
        });

        deleteAccButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This will also remove all events you have organized. This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // User confirmed, start the deletion process
                        Snackbar.make(findViewById(android.R.id.content), "Deleting account...", Snackbar.LENGTH_SHORT).show();
                        performCascadingDelete(deviceId);
                    })
                    .setNegativeButton("Cancel", null) // Do nothing if canceled
                    .show();
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Prevent this from firing when the switch is set programmatically
            if (currentUser == null || buttonView.isPressed()) {
                updateNotificationPreference(isChecked);
            }
        });

        avatarImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    /**
     * Updates the user's notification preference in Firestore.
     */
    private void updateNotificationPreference(boolean isEnabled) {
        if (currentUser != null) {
            currentUser.setNotificationsEnabled(isEnabled);
            UserDB.upsertUser(currentUser, (ok, e) -> {
                if (ok) {
                    String status = isEnabled ? "enabled" : "disabled";
                    Snackbar.make(findViewById(android.R.id.content), "Notifications " + status, Snackbar.LENGTH_SHORT).show();
                    Log.d(TAG, "Notification preference updated successfully.");
                } else {
                    // Revert the switch state on failure to provide accurate UI feedback
                    notificationSwitch.setChecked(!isEnabled);
                    Snackbar.make(findViewById(android.R.id.content), "Failed to update preference", Snackbar.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update notification preference.", e);
                }
            });
        }
    }

    /**
     * Performs a cascading delete of user data.
     * 1. Deletes user from Entrant, Organiser, and Admin (if necessary) collection.
     * Deleting the user as an organizer makes changes to linked documents as necessary
     * 2. Deletes the main user document.
     * 3. Navigates back to the sign-up screen.
     * @param deviceId The ID of the user to delete.
     */
    private void performCascadingDelete(String deviceId) {
        // Delete user from Entrant, Organiser and Admin collections.
        EntrantDB.deleteEntrant(deviceId, ok -> {
            if (!ok) {
                Log.e(TAG, "Failed to delete from Entrants, but continuing deletion process.");
            }
        });
        OrganizerDB.deleteOrganizer(deviceId, ok -> {
            if (!ok) {
                Log.e(TAG, "Failed to delete from Organizers, but continuing deletion process.");
            }
        });

        AdminDB.deleteAdmin(deviceId, ok -> {
            if (!ok) {
                Log.e(TAG, "Failed to delete from Admins, but continuing deletion process.");
            }
        });


        // After a brief delay to allow role deletion to start, delete the main user document.
        UserDB.deleteUser(deviceId, (finalOk, finalE) -> {
            if (finalOk) {
                Log.d(TAG, "User account deleted successfully.");
                Snackbar.make(findViewById(android.R.id.content), "Account deleted.", Snackbar.LENGTH_SHORT).show();


                // Navigate back to the main sign-up/entry activity
                Intent intent = new Intent(EditProfileActivity.this, SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                handleDeleteError(finalE);
            }
        });
    }

    private void handleDeleteError(Exception e) {
        Log.e(TAG, "Failed to delete user account.", e);
        Snackbar.make(findViewById(android.R.id.content), "Failed to delete account.", Snackbar.LENGTH_SHORT).show();
    }

    private void loadUserData(String userId) {
        UserDB.getUserOrNull(userId, (user, e) -> {
            if (e != null) {
                Log.e(TAG, "Error loading user data.", e);
                Snackbar.make(findViewById(android.R.id.content), "Failed to load data.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (user != null) {
                currentUser = user;
                runOnUiThread(() -> {
                    nameEditText.setText(user.getName());
                    emailEditText.setText(user.getEmail());
                    phoneEditText.setText(user.getPhoneNum());
                    notificationSwitch.setChecked(user.isNotificationsEnabled());

                    // Fetch user profile picture from image storage
                    FirebaseImageStorageService imageStorage = new FirebaseImageStorageService();
                    imageStorage.getEntrantProfileDownloadUrl(userId).addOnSuccessListener(uri -> {
                        Glide.with(this).load(uri).circleCrop().into(avatarImageView);
                    }).addOnFailureListener(e1 -> {
                        Log.e(TAG, "Failed to load profile picture.", e);
                    });
                });
            } else {
                Log.w(TAG, "User with ID " + userId + " not found.");
                Snackbar.make(findViewById(android.R.id.content), "User not found.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        if (currentUser == null) {
            Snackbar.make(findViewById(android.R.id.content), "Cannot save, user data not loaded.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Get updated text from EditText fields
        String newName = nameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        String newPhone = phoneEditText.getText().toString().trim();
        boolean notificationsAreEnabled = notificationSwitch.isChecked();


        // Update the local currentUser object with the new data
        currentUser.setName(newName);
        currentUser.setEmail(newEmail);
        currentUser.setPhoneNum(newPhone);
        currentUser.setNotificationsEnabled(notificationsAreEnabled);

        // Update the profile picture if a new image was selected
        if (selectedImageUri != null) {
            FirebaseImageStorageService imageStorage = new FirebaseImageStorageService();
            imageStorage.uploadEntrantProfile(deviceId, selectedImageUri);
        }

        // Use the existing upsertUser method
        UserDB.upsertUser(currentUser, (ok, e) -> {
            if (ok) {
                // Success
                Log.d(TAG, "User profile updated successfully.");
                Snackbar.make(findViewById(android.R.id.content), "Profile updated!", Snackbar.LENGTH_SHORT).show();
                finish(); // Close the activity and return
            } else {
                // Failure
                Log.e(TAG, "Error updating user profile.", e);
                Snackbar.make(findViewById(android.R.id.content), "Failed to update profile.", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
