package com.example.fairdraw;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class DeepLinkRouterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent in = getIntent();
        Uri data = (in != null) ? in.getData() : null;

        if (data == null || !"fairdraw".equals(data.getScheme()) || !"open".equals(data.getHost())) {
            Toast.makeText(this, "Invalid link", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String destClassName = data.getQueryParameter("dest");
        if (destClassName == null || destClassName.isEmpty()) {
            Toast.makeText(this, "Missing destination", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            Class<?> clazz = Class.forName(destClassName);
            Intent forward = new Intent(this, clazz);

            // Copy all query params (except 'dest') as extras
            Set<String> names = data.getQueryParameterNames();
            for (String k : names) {
                if ("dest".equals(k)) continue;
                String v = data.getQueryParameter(k);
                // Query params are strings; add as String extras
                forward.putExtra(k, v);
            }

            startActivity(forward);
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Destination not found", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
