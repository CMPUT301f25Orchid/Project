package com.example.fairdraw.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.AdminDB;
import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.DBs.OrganizerDB;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Models.User;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * ProfileActivity displays the user's profile information and allows editing and
 * deletion of the account. It expects a "deviceId" string extra to fetch the user data.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    //Declaring UI elements
    private Button editButton, returnButton, deleteAccButton, viewHistoryButton;
    private TextView nameTextView, emailTextView, phoneTextView;
    private MaterialSwitch notificationSwitch;

    private ListenerRegistration userListener;
    private User currentUser;
    private ShapeableImageView avatarImageView;


    /**
     * Activity that displays a user's profile information. Expects a "deviceId" string extra.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redirect to splash if the underlying user doc is missing or deleted
        ActivityUtils.ensureUserExistsOrRedirect(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        //Initializing UI elements
        nameTextView = findViewById(R.id.etName);
        emailTextView = findViewById(R.id.etEmail);
        phoneTextView = findViewById(R.id.etPhone);
        notificationSwitch = findViewById(R.id.swNotifications);
        avatarImageView = findViewById(R.id.ivAvatar);

        editButton = findViewById(R.id.btnEdit);
        returnButton = findViewById(R.id.btnReturnHome);
        deleteAccButton = findViewById(R.id.btnDelete);
        viewHistoryButton = findViewById(R.id.btnViewHistory);

        //Getting the device ID passed from the previous activity
        String deviceId = DevicePrefsManager.getDeviceId(this);

        if (deviceId != null && !deviceId.isEmpty()){
            //Fetching user data from Firestore based on the device ID
            fetchAndDisplayUserData(deviceId);
            //Log.d(TAG, "Device ID received: " + deviceId);

        } else {
            //Handle the case where no device ID is provided
            Log.e(TAG, "No device ID provided in Intent extras.");
            Snackbar.make(findViewById(android.R.id.content), "Could not load user profile.", Snackbar.LENGTH_SHORT).show();
            finish();
        }

        editButton.setOnClickListener(View -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantHomeActivity.class);
            startActivity(intent);
            finish();
        });


        deleteAccButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This will also affect all events you have organized. This action cannot be undone.")
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
                runOnUiThread(() -> {
                    Snackbar.make(findViewById(android.R.id.content), "Account deleted.", Snackbar.LENGTH_SHORT).show();

                    // Clear cached account data including device id before navigating to SignUpActivity
                    DevicePrefsManager.clearCachedAccountData(ProfileActivity.this);

                    // Navigate back to the main sign-up/entry activity
                    Intent intent = new Intent(ProfileActivity.this, SignUpActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            } else {
                handleDeleteError(finalE);
            }
        });
    }

    private void handleDeleteError(Exception e) {
        Log.e(TAG, "Failed to delete user account.", e);
        Snackbar.make(findViewById(android.R.id.content), "Failed to delete account.", Snackbar.LENGTH_SHORT).show();
    }


    /**
     * Fetches user data from Firestore based on the provided device ID and populates UI.
     * @param deviceId The device ID of the user to fetch data for.
     */
    private void fetchAndDisplayUserData(String deviceId) {
        userListener = UserDB.addUserSnapshotListener(deviceId, new UserDB.GetUserCallback() {
            @Override
            public void onCallback(@Nullable User user, @Nullable Exception e) {
                if (e != null) {
                    Log.e(TAG, "Error fetching user data.", e);
                    Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (user != null) {
                    currentUser = user;
                    //User was found, now update the UI
                    //Ensure you are on the main thread when updating UI
                    runOnUiThread(() -> {
                        nameTextView.setText(user.getName());
                        emailTextView.setText(user.getEmail());
                        phoneTextView.setText(user.getPhoneNum());
                        notificationSwitch.setChecked(user.isNotificationsEnabled());
                        // Fetch user profile picture from image storage
                        FirebaseImageStorageService imageStorage = new FirebaseImageStorageService();
                        imageStorage.getEntrantProfileDownloadUrl(deviceId).addOnSuccessListener(uri -> {
                            Glide.with(ProfileActivity.this).load(uri).circleCrop().into(avatarImageView);
                        }).addOnFailureListener(e1 -> {
                            Log.e(TAG, "Failed to load profile picture.", e);
                        });
                    });
                } else {
                    //User not found
                    Log.e(TAG, "User not found for device ID: " + deviceId);
                    Snackbar.make(findViewById(android.R.id.content), "User not found.", Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }
}
