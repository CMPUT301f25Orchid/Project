package com.example.fairdraw.Activities;

import static com.example.fairdraw.DBs.OrganizerDB.getOrganizerCollection;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fairdraw.DBs.EntrantDB;
import com.example.fairdraw.R;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.fairdraw.Models.*;

import java.util.Date;

/**
 * Screen used to create a new user account. Collects basic user info and writes a
 * {@link com.example.fairdraw.Models.User} to Firestore using {@link UserDB#upsertUser}.
 */
public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText nameEt, emailEt, phoneEt;
    private Button submitBtn;

    Intent intent;

    /**
     * Activity entry point. Wires the sign up form and submits user data to the database.
     * @param savedInstanceState saved state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEt = findViewById(R.id.fullName);
        emailEt = findViewById(R.id.etEmail);
        phoneEt = findViewById(R.id.etPhone);
        submitBtn = findViewById(R.id.btnSignUp);

        submitBtn.setOnClickListener(v -> {
            String name = (nameEt.getText() == null ? "" : nameEt.getText().toString().trim());
            String email = (emailEt.getText() == null ? "" : emailEt.getText().toString().trim());
            String phone = (phoneEt.getText() == null ? "" : phoneEt.getText().toString().trim());

            // phone can be empty, but name and email are required
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
                // Use Snackbar instead of Toast
                Snackbar.make(
                        v,   // root view of the Activity
                        "Please fill all fields.",
                        Snackbar.LENGTH_SHORT
                ).show();
                return;
            }

            // Check that email is in email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // Use Snackbar instead of Toast
                Snackbar.make(
                        v,
                        "Please enter a valid email address.",
                        Snackbar.LENGTH_SHORT
                ).show();
                return;
            }

            final String deviceId = DevicePrefsManager.getDeviceId(this);
            // set dateJoined to now
            User user = new User(name, email, phone, deviceId, /*fcmToken*/ null, new Date());

            UserDB.upsertUser(user, (ok, e) -> {
                if (!ok) {
                    String msg = (e != null) ? e.getMessage() : "Unknown error";
                    Snackbar.make(findViewById(android.R.id.content), "Failed to create account: " + msg, Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ✅ Ensure an Entrant document exists
                Entrant entrant = new Entrant(deviceId);
                EntrantDB.addEntrant(entrant, (success) -> {
                    if (!success) {
                        // You can log or show a non-blocking warning,
                        // but don't block the user from continuing
                        Log.w("SignUpActivity", "Failed to create Entrant doc");
                    }

                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                    finish();
                });
            });
        });
    }
}
