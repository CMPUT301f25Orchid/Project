package com.example.fairdraw.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

/**
 * Small helper Activity that routes app-specific deep links to an internal Activity class.
 *
 * The 'dest' query parameter must be the fully qualified Activity class name. All other
 * query parameters are copied into the forwarded Intent as String extras.
 */
public class DeepLinkRouterActivity extends AppCompatActivity {

    /**
     * Reads the incoming Intent Uri, validates required parameters and forwards the Intent to
     * the destination Activity. Shows a Toast on invalid links or when the destination class
     * cannot be found.
     *
     * @param savedInstanceState previous state bundle
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent in = getIntent();
        Uri data = (in != null) ? in.getData() : null;

        if (data == null || !"fairdraw".equals(data.getScheme()) || !"open".equals(data.getHost())) {
            Snackbar.make(findViewById(android.R.id.content), "Invalid link", Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }

        String destClassName = data.getQueryParameter("dest");
        if (destClassName == null || destClassName.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Missing destination", Snackbar.LENGTH_SHORT).show();
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
            Snackbar.make(findViewById(android.R.id.content), "Destination not found", Snackbar.LENGTH_SHORT).show();
        }

        finish();
    }
}
