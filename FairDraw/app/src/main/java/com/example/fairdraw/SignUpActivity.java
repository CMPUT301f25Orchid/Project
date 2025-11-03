package com.example.fairdraw;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText nameEt, emailEt, phoneEt;
    private Button submitBtn;

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
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        });
    }
}
