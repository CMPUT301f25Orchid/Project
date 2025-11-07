package com.example.fairdraw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fairdraw.R;
import com.example.fairdraw.DBs.UserDB;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.example.fairdraw.ServiceUtility.GatePrefs;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.fairdraw.Models.*;

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
            String name = nameEt.getText().toString().trim();
            String email = emailEt.getText().toString().trim();
            String phone = phoneEt.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            final String deviceId = DevicePrefsManager.getDeviceId(this);
            User user = new User(name, email, phone, deviceId, /*fcmToken*/ null);

            UserDB.upsertUser(user, (ok, e) -> {
                if (!ok) {
                    String msg = (e != null) ? e.getMessage() : "Unknown error";
                    Toast.makeText(this, "Failed to create account: " + msg, Toast.LENGTH_LONG).show();
                    return;
                }
                GatePrefs.setKnownExists(this, true);
                intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
                finish();
            });
        });
    }
}
