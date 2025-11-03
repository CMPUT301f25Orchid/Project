package com.example.fairdraw;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String deviceId = DevicePrefsManager.getDeviceId(this);
        // Check if the user exists in the database and set the gate preference accordingly
        if (GatePrefs.getKnownExists(this)) {
            startActivity(new Intent(this, MainActivity.class));
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
                Log.d("SplashActivity", "User exists, redirecting to MainActivity:");
                startActivity(new Intent(this, MainActivity.class));
            }
            else {
                Log.d("SplashActivity", "User does not exist, redirecting to SignUpActivity:");
                startActivity(new Intent(this, SignUpActivity.class));
            }
            finish();
        }));
//        EdgeToEdge.enable(this);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
}