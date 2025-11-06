package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.example.fairdraw.ServiceUtility.GatePrefs;
import com.example.fairdraw.R;

public class SplashActivity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String deviceId = DevicePrefsManager.getDeviceId(this);
        // Check if the user exists in the database and set the gate preference accordingly
        if (GatePrefs.getKnownExists(this)) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            UserDB.userExists(deviceId, ((exists, e) -> {
                if (e == null) GatePrefs.setKnownExists(this, exists);
            }));
        }

        UserDB.userExists(deviceId, ((exists, e) -> {
            if (e != null) {
                Log.d("SplashActivity", "Error checking if user exists:");
                startActivity(new Intent(this, SignUpActivity.class));
            }
            else if (exists) {
                Log.d("SplashActivity", "User exists, redirecting to ProfileActivity:");
                intent = new Intent(this, EntrantHomeActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            }
            else {
                Log.d("SplashActivity", "User does not exist, redirecting to SignUpActivity:");
                intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
            }
            finish();
        }));
    }
}