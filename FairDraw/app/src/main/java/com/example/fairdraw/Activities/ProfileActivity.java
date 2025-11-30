package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.Models.User;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
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

    private ListenerRegistration userListener;

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
            // Handle edit button click
            // Start EditProfileActivity
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
//                    // IMPORTANT: Finish the activity to return to the previous screen.
//                    // This is what makes the test pass.
//                    finish();
//                } else {
//                    // There was an error deleting the user
//                    Log.e(TAG, "Failed to delete user account.", e);
//                    Toast.makeText(ProfileActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });    NEEDS TO BE COMPLETED, SHOULD DELETING THE ACCOUNT RETURN YOU TO THE SIGN UP PAGE INSTEAD OF JUST FINISHING THE ACTIVITY

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
                    //User was found, now update the UI
                    //Ensure you are on the main thread when updating UI
                    runOnUiThread(() -> {
                        nameTextView.setText(user.getName());
                        emailTextView.setText(user.getEmail());
                        phoneTextView.setText(user.getPhoneNum());

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
