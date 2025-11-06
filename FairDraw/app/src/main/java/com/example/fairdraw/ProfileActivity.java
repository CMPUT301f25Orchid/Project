package com.example.fairdraw;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    //Declaring UI elements
    private Button editButton, returnButton, deleteAccButton, viewHistoryButton;
    private TextView nameTextView, usernameTextView, emailTextView, phoneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        
        //Initializing UI elements
        nameTextView = findViewById(R.id.etName);
        usernameTextView = findViewById(R.id.etUsername);
        emailTextView = findViewById(R.id.etEmail);
        phoneTextView = findViewById(R.id.etPhone);

        editButton = findViewById(R.id.btnEdit);
        returnButton = findViewById(R.id.btnReturnHome);
        deleteAccButton = findViewById(R.id.btnDelete);
        viewHistoryButton = findViewById(R.id.btnViewHistory);

        //Getting the device ID passed from the previous activity
        String deviceId = getIntent().getStringExtra("deviceId");

        if (deviceId != null && !deviceId.isEmpty()){
            //Fetching user data from Firestore based on the device ID
            // fetchAndDisplayUserData(deviceId);
            Log.d(TAG, "Device ID received: " + deviceId);

        } else {
            //Handle the case where no device ID is provided
            Log.e(TAG, "No device ID provided in Intent extras.");
            Toast.makeText(this, "Could not load user profile.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Fetches user data from Firestore based on the provided device ID and populates UI.
     * @param deviceId The device ID of the user to fetch data for.
     */
    private void fetchAndDisplayUserData(String deviceId) {
        UserDB.getUserOrNull(deviceId, new UserDB.GetUserCallback() {
            @Override
            public void onCallback(@Nullable User user, @Nullable Exception e) {
                if (e != null) {
                    Log.e(TAG, "Error fetching user data.", e);
                    Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                ;

                if (user != null) {
                    //User was found, now update the UI
                    //Ensure you are on the main
                }

            }
        });
    }
}


