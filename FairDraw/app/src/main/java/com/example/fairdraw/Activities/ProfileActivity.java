package com.example.fairdraw.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.DBs.EventDB;
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


    /**
     * Activity that displays a user's profile information. Expects a "deviceId" string extra.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        //Initializing UI elements
        nameTextView = findViewById(R.id.etName);
        emailTextView = findViewById(R.id.etEmail);
        phoneTextView = findViewById(R.id.etPhone);
        notificationSwitch = findViewById(R.id.swNotifications);

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
            Toast.makeText(this, "Could not load user profile.", Toast.LENGTH_SHORT).show();
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

//        deleteAccButton.setOnClickListener(v -> {
//            // Call the deleteUser method from UserDB
//            UserDB.deleteUser(deviceId, (ok, e) -> {
//                // This callback runs after the delete operation is complete
//                if (ok) {
//                    // The user was successfully deleted
//                    Log.d(TAG, "User account deleted successfully.");
//                    Toast.makeText(ProfileActivity.this, "Account deleted.", Toast.LENGTH_SHORT).show();
//
//                    // Navigate back to the main sign-up/entry activity
//                    Intent intent = new Intent(ProfileActivity.this, SignUpActivity.class);
//
//                    // FLAG_ACTIVITY_NEW_TASK: Starts the activity in a new task.
//                    // FLAG_ACTIVITY_CLEAR_TASK: Clears the existing task stack, so the user
//                    //    cannot navigate back to the authenticated parts of the app.
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//                    startActivity(intent);
//
//                    // Finish the current activity
//                    finish();
//                } else {
//                    // There was an error deleting the user
//                    Log.e(TAG, "Failed to delete user account.", e);
//                    Toast.makeText(ProfileActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
//                }
//            }); //OLD DELETE METHOD, REMOVE WHEN DONE WITH NEW ONE
//        });

//        deleteAccButton.setOnClickListener(v -> {
//            new AlertDialog.Builder(this)
//                    .setTitle("Delete Account")
//                    .setMessage("Are you sure you want to delete your account? This will also remove all events you have organized. This action cannot be undone.")
//                    .setPositiveButton("Delete", (dialog, which) -> {
//                        // User confirmed, start the deletion process
//                        Toast.makeText(ProfileActivity.this, "Deleting account...", Toast.LENGTH_SHORT).show();
//                        performCascadingDelete(deviceId);
//                    })
//                    .setNegativeButton("Cancel", null) // Do nothing if canceled
//                    .show();
//        });

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
                    Toast.makeText(this, "Notifications " + status, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Notification preference updated successfully.");
                } else {
                    // Revert the switch state on failure to provide accurate UI feedback
                    notificationSwitch.setChecked(!isEnabled);
                    Toast.makeText(this, "Failed to update preference", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update notification preference.", e);
                }
            });
        }
    }

//    /**
//     * Performs a cascading delete of user data.
//     * 1. Deletes events organized by the user.
//     * 2. Deletes the user's roles (Organiser, Entrant, etc.).
//     * 3. Deletes the main user document.
//     * 4. Navigates back to the sign-up screen.
//     * @param deviceId The ID of the user to delete.
//     */
//    private void performCascadingDelete(String deviceId) {
//        // 1: Delete all events organized by this user.
//        // NOTE: ANDREW WORKING ON THIS METHOD. WHEN DONE CHANGE AS NECESSARY
//        //SCROLL UP ON GEMINI TO SEE THE SUGGESTED METHOD THAT DELETES ALL EVENTS
//        EventDB.deleteEventsByOrganizer(deviceId, (ok, e) -> {
//            if (ok) {
//                Log.d(TAG, "Successfully deleted events for user: " + deviceId);
//                // 2: Now delete the user's role documents. We can do this in parallel.
//                // NOTE: ADD/UNCOMMENT DELETE ROLE METHODS IN USERDB
//                UserDB.deleteRole("Organisers", deviceId, (ok1, e1) -> {});
//                UserDB.deleteRole("Entrants", deviceId, (ok2, e2) -> {});
//                UserDB.deleteRole("Admins", deviceId, (ok3, e3) -> {}); // If you have an Admins collection
//
//                // 3: After a brief delay to allow role deletion to start, delete the main user document.
//                UserDB.deleteUser(deviceId, (finalOk, finalE) -> {
//                    if (finalOk) {
//                        Log.d(TAG, "User account deleted successfully.");
//                        Toast.makeText(ProfileActivity.this, "Account deleted.", Toast.LENGTH_SHORT).show();
//
//                        // Step 4: Navigate back to the main sign-up/entry activity
//                        Intent intent = new Intent(ProfileActivity.this, SignUpActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                        finish();
//                    } else {
//                        handleDeleteError(finalE);
//                    }
//                });
//            } else {
//                handleDeleteError(e);
//            }
//        }); //DON'T FORGET TO ADD IT IN EDIT PROFILE ASW
//    }

    private void handleDeleteError(Exception e) {
        Log.e(TAG, "Failed to delete user account.", e);
        Toast.makeText(ProfileActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

                    });
                } else {
                    //User not found
                    Log.e(TAG, "User not found for device ID: " + deviceId);
                    Toast.makeText(ProfileActivity.this, "User not found.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
        }
    }
}
