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

/**
 * Launcher activity that determines whether the app should route to the sign-up flow or the
 * entrant home experience based on whether a user document exists for the current device.
 *
 * It performs a quick asynchronous check against {@link UserDB} and starts either
 * {@link SignUpActivity} or {@link EntrantHomeActivity} accordingly.
 */
public class SplashActivity extends AppCompatActivity {

    Intent intent;

    /**
     * Small launcher activity that decides whether to route to SignUpActivity or EntrantHomeActivity
     * based on whether a user document exists for this device id. Updates GatePrefs accordingly.
     * @param savedInstanceState saved state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String deviceId = DevicePrefsManager.getDeviceId(this);
        UserDB.userExists(deviceId, ((exists, e) -> {
            if (e != null) {
                Log.d("SplashActivity", "Error checking if user exists:");
                startActivity(new Intent(this, SignUpActivity.class));
            }
            else if (exists) {
                Log.d("SplashActivity", "User exists, redirecting to EntrantHomeActivity:");
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