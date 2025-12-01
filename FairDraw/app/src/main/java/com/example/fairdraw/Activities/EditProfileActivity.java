package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
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

    private Button saveChangesButton, cancelButton, returnHomeButton;
    private EditText nameEditText, emailEditText, phoneEditText;
    private String deviceId; //Deviceid used to identify user

    private MaterialSwitch notificationSwitch;
    private User currentUser; // Store the fetched user object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_edit);

        //Initialize views
        saveChangesButton = findViewById(R.id.btnSaveChanges);
        cancelButton = findViewById(R.id.btnCancel);
        returnHomeButton = findViewById(R.id.btnReturnHome);
        nameEditText = findViewById(R.id.etName);
        emailEditText = findViewById(R.id.etEmail);
        phoneEditText = findViewById(R.id.etPhone);
        notificationSwitch = findViewById(R.id.swNotifications);

        // Get User ID
        deviceId = DevicePrefsManager.getDeviceId(this);

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
